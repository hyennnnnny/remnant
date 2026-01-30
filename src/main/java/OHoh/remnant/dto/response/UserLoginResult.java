package OHoh.remnant.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserLoginResult {
    private Long userId;
    private String name;
    private Long sessionId;
}
