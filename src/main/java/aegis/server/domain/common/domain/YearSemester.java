package aegis.server.domain.common.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Year;

@Getter
@RequiredArgsConstructor
public enum YearSemester {
    YEAR_SEMESTER_2025_1("2025-1"),
    YEAR_SEMESTER_2025_2("2025-2");

    private final String value;
}
