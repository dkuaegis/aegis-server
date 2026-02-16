package aegis.server.domain.common.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum YearSemester {
    YEAR_SEMESTER_2025_1("2025-1"),
    YEAR_SEMESTER_2025_2("2025-2"),
    YEAR_SEMESTER_2026_1("2026-1");

    private final String value;
}
