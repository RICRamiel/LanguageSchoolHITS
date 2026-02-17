package com.hits.language_school_back.controller;

import com.hits.language_school_back.inteface.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/attachments")
@RequiredArgsConstructor
public class AttachmentController {
    private final AttachmentService attachmentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadAttachment(
            @RequestParam("taskId") Long taskId,
            @RequestParam("file") MultipartFile file) {
        attachmentService.uploadAttachment(taskId, file);
    }

    @DeleteMapping("/{attachmentId}")
    public void deleteAttachment(@PathVariable Long attachmentId) {
        attachmentService.deleteAttachment(attachmentId);
    }

    @GetMapping("/{attachmentId}/download-link")
    public String getDownloadLink(@PathVariable Long attachmentId) {
        return attachmentService.getDownloadLink(attachmentId);
    }
}