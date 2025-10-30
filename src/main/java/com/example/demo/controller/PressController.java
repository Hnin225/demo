package com.example.demo.controller;

import com.example.demo.entity.Press;
import com.example.demo.entity.PressAttachment;
import com.example.demo.service.PressService;
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
@RequestMapping("/press")
public class PressController {

    @Autowired
    private PressService pressService;

    @GetMapping("/list")
    public String listPage(
            @RequestParam(value = "searchType", required = false, defaultValue = "전체") String searchType,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false, defaultValue = "전체") String status,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            Model model) {

        try {
            List<Press> pressList = pressService.searchPressAdvanced(
                    searchType, keyword, status, startDate, endDate
            );

            model.addAttribute("pressList", pressList);
            model.addAttribute("searchType", searchType);
            model.addAttribute("keyword", keyword);
            model.addAttribute("status", status);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);

            return "press/list";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "데이터를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "press/list";
        }
    }

    @GetMapping("/write")
    public String writePage() {
        return "press/write";
    }

    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        try {
            Press press = pressService.getPressById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid press Id:" + id));
            List<PressAttachment> attachments = pressService.getAttachments(id);

            model.addAttribute("press", press);
            model.addAttribute("attachments", attachments);
            return "press/edit";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/press/list";
        }
    }

    @GetMapping("/detail/{id}")
    public String detailPage(@PathVariable Long id, Model model) {
        try {
            Press press = pressService.getPressById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid press Id:" + id));
            List<PressAttachment> attachments = pressService.getAttachments(id);

            pressService.increaseViewCount(id);

            model.addAttribute("press", press);
            model.addAttribute("attachments", attachments);
            return "press/detail";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/press/list";
        }
    }

    @PostMapping("/api/save")
    @ResponseBody
    public ResponseEntity<?> savePress(
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
            Press press;
            if (id != null) {
                press = pressService.getPressById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid press Id:" + id));
            } else {
                press = new Press();
            }

            press.setTitle(title);
            press.setContent(content);
            press.setCategory(category);
            press.setAuthor(author);
            press.setPinned(pinned);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            if (startDateStr != null && !startDateStr.isEmpty()) {
                press.setStartDate(LocalDateTime.parse(startDateStr, formatter));
            }
            if (endDateStr != null && !endDateStr.isEmpty()) {
                press.setEndDate(LocalDateTime.parse(endDateStr, formatter));
            }

            Press savedPress = pressService.savePress(press, files);
            return ResponseEntity.ok(savedPress);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deletePress(@PathVariable Long id) {
        try {
            pressService.deletePress(id);
            return ResponseEntity.ok("삭제되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}