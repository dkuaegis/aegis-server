package aegis.server.global.security.controller;

import java.util.Optional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.oidc.UserDetails;

// TODO: 전면 리팩토링 필요
@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final PaymentRepository paymentRepository;

    @Value("${email-restriction.admin-email}")
    private String adminEmail;

    @Operation(
            summary = "인증 상태 확인",
            description = "사용자의 인증 상태와 결제 상태를 확인합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "인증 상태 확인 성공"),
                @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
                @ApiResponse(responseCode = "404", description = "학생 정보를 찾을 수 없음", content = @Content)
            })
    @GetMapping("/auth/check")
    public ResponseEntity<AuthCheckResponse> check(@Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (userDetails.getEmail().equals(adminEmail)) {
            return ResponseEntity.status(HttpStatus.OK).body(new AuthCheckResponse(PaymentStatus.COMPLETED));
        }

        Optional<Payment> optionalPayment =
                paymentRepository.findByMemberIdInCurrentYearSemester(userDetails.getMemberId());

        if (optionalPayment.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new AuthCheckResponse(optionalPayment.get().getStatus()));
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(new AuthCheckResponse(PaymentStatus.PENDING));
        }
    }

    @Operation(
            summary = "비단국대 이메일 오류 페이지",
            description = "단국대학교 이메일이 아닌 경우 리다이렉트되는 오류 페이지입니다.",
            responses = {@ApiResponse(responseCode = "200", description = "오류 페이지 반환")})
    @GetMapping("/auth/error/not-dku")
    public ResponseEntity<String> notDku() {
        String html =
                """
               <!DOCTYPE html>
               <html lang="ko">
                 <head>
                   <meta charset="UTF-8" />
                   <meta name="viewport" content="width=device-width, initial-scale=1" />
                   <title>인증 실패</title>
                   <link rel="stylesheet" href="https://unpkg.com/mvp.css" />
                   <style>
                     header {
                       padding: 1rem 1rem;
                     }
                     .container {
                       max-width: 640px;
                       margin: 2rem auto;
                       padding: 0 1rem;
                       word-break: keep-all;
                     }
                     .button-container {
                       text-align: center;
                     }
                     .buttons {
                       display: flex;
                       gap: 0.75rem;
                       justify-content: center;
                       flex-wrap: wrap;
                     }
                     .button {
                       display: inline-block;
                       background: #007bff;
                       color: #fff;
                       padding: 0.75em 1.1em;
                       border-radius: 6px;
                       text-decoration: none;
                     }
                     .button:hover {
                       background: #0056b3;
                     }
                   </style>
                 </head>
                 <body>
                   <main class="container">
                     <header>
                       <h1>단국대학교 이메일만 지원합니다</h1>
                       <p>@dankook.ac.kr 이메일로 다시 시도해주세요.</p>
                     </header>
                     <section class="button-container">
                       <div class="buttons">
                         <a href="https://join.dkuaegis.org" class="button">회원가입</a>
                         <a href="https://mypage.dkuaegis.org" class="button">마이페이지</a>
                         <a href="https://study.dkuaegis.org" class="button">스터디</a>
                       </div>
                     </section>
                   </main>
                 </body>
               </html>
                """;
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
}
