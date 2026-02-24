package aegis.server.domain.payment.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import aegis.server.domain.payment.domain.Payment;

public record AdminPaymentPageResponse(
        List<AdminPaymentItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {

    public static AdminPaymentPageResponse from(Page<Payment> paymentPage) {
        return new AdminPaymentPageResponse(
                paymentPage.getContent().stream()
                        .map(AdminPaymentItemResponse::from)
                        .toList(),
                paymentPage.getNumber(),
                paymentPage.getSize(),
                paymentPage.getTotalElements(),
                paymentPage.getTotalPages(),
                paymentPage.hasNext());
    }
}
