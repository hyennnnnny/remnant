package OHoh.remnant.repository;

import OHoh.remnant.domain.AnswerKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerKeyRepository extends JpaRepository<AnswerKey, Long> {
    // 특정 문제에 달린 모든 정답 좌표(x, y)를 가져와서 판정할 때 사용
    List<AnswerKey> findByQuestionId(Long questionId);
}
