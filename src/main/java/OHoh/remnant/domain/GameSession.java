package OHoh.remnant.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class GameSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Integer difficultyLevel; // 사용자가 선택한 난이도
    private LocalDateTime createdAt = LocalDateTime.now();

    // GameResult와 1:1 관계 (양방향)
    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL)
    private GameResult gameResult;
}
