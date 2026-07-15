package com.hoang.backend.common.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MinioService {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final MinioClient minioClient;
    private final String bucket;
    private final String publicUrl;

    public MinioService(MinioClient minioClient,
                        @Value("${minio.bucket}") String bucket,
                        @Value("${minio.public-url:}") String publicUrl) {
        this.minioClient = minioClient;
        this.bucket = bucket;
        this.publicUrl = publicUrl;
        initBucket();
    }

    private void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                setPublicReadPolicy();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MinIO bucket: " + bucket, e);
        }
    }

    private void setPublicReadPolicy() throws Exception {
        String policy = """
                {
                    "Version": "2012-10-17",
                    "Statement": [{
                        "Effect": "Allow",
                        "Principal": {"AWS": ["*"]},
                        "Action": ["s3:GetObject"],
                        "Resource": ["arn:aws:s3:::%s/*"]
                    }]
                }
                """.formatted(bucket);
        minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder().bucket(bucket).config(policy).build()
        );
    }

    public String upload(String objectName, MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(is, file.getSize(), -1)
                    .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                    .build();
            minioClient.putObject(args);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Failed to upload to MinIO: " + objectName, e);
        }
        if (!publicUrl.isBlank()) {
            return publicUrl + "/" + bucket + "/" + objectName;
        }
        return "/" + objectName;
    }

    public void delete(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete from MinIO: " + objectName, e);
        }
    }

    public String storeImage(String folder, String subFolder, MultipartFile imageFile) throws IOException {
        String originalName = imageFile.getOriginalFilename() == null ? "image" : imageFile.getOriginalFilename();
        String extension = extensionOf(originalName);
        String baseName = baseNameOf(originalName);

        String safeFolder = sanitizePathPart(folder);
        String safeSubFolder = sanitizePathPart(subFolder);
        String filename = LocalDateTime.now().format(TIMESTAMP_FORMATTER) + "_" + sanitizePathPart(baseName);

        String objectName = safeFolder + "/" + safeSubFolder + "/" + filename + extension;
        return upload(objectName, imageFile);
    }

    public String storeAvatar(Long userId, MultipartFile file) throws IOException {
        String safeFileName = file.getOriginalFilename() == null ? "avatar" : file.getOriginalFilename();
        safeFileName = safeFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String objectName = "avatars/" + userId + "/" + safeFileName;
        return upload(objectName, file);
    }

    public static String extensionOf(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

    public static String baseNameOf(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex <= 0) {
            return filename;
        }
        return filename.substring(0, dotIndex);
    }

    public static String sanitizePathPart(String input) {
        String value = String.valueOf(input).replaceAll("[^a-zA-Z0-9._-]", "_");
        if (value.isBlank()) {
            return "item";
        }
        return value;
    }
}
