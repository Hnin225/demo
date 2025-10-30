package com.example.demo.service;

import com.example.demo.entity.Notice;
import com.example.demo.entity.NoticeAttachment;
import com.example.demo.repository.NoticeAttachmentRepository;
import com.example.demo.repository.NoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Service
public class NoticeService {

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private NoticeAttachmentRepository attachmentRepository;

    private final String uploadDir = "uploads/notices/";

    // 허용된 확장자
    private final List<String> allowedExtensions = Arrays.asList(
            "jpg", "jpeg", "png", "svg", "gif",
            "hwp", "doc", "docx", "pdf", "ppt", "pptx", "txt", "xls", "xlsx",
            "gz", "zip"
    );

    private final int MAX_FILES = 5;
    private final long MAX_TOTAL_SIZE = 90 * 1024 * 1024;

    // 전체 목록 조회
    public List<Notice> getAllNotices() {
        return noticeRepository.findAllOrderByPinnedAndCreatedAt();
    }

    // ID로 조회
    public Optional<Notice> getNoticeById(Long id) {
        return noticeRepository.findById(id);
    }

    // 검색 메서드
    public List<Notice> searchNotices(String searchType, String keyword, String status) {
        List<Notice> notices;

        // 상태 필터링
        if (status != null && !status.equals("전체")) {
            notices = noticeRepository.findByStatusOrderByPinnedDescCreatedAtDesc(status);

            // 키워드 필터링
            if (keyword != null && !keyword.trim().isEmpty()) {
                String finalKeyword = keyword.trim();
                notices = notices.stream()
                        .filter(n -> {
                            if ("제목".equals(searchType)) {
                                return n.getTitle().contains(finalKeyword);
                            } else if ("작성자".equals(searchType)) {
                                return n.getAuthor() != null && n.getAuthor().contains(finalKeyword);
                            } else {
                                return n.getTitle().contains(finalKeyword) ||
                                        (n.getAuthor() != null && n.getAuthor().contains(finalKeyword));
                            }
                        })
                        .toList();
            }
        } else {
            // 키워드로만 검색
            if (keyword == null || keyword.trim().isEmpty()) {
                notices = noticeRepository.findAllOrderByPinnedAndCreatedAt();
            } else {
                String finalKeyword = keyword.trim();
                if ("제목".equals(searchType)) {
                    notices = noticeRepository.findByTitleContaining(finalKeyword);
                } else if ("작성자".equals(searchType)) {
                    notices = noticeRepository.findByAuthorContaining(finalKeyword);
                } else {
                    notices = noticeRepository.findByKeyword(finalKeyword);
                }
            }
        }

        return notices;
    }

    // 공지사항 저장
    @Transactional
    public Notice saveNotice(Notice notice, MultipartFile[] files) throws IOException {

        // 게시 기간 유효성 검사
        if (notice.getStartDate() != null && notice.getEndDate() != null) {
            if (notice.getEndDate().isBefore(notice.getStartDate())) {
                throw new IllegalArgumentException("게시 종료일은 시작일보다 이후여야 합니다.");
            }
        }

        // 게시 상태 자동 설정
        LocalDateTime now = LocalDateTime.now();
        if (notice.getStartDate() != null && notice.getStartDate().isAfter(now)) {
            notice.setStatus("예약");
        } else if (notice.getEndDate() != null && notice.getEndDate().isBefore(now)) {
            notice.setStatus("게시 종료");
        } else {
            notice.setStatus("게시 중");
        }

        // 공지사항 저장
        Notice savedNotice = noticeRepository.save(notice);

        // 파일 업로드 처리
        if (files != null && files.length > 0) {

            // 파일 개수 검증
            if (files.length > MAX_FILES) {
                throw new IllegalArgumentException("최대 5개까지 업로드 가능합니다.");
            }

            // 총 용량 검증
            long totalSize = Arrays.stream(files)
                    .mapToLong(MultipartFile::getSize)
                    .sum();

            if (totalSize > MAX_TOTAL_SIZE) {
                throw new IllegalArgumentException("총 90MB의 파일을 업로드 할 수 없습니다.");
            }

            int uploadedCount = 0;
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileName = file.getOriginalFilename();

                    // 확장자 검증
                    String extension = getFileExtension(fileName);
                    if (!isAllowedExtension(extension)) {
                        throw new IllegalArgumentException(
                                "허용되지 않은 파일 형식입니다: " + fileName
                        );
                    }

                    String savedFileName = saveFile(file);

                    NoticeAttachment attachment = new NoticeAttachment();
                    attachment.setNoticeId(savedNotice.getId());
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

            savedNotice.setAttachmentCount(uploadedCount);
            noticeRepository.save(savedNotice);
        }

        return savedNotice;
    }

