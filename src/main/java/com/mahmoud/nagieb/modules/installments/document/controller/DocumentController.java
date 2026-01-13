package com.mahmoud.nagieb.modules.installments.document.controller;

import com.mahmoud.nagieb.modules.installments.document.dto.DocumentRequest;
import com.mahmoud.nagieb.modules.installments.document.dto.DocumentResponse;
import com.mahmoud.nagieb.modules.installments.document.dto.DocumentSummary;
import com.mahmoud.nagieb.modules.installments.document.enums.EntityType;
import com.mahmoud.nagieb.modules.installments.document.enums.FileType;
import com.mahmoud.nagieb.modules.installments.document.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Document management.
 * Provides endpoints for uploading, retrieving, and managing documents
 * attached to various entities (customers, vendors, purchases, contracts, payments).
 *
 * @author Mahmoud
 */
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Creates a new document with metadata (file URL reference).
     */
    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(@Valid @RequestBody DocumentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.create(request));
    }

    /**
     * Uploads a document with file content.
     */
    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam EntityType entityType,
            @RequestParam Long entityId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String description) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.uploadDocument(entityType, entityId, file, description));
    }

    /**
     * Updates an existing document metadata.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponse> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody DocumentRequest request) {
        return ResponseEntity.ok(documentService.update(id, request));
    }

    /**
     * Gets document by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocumentById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getById(id));
    }

    /**
     * Downloads document file content.
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        DocumentResponse document = documentService.getById(id);
        byte[] fileContent = documentService.getFileContent(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(documentService.determineMediaType(document.getFileType()));

        headers.setContentDispositionFormData("attachment", documentService.extractFilename(document.getFileUrl()));
        headers.setContentLength(fileContent.length);

        return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
    }

    /**
     * Lists all documents with pagination and optional search.
     */
    @GetMapping
    public ResponseEntity<Page<DocumentSummary>> listDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(documentService.list(page, size, search));
    }

    /**
     * Lists documents for a specific entity.
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<Page<DocumentSummary>> listByEntity(
            @PathVariable EntityType entityType,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(documentService.listByEntity(entityType, entityId, page, size));
    }

    /**
     * Gets all documents for a specific entity (no pagination).
     */
    @GetMapping("/entity/{entityType}/{entityId}/all")
    public ResponseEntity<List<DocumentResponse>> getAllByEntity(
            @PathVariable EntityType entityType,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(documentService.getAllByEntity(entityType, entityId));
    }

    /**
     * Lists documents by entity type.
     */
    @GetMapping("/by-entity-type/{entityType}")
    public ResponseEntity<Page<DocumentSummary>> listByEntityType(
            @PathVariable EntityType entityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(documentService.listByEntityType(entityType, page, size));
    }

    /**
     * Lists documents by file type.
     */
    @GetMapping("/by-file-type/{fileType}")
    public ResponseEntity<Page<DocumentSummary>> listByFileType(
            @PathVariable FileType fileType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(documentService.listByFileType(fileType, page, size));
    }

    /**
     * Deletes a document by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDocument(@PathVariable Long id) {
        String message = documentService.delete(id);
        return ResponseEntity.ok(message);
    }

    /**
     * Deletes all documents for a specific entity.
     */
    @DeleteMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<Void> deleteAllByEntity(
            @PathVariable EntityType entityType,
            @PathVariable Long entityId) {
        documentService.deleteAllByEntity(entityType, entityId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets document count for a specific entity.
     */
    @GetMapping("/entity/{entityType}/{entityId}/count")
    public ResponseEntity<Map<String, Long>> countByEntity(
            @PathVariable EntityType entityType,
            @PathVariable Long entityId) {
        long count = documentService.countByEntity(entityType, entityId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Gets document statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getDocumentStats() {
        return ResponseEntity.ok(documentService.getDocumentStats());
    }


}

