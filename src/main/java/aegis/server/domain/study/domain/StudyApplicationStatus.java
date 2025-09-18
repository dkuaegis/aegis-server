package aegis.server.domain.study.domain;

public enum StudyApplicationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    // 응답 전용: 지원서 미제출 상태 표현(엔티티 저장 금지)
    NONE,
}
