package aegis.server.global.constant;

import aegis.server.domain.common.domain.YearSemester;

import java.math.BigDecimal;
import java.util.List;

public class Constant {

    public static final BigDecimal CLUB_DUES = BigDecimal.valueOf(10000);

    public static final YearSemester CURRENT_YEAR_SEMESTER = YearSemester.YEAR_SEMESTER_2025_1;

    public static final String PROD_CLIENT_JOIN_URL = "https://join.dk-aegis.org";

    public static final String LOCAL_VITE_BUILD_CLIENT_URL = "http://localhost:4173";
    public static final String LOCAL_VITE_CLIENT_URL = "http://localhost:5173";

    // TODO: Aegis Cloudflare 계정으로 변경
    public static final String CF_TUNNEL_4173_URL = "https://4173.seongmin.dev";
    public static final String CF_TUNNEL_5173_URL = "https://5173.seongmin.dev";

    public static final List<String> ALLOWED_CLIENT_URLS = List.of(
            PROD_CLIENT_JOIN_URL,
            LOCAL_VITE_BUILD_CLIENT_URL,
            LOCAL_VITE_CLIENT_URL,
            CF_TUNNEL_4173_URL,
            CF_TUNNEL_5173_URL
    );
}
