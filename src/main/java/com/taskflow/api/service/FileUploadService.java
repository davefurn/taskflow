package com.taskflow.api.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.taskflow.api.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final Cloudinary cloudinary;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024; // 2MB

    private static final List<String> ALLOWED_ATTACHMENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain", "text/csv"
    );

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    //  Task attachments

    public UploadResult uploadAttachment(MultipartFile file) {
        validateFile(file, MAX_FILE_SIZE, ALLOWED_ATTACHMENT_TYPES);
        return upload(file, "taskflow/attachments");
    }

    //  User avatars

    public UploadResult uploadAvatar(MultipartFile file) {
        validateFile(file, MAX_AVATAR_SIZE, ALLOWED_IMAGE_TYPES);
        return upload(file, "taskflow/avatars");
    }

    //  Core upload ─

    private UploadResult upload(MultipartFile file, String folder) {
        try {
            String publicId = folder + "/" + UUID.randomUUID();

            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id",       publicId,
                            "resource_type",   "auto",
                            "use_filename",    true,
                            "unique_filename", true
                    )
            );

            String url      = (String) result.get("secure_url");
            String pubId    = (String) result.get("public_id");
            long   bytes    = ((Number) result.get("bytes")).longValue();
            String format   = (String) result.get("format");

            log.info("Uploaded to Cloudinary: {} ({} bytes)", url, bytes);

            return new UploadResult(url, pubId, bytes,
                    file.getOriginalFilename(), file.getContentType());

        } catch (IOException e) {
            log.error("Cloudinary upload failed: {}", e.getMessage());
            throw new BadRequestException("File upload failed. Please try again.");
        }
    }

    //  Delete

    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Deleted from Cloudinary: {}", publicId);
        } catch (IOException e) {
            log.warn("Could not delete from Cloudinary: {}", e.getMessage());
        }
    }

    //  Validation

    private void validateFile(MultipartFile file, long maxSize,
                              List<String> allowedTypes) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file provided.");
        }
        if (file.getSize() > maxSize) {
            throw new BadRequestException(
                    "File too large. Maximum size is " + (maxSize / 1024 / 1024) + "MB.");
        }
        if (!allowedTypes.contains(file.getContentType())) {
            throw new BadRequestException(
                    "File type not allowed: " + file.getContentType());
        }
    }

    //  Result DTO

    public record UploadResult(
            String url,
            String publicId,
            long fileSize,
            String fileName,
            String mimeType
    ) {}
}