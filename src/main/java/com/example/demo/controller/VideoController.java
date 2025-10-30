package com.example.demo.controller;

import com.example.demo.entity.Video;
import com.example.demo.entity.VideoAttachment;
import com.example.demo.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @GetMapping("/list")
    public String listPage(
            @RequestParam(value = "searchType", required = false, defaultValue = "전체") String searchType,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false, defaultValue = "전체") String status,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            Model model) {

        try {
            List<Video> videoList = videoService.searchVideosAdvanced(
                    searchType, keyword, status, startDate, endDate
            );

            model.addAttribute("videoList", videoList);
            model.addAttribute("searchType", searchType);
            model.addAttribute("keyword", keyword);
            model.addAttribute("status", status);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);

            return "video/list";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "데이터를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "video/list";
        }
    }

    @GetMapping("/write")
    public String writePage() {
        return "video/write";
    }

    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        try {
            Video video = videoService.getVideoById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid video Id:" + id));
            List<VideoAttachment> attachments = videoService.getAttachments(id);

            model.addAttribute("video", video);
            model.addAttribute("attachments", attachments);
            return "video/edit";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/video/list";
        }
    }

    @GetMapping("/detail/{id}")
    public String detailPage(@PathVariable Long id, Model model) {
        try {
            Video video = videoService.getVideoById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid video Id:" + id));
            List<VideoAttachment> attachments = videoService.getAttachments(id);

            videoService.increaseViewCount(id);

            model.addAttribute("video", video);
            model.addAttribute("attachments", attachments);
            return "video/detail";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/video/list";
        }
    }

    @PostMapping("/api/save")
    @ResponseBody
    public ResponseEntity<?> saveVideo(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "pinned", defaultValue = "false") Boolean pinned,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {

        try {
            Video video;
            if (id != null) {
                video = videoService.getVideoById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid video Id:" + id));
            } else {
                video = new Video();
            }

            video.setTitle(title);
            video.setContent(content);
            video.setAuthor(author);
            video.setPinned(pinned);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            if (startDateStr != null && !startDateStr.isEmpty()) {
                video.setStartDate(LocalDateTime.parse(startDateStr, formatter));
            }
            if (endDateStr != null && !endDateStr.isEmpty()) {
                video.setEndDate(LocalDateTime.parse(endDateStr, formatter));
            }

            Video savedVideo = videoService.saveVideo(video, files);
            return ResponseEntity.ok(savedVideo);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteVideo(@PathVariable Long id) {
        try {
            videoService.deleteVideo(id);
            return ResponseEntity.ok("삭제되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}