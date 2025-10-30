package com.example.demo.service;

import com.example.demo.entity.Award;
import com.example.demo.repository.AwardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class AwardService {

    @Autowired
    private AwardRepository awardRepository;

    private final String uploadDir = "uploads/awards/";

    // 허용된 확장자
    private final String[] allowedExtensions = {"jpg", "jpeg", "png", "svg"};

    // 허용된 특수문자 (32개)
    private final String allowedSpecialChars = "!@#$%^&*()_+-=[]{}|;:',.<>?/~`\" ";

    public List<Award> getAllAwards() {
        return awardRepository.findAllByOrderByYearDesc();
    }

    public Optional<Award> getAwardById(Long id) {
        return awardRepository.findById(id);
    }

    @Transactional
    public Award saveAward(Award award, MultipartFile imageFile) throws IOException {

        // 제목 유효성 검사
        if (award.getTitle() == null || award.getTitle().length() < 10 || award.getTitle().length() > 50) {
            throw new IllegalArgumentException("제목은 10-50자 사이여야 합니다.");
        }

        // 제목 특수문자 검증
        if (!isValidTitle(award.getTitle())) {
            throw new IllegalArgumentException("제목에 허용되지 않은 특수문자가 포함되어 있습니다.");
        }

        // 이미지 파일 처리
        if (imageFile != null && !imageFile.isEmpty()) {

            // 파일 크기 검증 (20MB)
            if (imageFile.getSize() > 20 * 1024 * 1024) {
                throw new IllegalArgumentException("파일 크기는 20MB를 초과할 수 없습니다.");
            }

            // 파일명 검증 (공백, 특수문자 제외)
            String originalFileName = imageFile.getOriginalFilename();
            if (originalFileName != null && !isValidFileName(originalFileName)) {
                throw new IllegalArgumentException("파일명에 공백이나 특수문자가 포함될 수 없습니다.");
            }

            // 확장자 검증
            String extension = getFileExtension(originalFileName);
            if (!isAllowedExtension(extension)) {
                throw new IllegalArgumentException("허용되지 않은 파일 형식입니다. (jpg, jpeg, png, svg만 가능)");
            }

            String fileName = saveFile(imageFile);
            award.setImageFileName(originalFileName);
            award.setImageFilePath(uploadDir + fileName);
        }

        return awardRepository.save(award);
    }

    // 제목 유효성 검사 (영문, 숫자, 한글, 허용된 특수문자만)
    private boolean isValidTitle(String title) {
        for (char c : title.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && allowedSpecialChars.indexOf(c) == -1) {
                return false;
            }
        }
        return true;
    }

    // 파일명 유효성 검사 (영문, 숫자, ., - 만 허용)
    private boolean isValidFileName(String fileName) {
        return fileName.matches("^[a-zA-Z0-9._-]+$");
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
        for (String allowed : allowedExtensions) {
            if (allowed.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
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

    @Transactional
    public void deleteAward(Long id) {
        Optional<Award> award = awardRepository.findById(id);
        if (award.isPresent()) {
            Award awardEntity = award.get();
            deleteFile(awardEntity.getImageFilePath());
            awardRepository.deleteById(id);
        }
    }

    @Transactional
    public void toggleVisibility(Long id) {
        Optional<Award> award = awardRepository.findById(id);
        if (award.isPresent()) {
            Award awardEntity = award.get();
            awardEntity.setIsVisible(!awardEntity.getIsVisible());
            awardRepository.save(awardEntity);
        }
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