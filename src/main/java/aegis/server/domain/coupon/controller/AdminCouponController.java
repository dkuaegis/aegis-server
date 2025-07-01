package aegis.server.domain.coupon.controller;

import java.util.List;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.coupon.dto.request.CouponCodeCreateRequest;
import aegis.server.domain.coupon.dto.request.CouponCreateRequest;
import aegis.server.domain.coupon.dto.request.CouponIssueRequest;
import aegis.server.domain.coupon.dto.response.CouponCodeResponse;
import aegis.server.domain.coupon.dto.response.CouponResponse;
import aegis.server.domain.coupon.dto.response.IssuedCouponResponse;
import aegis.server.domain.coupon.service.CouponService;

@Tag(name = "Admin Coupon", description = "관리자 쿠폰 관리 API")
@RestController
@RequestMapping("/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;

    @Operation(summary = "모든 쿠폰 조회", description = "관리자가 등록된 모든 쿠폰을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쿠폰 조회 성공")
    })
    @GetMapping
    public ResponseEntity<List<CouponResponse>> getAllCoupons() {
        List<CouponResponse> response = couponService.findAllCoupons();
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "쿠폰 생성", description = "새로운 쿠폰을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "쿠폰 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "409", description = "동일한 이름과 할인금액의 쿠폰이 이미 존재")
    })
    @PostMapping
    public ResponseEntity<Void> createCoupon(
            @Valid @RequestBody CouponCreateRequest request
    ) {
        couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "쿠폰 삭제", description = "지정된 ID의 쿠폰을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "쿠폰 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 쿠폰"),
            @ApiResponse(responseCode = "409", description = "발급된 쿠폰이 있어 삭제할 수 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(
            @Parameter(description = "삭제할 쿠폰 ID") @PathVariable Long id
    ) {
        couponService.deleteCoupon(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // - - -

    @Operation(summary = "모든 발급된 쿠폰 조회", description = "관리자가 발급된 모든 쿠폰을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "발급된 쿠폰 조회 성공")
    })
    @GetMapping("/issued")
    public ResponseEntity<List<IssuedCouponResponse>> getAllIssuedCoupons() {
        List<IssuedCouponResponse> response = couponService.findAllIssuedCoupons();
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "쿠폰 발급", description = "특정 사용자들에게 쿠폰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "쿠폰 발급 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 쿠폰 또는 사용자")
    })
    @PostMapping("/issued")
    public ResponseEntity<Void> createIssuedCoupon(
            @Valid @RequestBody CouponIssueRequest request
    ) {
        couponService.createIssuedCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "발급된 쿠폰 삭제", description = "지정된 ID의 발급된 쿠폰을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "발급된 쿠폰 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 발급된 쿠폰")
    })
    @DeleteMapping("/issued/{id}")
    public ResponseEntity<Void> deleteIssuedCoupon(
            @Parameter(description = "삭제할 발급된 쿠폰 ID") @PathVariable Long id
    ) {
        couponService.deleteIssuedCoupon(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // - - -

    @Operation(summary = "모든 쿠폰 코드 조회", description = "관리자가 생성된 모든 쿠폰 코드를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쿠폰 코드 조회 성공")
    })
    @GetMapping("/code")
    public ResponseEntity<List<CouponCodeResponse>> getAllCodeCoupons() {
        List<CouponCodeResponse> response = couponService.findAllCouponCode();
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "쿠폰 코드 생성", description = "지정된 쿠폰에 대한 코드를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "쿠폰 코드 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 쿠폰"),
            @ApiResponse(responseCode = "500", description = "고유 쿠폰 코드 생성 실패 (100회 시도 후 실패)")
    })
    @PostMapping("/code")
    public ResponseEntity<Void> createCodeCoupon(
            @Valid @RequestBody CouponCodeCreateRequest request
    ) {
        couponService.createCouponCode(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "쿠폰 코드 삭제", description = "지정된 ID의 쿠폰 코드를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "쿠폰 코드 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 쿠폰 코드")
    })
    @DeleteMapping("/code/{id}")
    public ResponseEntity<Void> deleteCodeCoupon(
            @Parameter(description = "삭제할 쿠폰 코드 ID") @PathVariable Long id
    ) {
        couponService.deleteCodeCoupon(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
