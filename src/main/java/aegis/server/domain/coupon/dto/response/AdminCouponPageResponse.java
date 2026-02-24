package aegis.server.domain.coupon.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import aegis.server.domain.coupon.domain.Coupon;

public record AdminCouponPageResponse(
        List<AdminCouponResponse> content, int page, int size, long totalElements, int totalPages, boolean hasNext) {

    public static AdminCouponPageResponse from(Page<Coupon> couponPage) {
        return new AdminCouponPageResponse(
                couponPage.getContent().stream().map(AdminCouponResponse::from).toList(),
                couponPage.getNumber(),
                couponPage.getSize(),
                couponPage.getTotalElements(),
                couponPage.getTotalPages(),
                couponPage.hasNext());
    }
}
