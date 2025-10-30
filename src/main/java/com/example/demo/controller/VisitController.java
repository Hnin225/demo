package com.example.demo.controller;

import com.example.demo.entity.Visit;
import com.example.demo.entity.VisitAttachment;
import com.example.demo.service.VisitService;
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
@RequestMapping("/visit")
public class VisitController {

    @Autowired
    private VisitService visitService;

    @GetMapping("/list")
    public String listPage(
            @RequestParam(value = "searchType", required = false, defaultValue = "전체") String searchType,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false, defaultValue = "전체") String status,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            Model model) {

        try {
            List<Visit> visitList = visitService.searchVisitsAdvanced(
                    searchType, keyword, status, startDate, endDate
            );

            model.addAttribute("visitList", visitList);
            model.addAttribute("searchType", searchType);
            model.addAttribute("keyword", keyword);
            model.addAttribute("status", status);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);

            return "visit/list";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "데이터를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "visit/list";
        }
    }

    @GetMapping("/write")
    public String writePage() {
        return "visit/write";
    }

    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        try {
            Visit visit = visitService.getVisitById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid visit Id:" + id));
            List<VisitAttachment> attachments = visitService.getAttachments(id);

            model.addAttribute("visit", visit);
            model.addAttribute("attachments", attachments);
            return "visit/edit";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/visit/list";
        }
    }

    @GetMapping("/detail/{id}")
    public String detailPage(@PathVariable Long id, Model model) {
        try {
            Visit visit = visitService.getVisitById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid visit Id:" + id));
            List<VisitAttachment> attachments = visitService.getAttachments(id);

            visitService.increaseViewCount(id);

            model.addAttribute("visit", visit);
            model.addAttribute("attachments", attachments);
            return "visit/detail";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/visit/list";
        }
    }

    @PostMapping("/api/save")
    @ResponseBody
    public ResponseEntity<?> saveVisit(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "pinned", defaultValue = "false") Boolean pinned,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {

        try {
            Visit visit;
            if (id != null) {
                visit = visitService.getVisitById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid visit Id:" + id));
            } else {
                visit = new Visit();
            }

            visit.setTitle(title);
            visit.setContent(content);
            visit.setAuthor(author);
            visit.setPinned(pinned);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            if (startDateStr != null && !startDateStr.isEmpty()) {
                visit.setStartDate(LocalDateTime.parse(startDateStr, formatter));
            }
            if (endDateStr != null && !endDateStr.isEmpty()) {
                visit.setEndDate(LocalDateTime.parse(endDateStr, formatter));
            }

            Visit savedVisit = visitService.saveVisit(visit, files);
            return ResponseEntity.ok(savedVisit);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteVisit(@PathVariable Long id) {
        try {
            visitService.deleteVisit(id);
            return ResponseEntity.ok("삭제되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}