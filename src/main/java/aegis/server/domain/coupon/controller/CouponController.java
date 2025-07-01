package aegis.server.domain.coupon.controller;

import aegis.server.domain.coupon.dto.request.CouponCodeUseRequest;
import aegis.server.domain.coupon.dto.response.IssuedCouponResponse;
import aegis.server.domain.coupon.service.CouponService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.oidc.UserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Coupon", description = "사용자 쿠폰 관리 API")
@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "내 발급된 쿠폰 조회", description = "로그인한 사용자의 유효한 발급된 쿠폰을 모두 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쿠폰 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/issued/me")
    public ResponseEntity<List<IssuedCouponResponse>> getMyAllValidIssuedCoupon(
            @Parameter(hidden = true) @LoginUser UserDetails userDetails
    ) {
        List<IssuedCouponResponse> responses = couponService.findMyAllValidIssuedCoupons(userDetails);
        return ResponseEntity.ok().body(responses);
    }

    @Operation(summary = "쿠폰 코드 사용", description = "쿠폰 코드를 사용하여 쿠폰을 발급받습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쿠폰 코드 사용 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 쿠폰 코드 형식"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 쿠폰 코드"),
            @ApiResponse(responseCode = "409", description = "이미 사용된 쿠폰 코드 또는 중복 사용")
    })
    @PostMapping("/code")
    public ResponseEntity<Void> codeCouponIssue(
            @Parameter(hidden = true) @LoginUser UserDetails userDetails,
            @Valid @RequestBody CouponCodeUseRequest request
    ) {
        couponService.useCouponCode(userDetails, request);
        return ResponseEntity.ok().build();

    }
}
