package aegis.server.domain.survey.domain;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.survey.dto.SurveyRequest;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Survey {

    @Id
    @GeneratedValue
    @Column(name = "survey_id")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ElementCollection
    @CollectionTable(name = "interests", joinColumns = @JoinColumn(name = "survey_id"))
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<InterestField> interestFields = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "survey_interest_etc")
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "interest_field")
    @Column(name = "etc")
    @Builder.Default
    private Map<InterestField, String> interestEtc = new HashMap<>();

    @Column(length = 1000)
    private String registrationReason;

    @Column(length = 1000)
    private String feedBack;

    @CreatedDate
    private LocalDateTime createdAt;


    public void update(SurveyRequest surveyRequest) {
        this.interestFields = surveyRequest.getInterestFields();
        this.interestEtc = surveyRequest.getInterestEtc();
        this.registrationReason = surveyRequest.getRegistrationReason();
        this.feedBack = surveyRequest.getFeedBack();
    }

    public static Survey createSurvey(Member member, SurveyRequest request) {
        return Survey.builder()
                .member(member)
                .interestFields(request.getInterestFields())
                .interestEtc(request.getInterestEtc())
                .registrationReason(request.getRegistrationReason())
                .feedBack(request.getFeedBack())
                .build();
    }
}
