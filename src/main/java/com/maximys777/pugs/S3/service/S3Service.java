package com.maximys777.pugs.S3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.maximys777.pugs.exception.exceptions.BadRequestException;
import com.maximys777.pugs.exception.exceptions.IllegalArgumentException;
import com.maximys777.pugs.exception.exceptions.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {
    private final AmazonS3 s3Client;

    @Value("${aws.bucket.name}")
    private String bucketName;

    public String uploadFile(MultipartFile file) {
        validateFile(file);

        String extension = getFileExtension(file.getOriginalFilename());
        String key = UUID.randomUUID() + extension;

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());
        objectMetadata.setContentType(file.getContentType());

        try {
            PutObjectRequest request = new PutObjectRequest(bucketName, key, file.getInputStream(), objectMetadata);

            s3Client.putObject(request);

            return s3Client.getUrl(bucketName, key).toString();
        } catch (IOException e) {
            log.error("Error while reading file", e);
            throw new RuntimeException("Error while reading file" + e.getMessage());
        } catch (Exception e) {
            log.error("S3 fails {}", e.getMessage());
            throw new ServiceUnavailableException("Error while uploading file");
        }
    }

    public void deleteFile(String fileUrl) {
        String key = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        s3Client.deleteObject(bucketName, key);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File is too large. Maximum size 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File is invalid");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            throw new BadRequestException("File extension is invalid");
        }
        return fileName.substring(fileName.lastIndexOf('.'));
    }
}