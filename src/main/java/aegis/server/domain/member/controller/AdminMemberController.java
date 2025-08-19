package aegis.server.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.member.dto.response.MemberDemoteResponse;
import aegis.server.domain.member.service.MemberService;

@Tag(name = "Admin Member", description = "관리자 회원 관리 API")
@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final MemberService memberService;

    @Operation(
            summary = "회원 강등",
            description = "현재 학기 회비를 납부하지 않은 회원들을 ROLE_GUEST로 강등합니다. 관리자(ROLE_ADMIN)는 강등 대상에서 제외됩니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "회원 강등 성공"),
                @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content)
            })
    @PostMapping("/demote")
    public ResponseEntity<MemberDemoteResponse> demoteMembersForCurrentSemester() {
        MemberDemoteResponse response = memberService.demoteMembersForCurrentSemester();
        return ResponseEntity.ok().body(response);
    }
}
