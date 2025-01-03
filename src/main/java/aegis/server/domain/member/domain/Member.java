package aegis.server.domain.member.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String email;

    private String name;

    @Enumerated(EnumType.STRING)
    private Role role;

    public static Member createGuestMember(String email, String name) {
        return Member.builder()
                .email(email)
                .name(name)
                .role(Role.GUEST)
                .build();
    }
}
