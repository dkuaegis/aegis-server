package aegis.server.global.constant;

import java.math.BigDecimal;
import java.util.List;

import aegis.server.domain.common.domain.YearSemester;

public class Constant {

    public static final BigDecimal CLUB_DUES = BigDecimal.valueOf(15000);

    public static final YearSemester CURRENT_YEAR_SEMESTER = YearSemester.YEAR_SEMESTER_2025_2;

    public static final String PROD_SERVER_URL = "https://api.dkuaegis.org";
    public static final String PROD_JOIN_URL = "https://join.dkuaegis.org";
    public static final String PROD_STUDY_URL = "https://study.dkuaegis.org";

    public static final String DEV_SERVER_URL = "https://dev-api.dkuaegis.org";
    public static final String DEV_JOIN_URL = "https://dev-join.dkuaegis.org";
    public static final String DEV_STUDY_URL = "https://dev-study.dkuaegis.org";

    public static final String LOCAL_VITE_BUILD_CLIENT_URL = "http://localhost:4173";
    public static final String LOCAL_VITE_CLIENT_URL = "http://localhost:5173";
    public static final String LOCAL_SERVER_URL = "http://localhost:8080";

    public static final List<String> ALLOWED_CLIENT_URLS = List.of(
            PROD_SERVER_URL,
            PROD_JOIN_URL,
            PROD_STUDY_URL,
            DEV_SERVER_URL,
            DEV_JOIN_URL,
            DEV_STUDY_URL,
            LOCAL_VITE_BUILD_CLIENT_URL,
            LOCAL_VITE_CLIENT_URL,
            LOCAL_SERVER_URL);
}
