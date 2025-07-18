package aegis.server.global.constant;

import java.math.BigDecimal;
import java.util.List;

import aegis.server.domain.common.domain.YearSemester;

public class Constant {

    public static final BigDecimal CLUB_DUES = BigDecimal.valueOf(15000);

    public static final YearSemester CURRENT_YEAR_SEMESTER = YearSemester.YEAR_SEMESTER_2025_2;

    public static final String PROD_CLIENT_JOIN_URL = "https://join.dkuaegis.org";

    public static final String DEV_CLIENT_JOIN_URL = "https://dev-join.dkuaegis.org";
    public static final String DEV_1_CLIENT_JOIN_URL = "https://dev-1-join.dkuaegis.org";
    public static final String DEV_2_CLIENT_JOIN_URL = "https://dev-2-join.dkuaegis.org";
    public static final String DEV_3_CLIENT_JOIN_URL = "https://dev-3-join.dkuaegis.org";

    public static final String LOCAL_VITE_BUILD_CLIENT_URL = "http://localhost:4173";
    public static final String LOCAL_VITE_CLIENT_URL = "http://localhost:5173";
    public static final String LOCAL_SERVER_URL = "http://localhost:8080";

    // TODO: Aegis Cloudflare 계정으로 변경
    public static final String CF_TUNNEL_4173_URL = "https://4173.seongmin.dev";
    public static final String CF_TUNNEL_5173_URL = "https://5173.seongmin.dev";

    public static final List<String> ALLOWED_CLIENT_URLS = List.of(
            PROD_CLIENT_JOIN_URL,
            DEV_CLIENT_JOIN_URL,
            DEV_1_CLIENT_JOIN_URL,
            DEV_2_CLIENT_JOIN_URL,
            DEV_3_CLIENT_JOIN_URL,
            LOCAL_VITE_BUILD_CLIENT_URL,
            LOCAL_VITE_CLIENT_URL,
            LOCAL_SERVER_URL,
            CF_TUNNEL_4173_URL,
            CF_TUNNEL_5173_URL);
}
