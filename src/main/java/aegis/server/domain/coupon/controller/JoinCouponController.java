package aegis.server.domain.coupon.controller;

import aegis.server.domain.coupon.dto.response.IssuedCouponResponse;
import aegis.server.domain.coupon.service.CouponService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.dto.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/join/coupons")
@RequiredArgsConstructor
public class JoinCouponController {

    private final CouponService couponService;

    @GetMapping("/issued/me")
    public ResponseEntity<List<IssuedCouponResponse>> getMyAllValidIssuedCoupons(@LoginUser SessionUser user) {
        List<IssuedCouponResponse> response = couponService.findMyAllValidIssuedCoupons(user.getId());
        return ResponseEntity.ok().body(response);
    }
}
