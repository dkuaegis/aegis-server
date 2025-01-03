package aegis.server.global.security.dto;

import aegis.server.domain.member.domain.Role;
import aegis.server.global.security.oidc.UserAuthInfo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class SessionUser implements Serializable {
    private final Long id;
    private final String email;
    private final String name;
    private final Role role;

    public static SessionUser from(UserAuthInfo userAuthInfo) {
        return SessionUser.builder()
                .id(userAuthInfo.getId())
                .email(userAuthInfo.getEmail())
                .name(userAuthInfo.getName())
                .role(userAuthInfo.getRole())
                .build();
    }
}
