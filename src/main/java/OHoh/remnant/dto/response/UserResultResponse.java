package OHoh.remnant.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserResultResponse {
    private Integer totalScore;
    private Integer agility;
    private Integer judgment;
    private List<Integer> graph;
}
