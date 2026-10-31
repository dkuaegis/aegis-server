package aegis.server.domain.featureflag.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.featureflag.dto.response.FeaturePolicyResponse;
import aegis.server.domain.featureflag.service.FeaturePolicyService;

@Tag(name = "Feature Flag", description = "운영 플래그 공개 조회 API")
@RestController
@RequiredArgsConstructor
public class FeatureFlagController {

    private final FeaturePolicyService featurePolicyService;

    @Operation(
            operationId = "getFeaturePolicy",
            summary = "운영 정책 조회",
            description = "회원가입, 스터디 개설, 스터디 신청이 지금 허용되는지 조회합니다. 로그인하지 않아도 조회할 수 있습니다.",
            responses = {@ApiResponse(responseCode = "200", description = "운영 정책 조회 성공")})
    @GetMapping("/feature-flags")
    public ResponseEntity<FeaturePolicyResponse> getFeaturePolicy() {
        return ResponseEntity.ok(featurePolicyService.getFeaturePolicy());
    }
}
