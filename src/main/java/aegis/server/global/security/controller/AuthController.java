package aegis.server.global.security.controller;

import aegis.server.domain.member.domain.Student;
import aegis.server.domain.member.repository.StudentRepository;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.oidc.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

// TODO: 전면 리팩토링 필요
@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final PaymentRepository paymentRepository;
    private final StudentRepository studentRepository;

    @GetMapping("/auth/check")
    public ResponseEntity<AuthCheckResponse> check(@LoginUser UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Student student = studentRepository.findByMemberIdInCurrentYearSemester(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));
        Optional<Payment> optionalPayment = paymentRepository.findByStudentInCurrentYearSemester(student);

        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();
            return ResponseEntity.status(HttpStatus.OK).body(new AuthCheckResponse(payment.getStatus()));
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(new AuthCheckResponse(PaymentStatus.PENDING));
        }
    }

    @GetMapping("/auth/error/not-dku")
    public ResponseEntity<String> notDku() {
        String html = """
                <!DOCTYPE html>
                  <html lang="ko">
                    <head>
                      <meta charset="UTF-8" />
                      <meta name="viewport" content="width=device-width, initial-scale=1" />
                      <title>인증 실패</title>
                      <link rel="stylesheet" href="https://unpkg.com/mvp.css" />
                      <style>
                        .container {
                          max-width: 600px;
                          margin: 2rem auto;
                          padding: 0 1rem;
                          word-break: keep-all;
                        }
                        .button-container {
                          text-align: center;
                        }
                        .button {
                          display: inline-block;
                          background: #007bff;
                          color: #fff;
                          padding: 0.75em 1.5em;
                          border-radius: 4px;
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
                          <h1>단국대학교 이메일로만 가입이 가능합니다</h1>
                          <p>@dankook.ac.kr 이메일로 다시 시도해주세요.</p>
                        </header>
                        <section class="button-container">
                          <p>
                            <a href="https://join.dkuaegis.org" class="button"
                              >메인으로 돌아가기</a
                            >
                          </p>
                        </section>
                      </main>
                    </body>
                  </html>
                """;
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
}
