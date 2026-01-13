package com.mahmoud.nagieb.modules.installments.document.service;

import com.mahmoud.nagieb.exception.BusinessException;
import com.mahmoud.nagieb.exception.ObjectNotFoundException;
import com.mahmoud.nagieb.modules.installments.contract.repo.ContractRepository;
import com.mahmoud.nagieb.modules.installments.customer.repo.CustomerRepository;
import com.mahmoud.nagieb.modules.installments.document.dto.DocumentRequest;
import com.mahmoud.nagieb.modules.installments.document.dto.DocumentResponse;
import com.mahmoud.nagieb.modules.installments.document.dto.DocumentSummary;
import com.mahmoud.nagieb.modules.installments.document.entity.Document;
import com.mahmoud.nagieb.modules.installments.document.enums.EntityType;
import com.mahmoud.nagieb.modules.installments.document.enums.FileType;
import com.mahmoud.nagieb.modules.installments.document.mapper.DocumentMapper;
import com.mahmoud.nagieb.modules.installments.document.repo.DocumentRepository;
import com.mahmoud.nagieb.modules.installments.payment.repo.PaymentRepository;
import com.mahmoud.nagieb.modules.installments.purchase.repo.PurchaseRepository;
import com.mahmoud.nagieb.modules.installments.vendor.repo.VendorRepository;
import com.mahmoud.nagieb.modules.shared.user.entity.User;
import com.mahmoud.nagieb.modules.shared.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing documents
 *
 * @author Mahmoud
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final VendorRepository vendorRepository;
    private final PurchaseRepository purchaseRepository;
    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;

    // Maximum file size: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // Allowed file extensions
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "bmp", "webp");
    private static final List<String> ALLOWED_PDF_EXTENSIONS = List.of("pdf");
    private static final List<String> ALLOWED_DOC_EXTENSIONS = List.of("doc", "docx", "xls", "xlsx", "txt");

    /**
     * Creates a new document with metadata only (file URL reference).
     */
    @Transactional
    public DocumentResponse create(DocumentRequest request) {
        log.info("Creating document for entity type: {} with entity ID: {}",
                request.getEntityType(), request.getEntityId());

        validateEntityExists(request.getEntityType(), request.getEntityId());

        if (request.getFileUrl() != null && !isValidUrl(request.getFileUrl())) {
            throw new BusinessException("messages.document.invalidUrl");
        }

        Document document = documentMapper.toDocument(request);

        // (TODO: get from security context when auth is implemented)
        User currentUser = userRepository.findById(1L)
                .orElseThrow(() -> new ObjectNotFoundException("messages.user.notFound", 1L));
        document.setUploadedBy(currentUser);

        Document savedDocument = documentRepository.save(document);
        log.info("Successfully created document with ID: {} for {} ID: {}",
                savedDocument.getId(), request.getEntityType(), request.getEntityId());

        return documentMapper.toDocumentResponse(savedDocument);
    }

    /**
     * Uploads a document with file content.
     */
    @Transactional
    public DocumentResponse uploadDocument(EntityType entityType, Long entityId,
                                           MultipartFile file, String description) {
        log.info("Uploading document for entity type: {} with entity ID: {}", entityType, entityId);

        validateEntityExists(entityType, entityId);

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("messages.document.fileTooLarge");
        }

        if (file.isEmpty()) {
            throw new BusinessException("messages.document.fileEmpty");
        }

        FileType fileType = determineFileType(file.getOriginalFilename());
        // Clean file name from path traversal characters

        String fileUrl = generateFileUrl(file.getOriginalFilename());


        try {
            Document document = Document.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .fileUrl(fileUrl)
                    .fileType(fileType)
                    .description(description)
                    .file(file.getBytes())
                    .build();

            // Set uploaded by user
            User currentUser = userRepository.findById(1L)
                    .orElseThrow(() -> new ObjectNotFoundException("messages.user.notFound", 1L));
            document.setUploadedBy(currentUser);

            Document savedDocument = documentRepository.save(document);
            log.info("Successfully uploaded document with ID: {} for {} ID: {}",
                    savedDocument.getId(), entityType, entityId);

            return documentMapper.toDocumentResponse(savedDocument);
        } catch (IOException e) {
            log.error("Error reading file content: {}", e.getMessage());
            throw new BusinessException("messages.document.uploadError");
        }
    }

    /**
     * Updates an existing document metadata.
     */
    @Transactional
    public DocumentResponse update(Long id, DocumentRequest request) {
        log.info("Updating document with ID: {}", id);

        Document existingDocument = documentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Document not found with ID: {}", id);
                    return new ObjectNotFoundException("messages.document.notFound", id);
                });

        if (!existingDocument.getEntityType().equals(request.getEntityType()) ||
                !existingDocument.getEntityId().equals(request.getEntityId())) {
            validateEntityExists(request.getEntityType(), request.getEntityId());
        }

        // Update fields
        documentMapper.updateDocumentFromRequest(request, existingDocument);

        Document updatedDocument = documentRepository.save(existingDocument);
        log.info("Successfully updated document with ID: {}", id);

        return documentMapper.toDocumentResponse(updatedDocument);
    }

    /**
     * Gets document by ID.
     */
    @Transactional(readOnly = true)
    public DocumentResponse getById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("messages.document.notFound", id));
        return documentMapper.toDocumentResponse(document);
    }

    /**
     * Gets document file content by ID.
     */
    @Transactional(readOnly = true)
    public byte[] getFileContent(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("messages.document.notFound", id));

        if (document.getFile() == null) {
            throw new BusinessException("messages.document.noFileContent");
        }

        return document.getFile();
    }

    /**
     * Lists all documents with pagination.
     */
    @Transactional(readOnly = true)
    public Page<DocumentSummary> list(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt"));

        Page<Document> documentPage;
        if (search != null && !search.isBlank()) {
            documentPage = documentRepository.searchByDescription(search.trim(), pageable);
        } else {
            documentPage = documentRepository.findAll(pageable);
        }

        return documentPage.map(documentMapper::toDocumentSummary);
    }

    /**
     * Lists documents for a specific entity.
     */
    @Transactional(readOnly = true)
    public Page<DocumentSummary> listByEntity(EntityType entityType, Long entityId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt"));
        Page<Document> documentPage = documentRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
        return documentPage.map(documentMapper::toDocumentSummary);
    }

    /**
     * Lists all documents for a specific entity (no pagination).
     */
    @Transactional(readOnly = true)
    public List<DocumentResponse> getAllByEntity(EntityType entityType, Long entityId) {
        List<Document> documents = documentRepository.findByEntityTypeAndEntityId(entityType, entityId);
        return documents.stream()
                .map(documentMapper::toDocumentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lists documents by entity type.
     */
    @Transactional(readOnly = true)
    public Page<DocumentSummary> listByEntityType(EntityType entityType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt"));
        Page<Document> documentPage = documentRepository.findByEntityType(entityType, pageable);
        return documentPage.map(documentMapper::toDocumentSummary);
    }

    /**
     * Lists documents by file type.
     */
    @Transactional(readOnly = true)
    public Page<DocumentSummary> listByFileType(FileType fileType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt"));
        Page<Document> documentPage = documentRepository.findByFileType(fileType, pageable);
        return documentPage.map(documentMapper::toDocumentSummary);
    }

    /**
     * Deletes a document by ID.
     */
    @Transactional
    public String delete(Long id) {
        log.info("Deleting document with ID: {}", id);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("messages.document.notFound", id));

        documentRepository.delete(document);
        log.info("Successfully deleted document with ID: {}", id);

        return "messages.document.deleted";
    }

    /**
     * Deletes all documents for a specific entity.
     */
    @Transactional
    public void deleteAllByEntity(EntityType entityType, Long entityId) {
        log.info("Deleting all documents for entity type: {} with entity ID: {}", entityType, entityId);
        documentRepository.deleteByEntityTypeAndEntityId(entityType, entityId);
        log.info("Successfully deleted all documents for {} ID: {}", entityType, entityId);
    }

    /**
     * Gets document count for a specific entity.
     */
    @Transactional(readOnly = true)
    public long countByEntity(EntityType entityType, Long entityId) {
        return documentRepository.countByEntityTypeAndEntityId(entityType, entityId);
    }

    /**
     * Gets document statistics.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getDocumentStats() {
        return Map.of(
                "totalDocuments", documentRepository.count(),
                "customerDocuments", documentRepository.countByEntityType(EntityType.CUSTOMER),
                "vendorDocuments", documentRepository.countByEntityType(EntityType.VENDOR),
                "purchaseDocuments", documentRepository.countByEntityType(EntityType.PURCHASE),
                "contractDocuments", documentRepository.countByEntityType(EntityType.CONTRACT),
                "paymentDocuments", documentRepository.countByEntityType(EntityType.PAYMENT),
                "imageFiles", documentRepository.countByFileType(FileType.IMAGE),
                "pdfFiles", documentRepository.countByFileType(FileType.PDF),
                "docFiles", documentRepository.countByFileType(FileType.DOC)
        );
    }

    // ============== Helper Methods ==============

    /**
     * Validates that the referenced entity exists.
     */
    private void validateEntityExists(EntityType entityType, Long entityId) {
        boolean exists = switch (entityType) {
            case CUSTOMER -> customerRepository.existsByIdAndActiveTrue(entityId);
            case VENDOR -> vendorRepository.existsByIdAndActiveTrue(entityId);
            case PURCHASE -> purchaseRepository.existsById(entityId);
            case CONTRACT -> contractRepository.existsById(entityId);
            case PAYMENT -> paymentRepository.existsById(entityId);
        };

        if (!exists) {
            log.error("Entity not found: {} with ID: {}", entityType, entityId);
            throw new ObjectNotFoundException("messages.document.entityNotFound", entityType, entityId);
        }
    }

    /**
     * Determines file type from filename extension.
     */
    private FileType determineFileType(String filename) {
        if (filename == null || !filename.contains(".")) {
            return FileType.OTHER;
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

        if (ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            return FileType.IMAGE;
        } else if (ALLOWED_PDF_EXTENSIONS.contains(extension)) {
            return FileType.PDF;
        } else if (ALLOWED_DOC_EXTENSIONS.contains(extension)) {
            return FileType.DOC;
        }

        return FileType.OTHER;
    }

    /**
     * Generates a unique file URL for stored files.
     */
    private String generateFileUrl(String originalFilename) {

        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BusinessException("messages.document.fileNameEmpty");
        }

        String cleanName = StringUtils.cleanPath(originalFilename);

        String extension = "";
        int lastDotIndex = cleanName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < cleanName.length() - 1) {
            extension = cleanName.substring(lastDotIndex).toLowerCase();
        }

        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        String baseName = (lastDotIndex > 0) ?
                cleanName.substring(0, lastDotIndex) : cleanName;

        String safeBaseName = baseName.replaceAll("[^a-zA-Z0-9-_]", "_");
        safeBaseName = safeBaseName.substring(0, Math.min(safeBaseName.length(), 50));

        return String.format("/documents/%s_%s_%s%s",
                timestamp, uuid, safeBaseName, extension);

    }

    /**
     * Validates URL format.
     */
    private boolean isValidUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        // Basic URL validation - can be enhanced as needed
        return url.startsWith("/") || url.startsWith("http://") || url.startsWith("https://");
    }


    /**
     * Determines MediaType from FileType.
     */
    public MediaType determineMediaType(FileType fileType) {
        return switch (fileType) {
            case IMAGE -> MediaType.IMAGE_JPEG;
            case PDF -> MediaType.APPLICATION_PDF;
            case DOC -> MediaType.APPLICATION_OCTET_STREAM;
            case OTHER -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }

    /**
     * Extracts filename from file URL.
     */
    public String extractFilename(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return "download";
        }
        int lastSlash = fileUrl.lastIndexOf("/");
        return lastSlash >= 0 ? fileUrl.substring(lastSlash + 1) : fileUrl;
    }
}

