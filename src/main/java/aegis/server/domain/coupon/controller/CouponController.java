package aegis.server.domain.coupon.controller;

import aegis.server.domain.coupon.dto.response.IssuedCouponResponse;
import aegis.server.domain.coupon.service.CouponService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.oidc.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/issued/me")
    public ResponseEntity<List<IssuedCouponResponse>> getMyAllValidIssuedCoupon(
            @LoginUser UserDetails userDetails
    ) {
        List<IssuedCouponResponse> responses = couponService.findMyAllValidIssuedCoupons(userDetails);
        return ResponseEntity.ok().body(responses);
    }
}
