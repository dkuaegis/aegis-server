package aegis.server.domain.survey.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import aegis.server.domain.member.domain.Student;
import aegis.server.domain.survey.domain.Survey;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
    Optional<Survey> findByStudent(Student student);

    List<Survey> findByStudentIn(List<Student> students);
}
