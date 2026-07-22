package com.splitter.controller;

import com.splitter.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/groups/{groupId}/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/csv")
    public ResponseEntity<byte[]> csv(@PathVariable Long groupId) {
        byte[] body = exportService.exportCsv(groupId).getBytes(StandardCharsets.UTF_8);
        return fileResponse(body, "expenses.csv", "text/csv");
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long groupId) {
        byte[] body = exportService.exportPdf(groupId);
        return fileResponse(body, "expenses.pdf", "application/pdf");
    }

    private ResponseEntity<byte[]> fileResponse(byte[] body, String filename, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        headers.setContentType(MediaType.parseMediaType(contentType));
        return new ResponseEntity<>(body, headers, org.springframework.http.HttpStatus.OK);
    }
}

