package aegis.server.global.security.oidc;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.Role;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class UserDetails implements Serializable {

    private Long memberId;
    private String email;
    private String name;
    private Role role;

    // Jackson용 생성자
    @JsonCreator
    public UserDetails(
            @JsonProperty("memberId") Long memberId,
            @JsonProperty("email") String email,
            @JsonProperty("name") String name,
            @JsonProperty("role") Role role) {
        this.memberId = memberId;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public static UserDetails from(Member member) {
        return UserDetails.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .role(member.getRole())
                .build();
    }
}
