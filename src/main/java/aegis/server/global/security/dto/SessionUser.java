package aegis.server.global.security.dto;

import aegis.server.domain.member.domain.Role;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class SessionUser implements Serializable {
    private final String name;
    private final String email;
    private final Role role;

    public SessionUser(String name, String email) {
        this.name = name;
        this.email = email;
        this.role = Role.USER;
    }
}
