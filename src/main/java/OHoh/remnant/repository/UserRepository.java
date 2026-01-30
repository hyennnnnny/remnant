package OHoh.remnant.repository;

import OHoh.remnant.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 이름이 PK 역할을 하므로 중복 체크 및 조회에 필수
    Optional<User> findByName(String name);
    Optional<User> findByGuardianId(Long guardianId);
}
