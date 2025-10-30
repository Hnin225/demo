package com.example.demo.controller;

import com.example.demo.entity.Award;
import com.example.demo.service.AwardService;
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
import java.util.List;

@Controller
@RequestMapping("/award")
public class AwardController {

    @Autowired
    private AwardService awardService;

    // 수상 이력 목록 페이지
    @GetMapping("/list")
    public String listPage(Model model) {
        try {
            List<Award> awards = awardService.getAllAwards();
            model.addAttribute("awards", awards);
            return "award/list";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "데이터를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "award/list";
        }
    }

    // 수상 이력 등록 페이지
    @GetMapping("/write")
    public String writePage() {
        return "award/write";
    }

    // 수상 이력 수정 페이지
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        try {
            Award award = awardService.getAwardById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid award Id:" + id));
            model.addAttribute("award", award);
            return "award/edit";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/award/list";
        }
    }

    // 이미지 파일 제공
    @GetMapping("/image/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path file = Paths.get("uploads/awards").resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("파일을 읽을 수 없습니다: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("파일을 읽을 수 없습니다: " + filename, e);
        }
    }

    // REST API - 수상 이력 저장
    @PostMapping("/api/save")
    @ResponseBody
    public ResponseEntity<?> saveAward(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam("year") Integer year,
            @RequestParam("title") String title,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "isVisible", defaultValue = "true") Boolean isVisible) {

        try {
            Award award;
            if (id != null) {
                award = awardService.getAwardById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid award Id:" + id));
            } else {
                award = new Award();
            }

            award.setYear(year);
            award.setTitle(title);
            award.setAuthor(author);
            award.setIsVisible(isVisible);

            Award savedAward = awardService.saveAward(award, imageFile);
            return ResponseEntity.ok(savedAward);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // REST API - 수상 이력 삭제
    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteAward(@PathVariable Long id) {
        try {
            awardService.deleteAward(id);
            return ResponseEntity.ok("삭제되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // REST API - 게시/숨김 토글
    @PostMapping("/api/toggle-visibility/{id}")
    @ResponseBody
    public ResponseEntity<String> toggleVisibility(@PathVariable Long id) {
        try {
            awardService.toggleVisibility(id);
            return ResponseEntity.ok("상태가 변경되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("상태 변경 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}