package com.secureclaims.identity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic paginated response DTO that safely serializes Spring Data Page results.
 * Extracts pagination metadata from PageImpl to avoid Jackson serialization issues.
 *
 * @param <T> the type of elements in the page
 * @author SecureClaims Team
 * @since 1.0
 */
@Data
@Builder
public class PagedResponse<T> {

    @Schema(description = "List of items in the current page")
    private List<T> content;

    @Schema(description = "Current page number (zero-based)")
    private int page;

    @Schema(description = "Page size")
    private int size;

    @Schema(description = "Total number of elements")
    private long totalElements;

    @Schema(description = "Total number of pages")
    private int totalPages;

    @Schema(description = "Whether this is the last page")
    private boolean last;

    /**
     * Factory method to convert a Spring Data Page into a PagedResponse.
     *
     * @param page the Spring Data Page object
     * @param <T>  the content type
     * @return a PagedResponse containing the page data and metadata
     */
    public static <T> PagedResponse<T> from(final Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
