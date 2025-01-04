package aegis.server.domain.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Department {
    SOFTWARE_ENGINEERING("SW융합대학 소프트웨어학과"),
    COMPUTER_ENGINEERING("SW융합대학 컴퓨터공학과"),
    MOBILE_SYSTEM_ENGINEERING("SW융합대학 모바일시스템공학과"),
    STATISTICS_DATA_SCIENCE("SW융합대학 통계데이터사이언스학과"),
    CYBER_SECURITY("SW융합대학 사이버보안학과"),
    SW_CONVERGENCE_DIVISION("SW융합대학 SW융합학부");

    private final String departmentName;

}
