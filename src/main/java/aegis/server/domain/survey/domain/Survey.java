package aegis.server.domain.survey.domain;

import aegis.server.domain.member.domain.Member;
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
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Survey {

    @Id
    @GeneratedValue
    @Column(name="survey_id")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member member;

    @ElementCollection
    @CollectionTable(name = "interests", joinColumns = @JoinColumn(name = "survey_id"))
    @Enumerated(EnumType.STRING)
    private Set<InterestField> interestFields = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "survey_interest_etc")
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "interest_field")
    @Column(name = "etc")
    private Map<InterestField, String> interestEtc = new HashMap<>();

    private String registrationReason;
    private String feedBack;

    @CreatedDate
    private LocalDateTime createdAt;


    public void update(SurveyDto surveyDto) {
        this.interestFields = surveyDto.getInterestFields();
        this.interestEtc = surveyDto.getInterestEtc();
        this.registrationReason = surveyDto.getRegistrationReason();
        this.feedBack = surveyDto.getFeedBack();
        this.createdAt = LocalDateTime.now();
    }
}
