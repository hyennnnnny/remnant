package OHoh.remnant.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class UserAnswerRequest {
    private List<AnswerDetail> answer;

    @Getter
    @NoArgsConstructor
    public static class AnswerDetail {
        private Integer clickOrder;
        private Double milisec;
        private Double x;
        private Double y;
    }
}
