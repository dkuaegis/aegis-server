package aegis.server.global.security.controller;

import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.oidc.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class AuthController {

    @GetMapping("/auth/check")
    public ResponseEntity<Void> check(@LoginUser UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok().build();
    }
}
