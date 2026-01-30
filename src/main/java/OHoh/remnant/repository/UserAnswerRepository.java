package OHoh.remnant.repository;

import OHoh.remnant.domain.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    // 특정 세션의 모든 클릭 데이터를 가져옴
    List<UserAnswer> findBySessionId(Long sessionId);

    // 클릭 순서대로 정렬해서 가져와야 첫 클릭 지연이나 안정성 계산이 정확해짐
    List<UserAnswer> findBySessionIdOrderByClickOrderAsc(Long sessionId);
}
