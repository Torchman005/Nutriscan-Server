package com.luminous.nutriscan.service;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @PostConstruct
    public void init() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                System.out.println("Bucket '" + bucketName + "' created.");
            }
        } catch (Exception e) {
            System.err.println("Could not initialize Minio bucket: " + e.getMessage());
        }
    }

    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            System.out.println("Deleted file from Minio: " + fileName);
        } catch (Exception e) {
            System.err.println("Failed to delete file from Minio: " + fileName + ". Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String uploadFile(org.springframework.web.multipart.MultipartFile file) {
        try {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            return uploadFile(file.getInputStream(), file.getSize(), fileName, file.getContentType());
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to Minio: " + e.getMessage(), e);
        }
    }

    public String uploadFile(java.io.InputStream inputStream, long size, String fileName, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );
            
            // Get presigned URL for access (valid for 2 hours)
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(io.minio.http.Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry(2, java.util.concurrent.TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to Minio: " + e.getMessage(), e);
        }
    }
}
