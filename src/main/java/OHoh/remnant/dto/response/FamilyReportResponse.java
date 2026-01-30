package OHoh.remnant.dto.response;

import lombok.Builder;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FamilyReportResponse {
    private String targetMonth;
    private Long totalPlayCount;
    private Summary summary;
    private RadarData currentRadar;
    private RadarData previousRadar;
    private List<Integer> scoreTrend;

    @Getter @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Summary {
        private Integer avgTotalScore;
        private Integer riskLevel; // 0:정상, 1:주의, 2:위험
        private Integer scoreChange;
        private String comment;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RadarData {
        private Integer accuracy;
        private Integer stability;
        private Integer execution;
        private Integer inhibition;
        private Integer efficiency;
    }
}
