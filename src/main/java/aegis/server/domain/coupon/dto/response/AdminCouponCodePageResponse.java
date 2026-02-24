package aegis.server.domain.coupon.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import aegis.server.domain.coupon.domain.CouponCode;

public record AdminCouponCodePageResponse(
        List<AdminCouponCodeResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {

    public static AdminCouponCodePageResponse from(Page<CouponCode> couponCodePage) {
        return new AdminCouponCodePageResponse(
                couponCodePage.getContent().stream()
                        .map(AdminCouponCodeResponse::from)
                        .toList(),
                couponCodePage.getNumber(),
                couponCodePage.getSize(),
                couponCodePage.getTotalElements(),
                couponCodePage.getTotalPages(),
                couponCodePage.hasNext());
    }
}
