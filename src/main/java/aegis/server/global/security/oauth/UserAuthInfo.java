package aegis.server.global.security.oauth;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.Role;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class UserAuthInfo implements Serializable {

    private Long id;

    private String email;

    private String name;

    private Role role;

    public static UserAuthInfo from(Member member) {
        return UserAuthInfo.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .role(member.getRole())
                .build();
    }
}
