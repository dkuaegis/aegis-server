package aegis.server.domain.study.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import aegis.server.domain.study.domain.Study;

public interface StudyRepository extends JpaRepository<Study, Long> {}
