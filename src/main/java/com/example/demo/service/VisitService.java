package com.example.demo.service;

import com.example.demo.entity.Visit;
import com.example.demo.entity.VisitAttachment;
import com.example.demo.repository.VisitAttachmentRepository;
import com.example.demo.repository.VisitRepository;
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
public class VisitService {

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private VisitAttachmentRepository attachmentRepository;

    private final String uploadDir = "uploads/visit/";

    private final List<String> allowedExtensions = Arrays.asList(
            "jpg", "jpeg", "png", "svg", "gif",
            "hwp", "doc", "docx", "pdf", "ppt", "pptx", "txt", "xls", "xlsx",
            "gz", "zip"
    );

    private final int MAX_FILES = 5;
    private final long MAX_TOTAL_SIZE = 90 * 1024 * 1024;

    public List<Visit> getAllVisits() {
        return visitRepository.findAllOrderByPinnedAndCreatedAt();
    }

    public Optional<Visit> getVisitById(Long id) {
        return visitRepository.findById(id);
    }

    public List<Visit> searchVisitsAdvanced(String searchType, String keyword,
                                            String status,
                                            String startDate, String endDate) {
        List<Visit> visits = visitRepository.findAllOrderByPinnedAndCreatedAt();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String finalKeyword = keyword.trim();
            visits = visits.stream()
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
            visits = visits.stream()
                    .filter(v -> finalStatus.equals(v.getStatus()))
                    .toList();
        }

        if (startDate != null && !startDate.trim().isEmpty()) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            visits = visits.stream()
                    .filter(v -> v.getCreatedAt().isAfter(start) || v.getCreatedAt().isEqual(start))
                    .toList();
        }

        if (endDate != null && !endDate.trim().isEmpty()) {
            LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
            visits = visits.stream()
                    .filter(v -> v.getCreatedAt().isBefore(end) || v.getCreatedAt().isEqual(end))
                    .toList();
        }

        return visits;
    }

    @Transactional
    public Visit saveVisit(Visit visit, MultipartFile[] files) throws IOException {

        if (visit.getStartDate() != null && visit.getEndDate() != null) {
            if (visit.getEndDate().isBefore(visit.getStartDate())) {
                throw new IllegalArgumentException("게시 종료일은 시작일보다 이후여야 합니다.");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        if (visit.getStartDate() != null && visit.getStartDate().isAfter(now)) {
            visit.setStatus("예약");
        } else if (visit.getEndDate() != null && visit.getEndDate().isBefore(now)) {
            visit.setStatus("게시 종료");
        } else {
            visit.setStatus("게시 중");
        }

        Visit savedVisit = visitRepository.save(visit);

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

                    VisitAttachment attachment = new VisitAttachment();
                    attachment.setVisitId(savedVisit.getId());
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

            savedVisit.setAttachmentCount(uploadedCount);
            visitRepository.save(savedVisit);
        }

        return savedVisit;
    }

    @Transactional
    public void deleteVisit(Long id) {
        Optional<Visit> visit = visitRepository.findById(id);
        if (visit.isPresent()) {
            List<VisitAttachment> attachments = attachmentRepository.findByVisitId(id);
            for (VisitAttachment attachment : attachments) {
                deleteFile(attachment.getFilePath());
            }
            attachmentRepository.deleteByVisitId(id);
            visitRepository.deleteById(id);
        }
    }

    @Transactional
    public void increaseViewCount(Long id) {
        Optional<Visit> visit = visitRepository.findById(id);
        if (visit.isPresent()) {
            Visit v = visit.get();
            v.setViewCount(v.getViewCount() + 1);
            visitRepository.save(v);
        }
    }

    public List<VisitAttachment> getAttachments(Long visitId) {
        return attachmentRepository.findByVisitId(visitId);
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