    // 공지사항 삭제
    @Transactional
    public void deleteNotice(Long id) {
        Optional<Notice> notice = noticeRepository.findById(id);
        if (notice.isPresent()) {
            // 첨부파일 삭제
            List<NoticeAttachment> attachments = attachmentRepository.findByNoticeId(id);
            for (NoticeAttachment attachment : attachments) {
                deleteFile(attachment.getFilePath());
            }
            attachmentRepository.deleteByNoticeId(id);

            // 공지사항 삭제
            noticeRepository.deleteById(id);
        }
    }

    // 조회수 증가
    @Transactional
    public void increaseViewCount(Long id) {
        Optional<Notice> notice = noticeRepository.findById(id);
        if (notice.isPresent()) {
            Notice n = notice.get();
            n.setViewCount(n.getViewCount() + 1);
            noticeRepository.save(n);
        }
    }

    // 첨부파일 조회
    public List<NoticeAttachment> getAttachments(Long noticeId) {
        return attachmentRepository.findByNoticeId(noticeId);
    }

    // 파일 확장자 추출
    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) return "";
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    // 확장자 검증
    private boolean isAllowedExtension(String extension) {
        return allowedExtensions.contains(extension.toLowerCase());
    }

    // 이미지 파일 여부
    private boolean isImageFile(String extension) {
        return Arrays.asList("jpg", "jpeg", "png", "svg", "gif").contains(extension.toLowerCase());
    }

    // 파일 저장
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

    // 파일 삭제
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

    // 고급 검색
    public List<Notice> searchNoticesAdvanced(String searchType, String keyword,
                                              String category, String status,
                                              String startDate, String endDate) {
        List<Notice> notices = noticeRepository.findAllOrderByPinnedAndCreatedAt();

        // 키워드 필터
        if (keyword != null && !keyword.trim().isEmpty()) {
            String finalKeyword = keyword.trim();
            notices = notices.stream()
                    .filter(n -> {
                        if ("제목".equals(searchType)) {
                            return n.getTitle().contains(finalKeyword);
                        } else if ("작성자".equals(searchType)) {
                            return n.getAuthor() != null && n.getAuthor().contains(finalKeyword);
                        } else {
                            return n.getTitle().contains(finalKeyword) ||
                                    (n.getAuthor() != null && n.getAuthor().contains(finalKeyword));
                        }
                    })
                    .toList();
        }

        // 분류 필터
        if (category != null && !category.equals("전체")) {
            String finalCategory = category;
            notices = notices.stream()
                    .filter(n -> finalCategory.equals(n.getCategory()))
                    .toList();
        }

        // 상태 필터
        if (status != null && !status.equals("전체")) {
            String finalStatus = status;
            notices = notices.stream()
                    .filter(n -> finalStatus.equals(n.getStatus()))
                    .toList();
        }

        // 날짜 필터
        if (startDate != null && !startDate.trim().isEmpty()) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            notices = notices.stream()
                    .filter(n -> n.getCreatedAt().isAfter(start) || n.getCreatedAt().isEqual(start))
                    .toList();
        }

        if (endDate != null && !endDate.trim().isEmpty()) {
            LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
            notices = notices.stream()
                    .filter(n -> n.getCreatedAt().isBefore(end) || n.getCreatedAt().isEqual(end))
                    .toList();
        }

        return notices;
    }
}