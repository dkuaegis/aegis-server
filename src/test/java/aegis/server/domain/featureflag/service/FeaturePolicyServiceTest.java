package aegis.server.domain.featureflag.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.featureflag.dto.request.MemberSignupUpdateRequest;
import aegis.server.domain.featureflag.dto.request.StudyCreationUpdateRequest;
import aegis.server.domain.featureflag.dto.request.StudyEnrollWindowUpdateRequest;
import aegis.server.domain.featureflag.dto.response.FeaturePolicyResponse;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.*;

class FeaturePolicyServiceTest extends IntegrationTest {

    @Autowired
    FeaturePolicyService featurePolicyService;

    @Autowired
    AdminFeatureFlagService adminFeatureFlagService;

    @Autowired
    FeatureFlagService featureFlagService;

    @Autowired
    Clock clock;

    @BeforeEach
    void setUp() {
        featureFlagService.reloadCacheFromDatabase();
    }

    @Nested
    class 운영_정책_조회 {

        @Test
        void 플래그가_없으면_모두_허용하고_신청_기간은_비어_있다() {
            // when
            FeaturePolicyResponse response = featurePolicyService.getFeaturePolicy();

            // then
            assertTrue(response.signupAllowed());
            assertTrue(response.studyCreationAllowed());
            assertTrue(response.studyEnrollmentAllowed());
            assertNull(response.studyEnrollmentOpenAt());
            assertNull(response.studyEnrollmentCloseAt());
        }

        @Test
        void 닫힌_기능과_지난_신청_기간을_반영한다() {
            // given
            LocalDateTime now = LocalDateTime.now(clock);
            adminFeatureFlagService.updateMemberSignup(new MemberSignupUpdateRequest(false));
            adminFeatureFlagService.updateStudyCreation(new StudyCreationUpdateRequest(false));
            adminFeatureFlagService.updateStudyEnrollWindow(
                    new StudyEnrollWindowUpdateRequest(now.minusDays(2), now.minusDays(1)));

            // when
            FeaturePolicyResponse response = featurePolicyService.getFeaturePolicy();

            // then
            assertFalse(response.signupAllowed());
            assertFalse(response.studyCreationAllowed());
            assertFalse(response.studyEnrollmentAllowed());
            assertEquals(now.minusDays(2), response.studyEnrollmentOpenAt());
            assertEquals(now.minusDays(1), response.studyEnrollmentCloseAt());
        }
    }
}
