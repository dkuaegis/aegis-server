package aegis.server.global.security.oidc;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.Role;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class UserDetails implements Serializable {

    private Long id;
    private String email;
    private String name;
    private Role role;

    public static UserDetails from(Member member) {
        return UserDetails.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .role(member.getRole())
                .build();
    }
}
