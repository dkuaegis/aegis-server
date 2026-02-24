package aegis.server.domain.coupon.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import aegis.server.domain.coupon.domain.IssuedCoupon;

public record AdminIssuedCouponPageResponse(
        List<AdminIssuedCouponResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {

    public static AdminIssuedCouponPageResponse from(Page<IssuedCoupon> issuedCouponPage) {
        return new AdminIssuedCouponPageResponse(
                issuedCouponPage.getContent().stream()
                        .map(AdminIssuedCouponResponse::from)
                        .toList(),
                issuedCouponPage.getNumber(),
                issuedCouponPage.getSize(),
                issuedCouponPage.getTotalElements(),
                issuedCouponPage.getTotalPages(),
                issuedCouponPage.hasNext());
    }
}
