package aegis.server.domain.featureflag.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.featureflag.domain.FeatureFlag;
import aegis.server.domain.featureflag.dto.request.StudyEnrollWindowUpdateRequest;
import aegis.server.domain.featureflag.dto.response.AdminFeatureFlagsResponse;
import aegis.server.domain.featureflag.repository.FeatureFlagRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.*;

class AdminFeatureFlagServiceTest extends IntegrationTest {

    @Autowired
    AdminFeatureFlagService adminFeatureFlagService;

    @Autowired
    FeaturePolicyService featurePolicyService;

    @Autowired
    FeatureFlagService featureFlagService;

    @Autowired
    FeatureFlagRepository featureFlagRepository;

    @Autowired
    Clock clock;

    @BeforeEach
    void setUp() {
        featureFlagService.reloadCacheFromDatabase();
    }

    @Nested
    class 스터디_신청_기간_플래그_수정 {

        @Test
        void 성공한다() {
            // given
            LocalDateTime now = LocalDateTime.now(clock);
            StudyEnrollWindowUpdateRequest request =
                    new StudyEnrollWindowUpdateRequest(now.minusMinutes(5), now.plusMinutes(5));

            // when
            AdminFeatureFlagsResponse response = adminFeatureFlagService.updateStudyEnrollWindow(request);

            // then
            // 반환값 검증
            assertTrue(response.studyEnrollWindow().valid());
            assertTrue(response.studyEnrollWindow().enrollmentAllowedNow());
            assertEquals(request.openAt(), response.studyEnrollWindow().openAt());
            assertEquals(request.closeAt(), response.studyEnrollWindow().closeAt());

            // DB 상태 검증
            FeatureFlag openFlag = featureFlagRepository
                    .findById(response.studyEnrollWindow().openFlagId())
                    .orElseThrow();
            FeatureFlag closeFlag = featureFlagRepository
                    .findById(response.studyEnrollWindow().closeFlagId())
                    .orElseThrow();
            assertEquals(request.openAt().toString(), openFlag.getValue());
            assertEquals(request.closeAt().toString(), closeFlag.getValue());

            assertTrue(featurePolicyService.isStudyEnrollmentAllowed());
        }

        @Test
        void 종료일시가_시작일시보다_빠르면_실패한다() {
            // given
            LocalDateTime now = LocalDateTime.now(clock);
            StudyEnrollWindowUpdateRequest request =
                    new StudyEnrollWindowUpdateRequest(now.plusMinutes(1), now.minusMinutes(1));

            // when & then
            CustomException exception =
                    assertThrows(CustomException.class, () -> adminFeatureFlagService.updateStudyEnrollWindow(request));
            assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        }
    }

    @Test
    void 플래그가_없으면_기본정책은_허용이다() {
        // when
        AdminFeatureFlagsResponse response = adminFeatureFlagService.getFeatureFlags();

        // then
        assertTrue(featurePolicyService.isStudyEnrollmentAllowed());
        assertFalse(response.studyEnrollWindow().valid());
    }
}
