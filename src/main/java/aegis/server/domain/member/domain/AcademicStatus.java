package aegis.server.domain.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AcademicStatus {
    ENROLLED("재학"),
    LEAVE_OF_ABSENCE("휴학"),
    GRADUATED("졸업");

    private final String value;
}
