package aegis.server.domain.payment.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import aegis.server.domain.payment.domain.Transaction;

public record AdminTransactionPageResponse(
        List<AdminTransactionItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {

    public static AdminTransactionPageResponse from(Page<Transaction> transactionPage) {
        return new AdminTransactionPageResponse(
                transactionPage.getContent().stream()
                        .map(AdminTransactionItemResponse::from)
                        .toList(),
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages(),
                transactionPage.hasNext());
    }
}
