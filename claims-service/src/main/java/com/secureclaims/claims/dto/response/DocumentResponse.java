package com.secureclaims.claims.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for document metadata.
 *
 * @author SecureClaims Team
 * @since 1.0
 */
@Data
@Builder
public class DocumentResponse {

    @Schema(description = "Document ID")
    private UUID documentId;

    @Schema(description = "Original filename")
    private String originalFilename;

    @Schema(description = "Storage path")
    private String filePath;

    @Schema(description = "MIME type")
    private String mimeType;

    @Schema(description = "File size in bytes")
    private Long fileSizeBytes;

    @Schema(description = "Upload timestamp")
    private LocalDateTime uploadedAt;
}
