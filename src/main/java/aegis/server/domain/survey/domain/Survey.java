package aegis.server.domain.survey.domain;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.*;

import lombok.*;

import aegis.server.domain.common.domain.BaseEntity;
import aegis.server.domain.member.domain.Student;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Survey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "survey_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ElementCollection
    @CollectionTable(name = "interests", joinColumns = @JoinColumn(name = "survey_id"))
    @Enumerated(EnumType.STRING)
    private Set<Interest> interests;

    @ElementCollection
    @CollectionTable(name = "interests_etc")
    @MapKeyColumn(name = "interests")
    @MapKeyEnumerated(EnumType.STRING)
    private Map<Interest, String> interestsEtc;

    @Enumerated(EnumType.STRING)
    private AcquisitionType acquisitionType;

    @Column(length = 1000)
    private String joinReason;

    @Column(length = 1000)
    private String feedback;

    private static final Set<Interest> ALLOWED_ETC_INTERESTS = Arrays.stream(Interest.values())
            .filter(interest -> interest.name().endsWith("ETC"))
            .collect(Collectors.toSet());

    private static void validateEtcInterests(Set<Interest> interests, Map<Interest, String> interestsEtc) {
        if (interestsEtc != null && !interestsEtc.isEmpty()) {
            for (Interest key : interestsEtc.keySet()) {
                // 1. interestEtc에 ETC Enum이 아닌 일반 Enum이 들어온 경우
                if (!ALLOWED_ETC_INTERESTS.contains(key)) {
                    throw new CustomException(ErrorCode.INVALID_INTEREST);
                }
                // 2. interestsEtc에는 존재하지만 interests에는 존재하지 않는 경우
                if (interests == null || !interests.contains(key)) {
                    throw new CustomException(ErrorCode.ETC_INTEREST_NOT_FOUND);
                }
            }
        }
    }

    public static Survey create(
            Student student,
            Set<Interest> interests,
            Map<Interest, String> interestsEtc,
            AcquisitionType acquisitionType,
            String joinReason,
            String feedback
    ) {
        validateEtcInterests(interests, interestsEtc);

        return Survey.builder()
                .student(student)
                .interests(interests)
                .interestsEtc(interestsEtc)
                .acquisitionType(acquisitionType)
                .joinReason(joinReason)
                .feedback(feedback)
                .build();
    }

    public void update(
            Set<Interest> interests,
            Map<Interest, String> interestsEtc,
            AcquisitionType acquisitionType,
            String joinReason,
            String feedback
    ) {
        validateEtcInterests(interests, interestsEtc);

        this.interests = interests;
        this.interestsEtc = interestsEtc;
        this.acquisitionType = acquisitionType;
        this.joinReason = joinReason;
        this.feedback = feedback;
    }
}
