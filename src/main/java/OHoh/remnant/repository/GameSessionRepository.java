package OHoh.remnant.repository;

import OHoh.remnant.domain.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    @Query("SELECT DISTINCT FUNCTION('MONTH', gs.createdAt) " +
            "FROM GameSession gs WHERE gs.user.id = :userId " +
            "ORDER BY 1 ASC")
    List<Integer> findDistinctMonthsByUserId(@Param("userId") Long userId);
}
