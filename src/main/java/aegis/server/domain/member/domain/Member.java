package aegis.server.domain.member.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false)
    private String oidcId;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    public static Member createMember(String oidcId, String email, String name) {
        return Member.builder()
                .role(Role.USER)
                .oidcId(oidcId)
                .email(email)
                .name(name)
                .build();
    }
}
