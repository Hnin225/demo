package com.example.demo.service;

import com.example.demo.entity.FileAttachment;
import com.example.demo.entity.Notice;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private final String uploadDir = "C:/uploads/";  // 파일 저장 경로

    public FileService() {
        // 업로드 폴더 생성
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // 파일 저장
    public List<FileAttachment> saveFiles(List<MultipartFile> files, Notice notice) throws IOException {
        List<FileAttachment> attachments = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                // 고유한 파일명 생성
                String originalFileName = file.getOriginalFilename();
                String savedFileName = UUID.randomUUID().toString() + "_" + originalFileName;
                String filePath = uploadDir + savedFileName;

                // 파일 저장
                Path path = Paths.get(filePath);
                Files.write(path, file.getBytes());

                // 파일 정보 저장
                FileAttachment attachment = new FileAttachment();
                attachment.setOriginalFileName(originalFileName);
                attachment.setSavedFileName(savedFileName);
                attachment.setFilePath(filePath);
                attachment.setFileSize(file.getSize());
                attachment.setNotice(notice);

                attachments.add(attachment);
            }
        }

        return attachments;


    }


}