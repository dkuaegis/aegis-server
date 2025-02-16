package aegis.server.domain.coupon.controller;

import aegis.server.domain.coupon.dto.request.CouponCreateRequest;
import aegis.server.domain.coupon.dto.request.CouponIssueRequest;
import aegis.server.domain.coupon.dto.response.CouponResponse;
import aegis.server.domain.coupon.dto.response.IssuedCouponResponse;
import aegis.server.domain.coupon.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<List<CouponResponse>> getAllCoupons() {
        List<CouponResponse> response = couponService.findAllCoupons();
        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<Void> createCoupon(
            @Valid @RequestBody CouponCreateRequest request
    ) {
        couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/issued")
    public ResponseEntity<List<IssuedCouponResponse>> getAllIssuedCoupons() {
        List<IssuedCouponResponse> response = couponService.findAllIssuedCoupons();
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/issued")
    public ResponseEntity<Void> createIssuedCoupon(
            @Valid @RequestBody CouponIssueRequest request
    ) {
        couponService.createIssuedCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/issued/{id}")
    public ResponseEntity<Void> deleteIssuedCoupon(@PathVariable Long id) {
        couponService.deleteIssuedCoupon(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
