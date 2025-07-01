package aegis.server.domain.member.controller;

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

import aegis.server.domain.member.dto.request.PersonalInfoUpdateRequest;
import aegis.server.domain.member.dto.response.PersonalInfoResponse;
import aegis.server.domain.member.service.MemberService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.oidc.UserDetails;

@Tag(name = "Member", description = "회원 개인정보 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "개인정보 조회", description = "로그인한 사용자의 개인정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "개인정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<PersonalInfoResponse> getPersonalInfo(
            @Parameter(hidden = true) @LoginUser UserDetails userDetails
    ) {
        return ResponseEntity.ok(memberService.getPersonalInfo(userDetails));
    }

    @Operation(summary = "개인정보 수정", description = "로그인한 사용자의 개인정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "개인정보 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음")
    })
    @PostMapping
    public ResponseEntity<Void> updatePersonalInfo(
            @Parameter(hidden = true) @LoginUser UserDetails userDetails,
            @Valid @RequestBody PersonalInfoUpdateRequest request
    ) {
        memberService.updatePersonalInfo(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
