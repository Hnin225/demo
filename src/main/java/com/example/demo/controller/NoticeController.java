package com.example.demo.controller;

import com.example.demo.entity.Notice;
import com.example.demo.entity.NoticeAttachment;
import com.example.demo.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    // 공지사항 목록 페이지
    @GetMapping("/list")
    public String listPage(
            @RequestParam(value = "searchType", required = false, defaultValue = "전체") String searchType,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false, defaultValue = "전체") String status,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            Model model) {

        try {
            List<Notice> notices = noticeService.searchNoticesAdvanced(
                    searchType, keyword, category, status, startDate, endDate
            );

            model.addAttribute("notices", notices);
            model.addAttribute("searchType", searchType);
            model.addAttribute("keyword", keyword);
            model.addAttribute("category", category);
            model.addAttribute("status", status);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);

            return "notice/list";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "데이터를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "notice/list";
        }
    }

    // 공지사항 작성 페이지
    @GetMapping("/write")
    public String writePage() {
        return "notice/write";
    }

    // 공지사항 수정 페이지
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        try {
            Notice notice = noticeService.getNoticeById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid notice Id:" + id));
            List<NoticeAttachment> attachments = noticeService.getAttachments(id);

            model.addAttribute("notice", notice);
            model.addAttribute("attachments", attachments);
            return "notice/edit";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/notice/list";
        }
    }

    // 공지사항 상세 페이지
    @GetMapping("/detail/{id}")
    public String detailPage(@PathVariable Long id, Model model) {
        try {
            Notice notice = noticeService.getNoticeById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid notice Id:" + id));
            List<NoticeAttachment> attachments = noticeService.getAttachments(id);

            // 조회수 증가
            noticeService.increaseViewCount(id);

            model.addAttribute("notice", notice);
            model.addAttribute("attachments", attachments);
            return "notice/detail";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/notice/list";
        }
    }

    // 파일 다운로드
    @GetMapping("/download/{attachmentId}")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(@PathVariable Long attachmentId) {
        try {
            // 첨부파일 정보 조회 로직 필요
            // 간단하게 구현
            Path file = Paths.get("uploads/notices").resolve("filename");
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("파일을 읽을 수 없습니다.");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("파일을 읽을 수 없습니다.", e);
        }
    }

    // REST API - 공지사항 저장
    @PostMapping("/api/save")
    @ResponseBody
    public ResponseEntity<?> saveNotice(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "pinned", defaultValue = "false") Boolean pinned,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {

        try {
            Notice notice;
            if (id != null) {
                notice = noticeService.getNoticeById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid notice Id:" + id));
            } else {
                notice = new Notice();
            }

            notice.setTitle(title);
            notice.setContent(content);
            notice.setCategory(category);
            notice.setAuthor(author);
            notice.setPinned(pinned);

            // 날짜 파싱
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            if (startDateStr != null && !startDateStr.isEmpty()) {
                notice.setStartDate(LocalDateTime.parse(startDateStr, formatter));
            }
            if (endDateStr != null && !endDateStr.isEmpty()) {
                notice.setEndDate(LocalDateTime.parse(endDateStr, formatter));
            }

            Notice savedNotice = noticeService.saveNotice(notice, files);

            return ResponseEntity.ok(savedNotice);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // REST API - 공지사항 삭제
    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteNotice(@PathVariable Long id) {
        try {
            noticeService.deleteNotice(id);
            return ResponseEntity.ok("삭제되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}