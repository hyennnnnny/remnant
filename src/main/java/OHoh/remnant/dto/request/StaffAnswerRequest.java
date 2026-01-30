package OHoh.remnant.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class StaffAnswerRequest {
    private List<AnswerDetail> answer;

    @Getter
    @NoArgsConstructor
    public static class AnswerDetail {
        private Long id; // 프론트에서 임시로 붙인 ID
        private Double x;
        private Double y;
    }
}
