package OHoh.remnant.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
public class UserAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private GameSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    private Integer clickOrder;
    private Double x; // 클릭한 x좌표
    private Double y; // 클릭한 y좌표
    private Double milisec; // 클릭까지 걸린 시간 (ms)
    private Boolean isCorrect; // 정답 여부
}
