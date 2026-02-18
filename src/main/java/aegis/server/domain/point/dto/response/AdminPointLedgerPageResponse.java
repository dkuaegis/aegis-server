package aegis.server.domain.point.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import aegis.server.domain.point.domain.PointTransaction;

public record AdminPointLedgerPageResponse(
        List<AdminPointLedgerItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {

    public static AdminPointLedgerPageResponse from(Page<PointTransaction> ledgerPage) {
        return new AdminPointLedgerPageResponse(
                ledgerPage.getContent().stream()
                        .map(AdminPointLedgerItemResponse::from)
                        .toList(),
                ledgerPage.getNumber(),
                ledgerPage.getSize(),
                ledgerPage.getTotalElements(),
                ledgerPage.getTotalPages(),
                ledgerPage.hasNext());
    }
}
