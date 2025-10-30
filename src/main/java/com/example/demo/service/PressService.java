package com.example.demo.service;

import com.example.demo.entity.Press;
import com.example.demo.entity.PressAttachment;
import com.example.demo.repository.PressAttachmentRepository;
import com.example.demo.repository.PressRepository;
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
public class PressService {

    @Autowired
    private PressRepository pressRepository;

    @Autowired
    private PressAttachmentRepository attachmentRepository;

    private final String uploadDir = "uploads/press/";

    private final List<String> allowedExtensions = Arrays.asList(
            "jpg", "jpeg", "png", "svg", "gif",
            "hwp", "doc", "docx", "pdf", "ppt", "pptx", "txt", "xls", "xlsx",
            "gz", "zip"
    );

    private final int MAX_FILES = 5;
    private final long MAX_TOTAL_SIZE = 90 * 1024 * 1024;

    public List<Press> getAllPress() {
        return pressRepository.findAllOrderByPinnedAndCreatedAt();
    }

    public Optional<Press> getPressById(Long id) {
        return pressRepository.findById(id);
    }

    public List<Press> searchPressAdvanced(String searchType, String keyword,
                                           String status,
                                           String startDate, String endDate) {
        List<Press> pressList = pressRepository.findAllOrderByPinnedAndCreatedAt();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String finalKeyword = keyword.trim();
            pressList = pressList.stream()
                    .filter(p -> {
                        if ("제목".equals(searchType)) {
                            return p.getTitle().contains(finalKeyword);
                        } else if ("작성자".equals(searchType)) {
                            return p.getAuthor() != null && p.getAuthor().contains(finalKeyword);
                        } else {
                            return p.getTitle().contains(finalKeyword) ||
                                    (p.getAuthor() != null && p.getAuthor().contains(finalKeyword));
                        }
                    })
                    .toList();
        }

        if (status != null && !status.equals("전체")) {
            String finalStatus = status;
            pressList = pressList.stream()
                    .filter(p -> finalStatus.equals(p.getStatus()))
                    .toList();
        }

        if (startDate != null && !startDate.trim().isEmpty()) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            pressList = pressList.stream()
                    .filter(p -> p.getCreatedAt().isAfter(start) || p.getCreatedAt().isEqual(start))
                    .toList();
        }

        if (endDate != null && !endDate.trim().isEmpty()) {
            LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
            pressList = pressList.stream()
                    .filter(p -> p.getCreatedAt().isBefore(end) || p.getCreatedAt().isEqual(end))
                    .toList();
        }

        return pressList;
    }


    @Transactional
    public Press savePress(Press press, MultipartFile[] files) throws IOException {

        if (press.getStartDate() != null && press.getEndDate() != null) {
            if (press.getEndDate().isBefore(press.getStartDate())) {
                throw new IllegalArgumentException("게시 종료일은 시작일보다 이후여야 합니다.");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        if (press.getStartDate() != null && press.getStartDate().isAfter(now)) {
            press.setStatus("예약");
        } else if (press.getEndDate() != null && press.getEndDate().isBefore(now)) {
            press.setStatus("게시 종료");
        } else {
            press.setStatus("게시 중");
        }

        Press savedPress = pressRepository.save(press);

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

                    PressAttachment attachment = new PressAttachment();
                    attachment.setPressId(savedPress.getId());
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

            savedPress.setAttachmentCount(uploadedCount);
            pressRepository.save(savedPress);
        }

        return savedPress;
    }

    @Transactional
    public void deletePress(Long id) {
        Optional<Press> press = pressRepository.findById(id);
        if (press.isPresent()) {
            List<PressAttachment> attachments = attachmentRepository.findByPressId(id);
            for (PressAttachment attachment : attachments) {
                deleteFile(attachment.getFilePath());
            }
            attachmentRepository.deleteByPressId(id);
            pressRepository.deleteById(id);
        }
    }

    @Transactional
    public void increaseViewCount(Long id) {
        Optional<Press> press = pressRepository.findById(id);
        if (press.isPresent()) {
            Press p = press.get();
            p.setViewCount(p.getViewCount() + 1);
            pressRepository.save(p);
        }
    }

    public List<PressAttachment> getAttachments(Long pressId) {
        return attachmentRepository.findByPressId(pressId);
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