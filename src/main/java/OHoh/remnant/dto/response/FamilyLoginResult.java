package OHoh.remnant.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FamilyLoginResult {
    private Long userId;        // 매칭된 어르신 ID
    private List<Integer> lists; // 세션이 존재하는 월 목록 (예: [1, 2, 3])
}
