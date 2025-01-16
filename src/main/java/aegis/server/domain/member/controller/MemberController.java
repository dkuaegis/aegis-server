package aegis.server.domain.member.controller;

import aegis.server.domain.member.dto.request.MemberUpdateRequest;
import aegis.server.domain.member.dto.response.MemberResponse;
import aegis.server.domain.member.service.MemberService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.dto.SessionUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    @Operation(
            summary = "구글 로그인 성공시 기본 인적사항 페이지로 이동",
            description = "session정보를 통해 MemberResponse를 생성, 현재 JoinProgress를 PERSONAL_INFORMATION로 변경",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "sessionUser를 통한 MemberResponse불러오기 성공",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MemberResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<MemberResponse> getMember(
            @LoginUser SessionUser sessionUser
    ) {
        MemberResponse response = memberService.getMember(sessionUser);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    @Operation(
            summary = "기본 인적사항 저장",
            responses = {@ApiResponse(responseCode = "201", description = "기본 인적사항 저장 완료")}
    )
    public ResponseEntity<Void> updateMember(
            @LoginUser SessionUser sessionUser,
            @Validated @RequestBody MemberUpdateRequest request
    ) {
        memberService.updateMember(sessionUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
