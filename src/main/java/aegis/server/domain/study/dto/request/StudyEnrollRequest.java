package aegis.server.domain.study.dto.request;

import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

public record StudyEnrollRequest(
        @Schema(description = "지원 이유. 지원서 방식 스터디에서만 필요하며 선착순 스터디는 null을 보냅니다.", nullable = true) @Size(max = 500) String applicationReason) {}
