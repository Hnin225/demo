package com.example.demo.service;

import com.example.demo.entity.Video;
import com.example.demo.entity.VideoAttachment;
import com.example.demo.repository.VideoAttachmentRepository;
import com.example.demo.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class VideoService {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private VideoAttachmentRepository attachmentRepository;

    private final String uploadDir = "uploads/video/";

    private final List<String> allowedExtensions = Arrays.asList(
            "jpg", "jpeg", "png", "svg", "gif",
            "hwp", "doc", "docx", "pdf", "ppt", "pptx", "txt", "xls", "xlsx",
            "gz", "zip", "mp4", "avi", "mov", "wmv", "flv", "mkv"
    );

    private final int MAX_FILES = 5;
    private final long MAX_TOTAL_SIZE = 90 * 1024 * 1024;

    public List<Video> getAllVideos() {
        return videoRepository.findAllOrderByPinnedAndCreatedAt();
    }

    public Optional<Video> getVideoById(Long id) {
        return videoRepository.findById(id);
    }

    public List<Video> searchVideosAdvanced(String searchType, String keyword,
                                            String status,
                                            String startDate, String endDate) {
        List<Video> videos = videoRepository.findAllOrderByPinnedAndCreatedAt();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String finalKeyword = keyword.trim();
            videos = videos.stream()
                    .filter(v -> {
                        if ("제목".equals(searchType)) {
                            return v.getTitle().contains(finalKeyword);
                        } else if ("작성자".equals(searchType)) {
                            return v.getAuthor() != null && v.getAuthor().contains(finalKeyword);
                        } else {
                            return v.getTitle().contains(finalKeyword) ||
                                    (v.getAuthor() != null && v.getAuthor().contains(finalKeyword));
                        }
                    })
                    .toList();
        }

        if (status != null && !status.equals("전체")) {
            String finalStatus = status;
            videos = videos.stream()
                    .filter(v -> finalStatus.equals(v.getStatus()))
                    .toList();
        }

        if (startDate != null && !startDate.trim().isEmpty()) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            videos = videos.stream()
                    .filter(v -> v.getCreatedAt().isAfter(start) || v.getCreatedAt().isEqual(start))
                    .toList();
        }

        if (endDate != null && !endDate.trim().isEmpty()) {
            LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
            videos = videos.stream()
                    .filter(v -> v.getCreatedAt().isBefore(end) || v.getCreatedAt().isEqual(end))
                    .toList();
        }

        return videos;
    }

    @Transactional
    public Video saveVideo(Video video, MultipartFile[] files) throws IOException {

        if (video.getStartDate() != null && video.getEndDate() != null) {
            if (video.getEndDate().isBefore(video.getStartDate())) {
                throw new IllegalArgumentException("게시 종료일은 시작일보다 이후여야 합니다.");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        if (video.getStartDate() != null && video.getStartDate().isAfter(now)) {
            video.setStatus("예약");
        } else if (video.getEndDate() != null && video.getEndDate().isBefore(now)) {
            video.setStatus("게시 종료");
        } else {
            video.setStatus("게시 중");
        }

        Video savedVideo = videoRepository.save(video);

        if (files != null && files.length > 0) {
            if (files.length > MAX_FILES) {
                throw new IllegalArgumentException("최대 5개까지 업로드 가능합니다.");
            }

            long totalSize = Arrays.stream(files).mapToLong(MultipartFile::getSize).sum();
            if (totalSize > MAX_TOTAL_SIZE) {
                throw new IllegalArgumentException("총 90MB의 파일을 업로드 할 수 없습니다.");
            }

            int uploadedCount = 0;
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileName = file.getOriginalFilename();
                    String extension = getFileExtension(fileName);

                    if (!isAllowedExtension(extension)) {
                        throw new IllegalArgumentException("허용되지 않은 파일 형식입니다: " + fileName);
                    }

                    String savedFileName = saveFile(file);

                    VideoAttachment attachment = new VideoAttachment();
                    attachment.setVideoId(savedVideo.getId());
                    attachment.setFileName(fileName);
                    attachment.setFilePath(uploadDir + savedFileName);
                    attachment.setFileSize(file.getSize());
                    attachment.setFileType(extension);

                    if (uploadedCount == 0 && isImageFile(extension)) {
                        attachment.setIsRepresentative(true);
                    }

                    attachmentRepository.save(attachment);
                    uploadedCount++;
                }
            }

            savedVideo.setAttachmentCount(uploadedCount);
            videoRepository.save(savedVideo);
        }

        return savedVideo;
    }

    @Transactional
    public void deleteVideo(Long id) {
        Optional<Video> video = videoRepository.findById(id);
        if (video.isPresent()) {
            List<VideoAttachment> attachments = attachmentRepository.findByVideoId(id);
            for (VideoAttachment attachment : attachments) {
                deleteFile(attachment.getFilePath());
            }
            attachmentRepository.deleteByVideoId(id);
            videoRepository.deleteById(id);
        }
    }

    @Transactional
    public void increaseViewCount(Long id) {
        Optional<Video> video = videoRepository.findById(id);
        if (video.isPresent()) {
            Video v = video.get();
            v.setViewCount(v.getViewCount() + 1);
            videoRepository.save(v);
        }
    }

    public List<VideoAttachment> getAttachments(Long videoId) {
        return attachmentRepository.findByVideoId(videoId);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) return "";
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    private boolean isAllowedExtension(String extension) {
        return allowedExtensions.contains(extension.toLowerCase());
    }

    private boolean isImageFile(String extension) {
        return Arrays.asList("jpg", "jpeg", "png", "svg", "gif").contains(extension.toLowerCase());
    }

    private String saveFile(MultipartFile file) throws IOException {
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }

        String originalFileName = file.getOriginalFilename();
        String fileName = System.currentTimeMillis() + "_" + originalFileName;
        Path filePath = Paths.get(uploadDir + fileName);
        Files.write(filePath, file.getBytes());

        return fileName;
    }

    private void deleteFile(String filePath) {
        if (filePath != null) {
            try {
                Path path = Paths.get(filePath);
                Files.deleteIfExists(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}