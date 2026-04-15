package com.hits.language_school_back.controller;

import com.hits.language_school_back.dto.AttachmentDownloadInfo;
import com.hits.language_school_back.service.AttachmentService;
import com.hits.language_school_back.service.MinioService;
import com.hits.language_school_back.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/attachments")
@RequiredArgsConstructor
public class AttachmentController {
    private final AttachmentService attachmentService;
    private final UserService userService;
    private final MinioService minioService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadAttachment(
            @RequestParam("taskId") UUID taskId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        attachmentService.uploadAttachment(taskId, file, userService.getMe(request).getId());
    }

    @PostMapping(value = "/to-notification", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadAttachmentForNotification(
            @RequestParam("taskId") UUID notificationId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        attachmentService.uploadAttachmentForNotification(notificationId, file, userService.getMe(request).getId());
    }

    @PostMapping(value = "/to-participation", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadAttachmentForParticipation(
            @RequestParam("participationId") UUID participationId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        attachmentService.uploadAttachmentForParticipation(participationId, file, userService.getMe(request).getId());
    }

    @DeleteMapping("/{attachmentId}")
    public void deleteAttachment(@PathVariable UUID attachmentId) {
        attachmentService.deleteAttachment(attachmentId);
    }

    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable UUID attachmentId) {
        try {
            AttachmentDownloadInfo info = attachmentService.getDownloadInfo(attachmentId);
            InputStream fileStream = attachmentService.downloadAttachment(attachmentId);
            InputStreamResource resource = new InputStreamResource(fileStream);

            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            if (info.getFileType() != null) {
                try {
                    mediaType = MediaType.parseMediaType(info.getFileType());
                } catch (Exception ignored) {
                }
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(info.getFileSize())
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + info.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading attachment {}: {}", attachmentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
