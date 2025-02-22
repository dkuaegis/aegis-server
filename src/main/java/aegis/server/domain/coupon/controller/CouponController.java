package aegis.server.domain.coupon.controller;

import aegis.server.domain.coupon.dto.request.CouponCodeUseRequest;
import aegis.server.domain.coupon.dto.response.IssuedCouponResponse;
import aegis.server.domain.coupon.service.CouponService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.oidc.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/code")
    public ResponseEntity<Void> codeCouponIssue(
            @LoginUser UserDetails userDetails,
            @RequestBody CouponCodeUseRequest request
    ) {
        couponService.useCouponCode(userDetails, request);
        return ResponseEntity.ok().build();

    }
}
