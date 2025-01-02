package aegis.server.domain.survey.domain;

import aegis.server.domain.member.domain.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Survey {

    @Id
    @GeneratedValue
    @Column(name="survey_id")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name="member_id")
    private Member member;

    @ElementCollection
    @CollectionTable(name = "interests", joinColumns = @JoinColumn(name = "survey_id"))
    @Enumerated(EnumType.STRING)
    private Set<InterestField> interestFields = new HashSet<>();


    @Size(min = 3)
    private String registrationReason;
    private String feedBack;

    @CreatedDate
    private LocalDateTime createdAt;


    public Survey(Member member, Set<InterestField> interestFields, String feedBack, String registrationReason) {
        this.member = member;
        this.interestFields = interestFields;
        this.feedBack = feedBack;
        this.registrationReason = registrationReason;
    }

}
