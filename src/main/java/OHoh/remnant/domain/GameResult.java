package OHoh.remnant.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
public class GameResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private GameSession session;

    private Integer totalScore;  // 총점
    // 5대 핵심 지표 점수 (T-Score)
    private Integer accuracyScore;   // 정확도 점수
    private Integer stabilityScore;  // 안정성 점수
    private Integer executionScore;  // 실행력 점수
    private Integer inhibitionScore; // 억제력 점수
    private Integer efficiencyScore; // 효율성 점수

    private Integer riskLevel;       // 위험도 (0: 정상, 1: 주의, 2: 위험)
    private String comment;          // 분석 코멘트

    private LocalDateTime createdAt = LocalDateTime.now();
}
