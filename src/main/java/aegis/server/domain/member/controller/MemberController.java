package aegis.server.domain.member.controller;

import aegis.server.domain.member.dto.request.PersonalInfoUpdateRequest;
import aegis.server.domain.member.dto.response.PersonalInfoResponse;
import aegis.server.domain.member.service.MemberService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.oidc.UserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<PersonalInfoResponse> getPersonalInfo(
            @LoginUser UserDetails userDetails
    ) {
        return ResponseEntity.ok(memberService.getPersonalInfo(userDetails));
    }

    @PostMapping
    public ResponseEntity<Void> updatePersonalInfo(
            @LoginUser UserDetails userDetails,
            @Valid @RequestBody PersonalInfoUpdateRequest request
    ) {
        memberService.updatePersonalInfo(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
