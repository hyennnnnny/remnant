package OHoh.remnant.repository;

import OHoh.remnant.domain.GameResult;
import OHoh.remnant.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameResultRepository extends JpaRepository<GameResult, Long> {
    // 해당 세션에 이미 결과가 있는지 확인 (중복 저장 방지용)
    Optional<GameResult> findBySessionId(Long sessionId);

    // [그래프용] 특정 유저의 최근 결과 7개를 날짜 역순으로 조회
    // 세션(Session)을 타고 유저(User)를 조회하는 방식
    List<GameResult> findTop7BySessionUserOrderByCreatedAtDesc(User user);

    @Query("SELECT r FROM GameResult r WHERE r.session.user.id = :userId " +
            "AND r.createdAt BETWEEN :start AND :end")
    List<GameResult> findAllByUserIdAndMonth(@Param("userId") Long userId,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);
}
