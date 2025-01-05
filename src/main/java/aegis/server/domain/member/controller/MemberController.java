package aegis.server.domain.member.controller;

import aegis.server.domain.member.dto.request.MemberUpdateRequest;
import aegis.server.domain.member.dto.response.MemberResponse;
import aegis.server.domain.member.service.MemberService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.dto.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponse> getMember(
            @LoginUser SessionUser sessionUser,
            @PathVariable Long memberId
    ) {
        MemberResponse response = memberService.getMember(sessionUser, memberId);
        return ResponseEntity.ok().body(response);
    }


    @PostMapping
    public ResponseEntity<Void> updateMember(
            @LoginUser SessionUser sessionUser,
            @Validated @RequestBody MemberUpdateRequest request
    ) {
        memberService.updateMember(sessionUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
