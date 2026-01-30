package OHoh.remnant.controller;

import OHoh.remnant.dto.request.UserAnswerRequest;
import OHoh.remnant.dto.request.UserLoginRequest;
import OHoh.remnant.dto.response.BaseResponse;
import OHoh.remnant.dto.response.UserLoginResult;
import OHoh.remnant.dto.response.UserResultResponse;
import OHoh.remnant.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 로그인
    @PostMapping("/login")
    public BaseResponse<UserLoginResult> login(@RequestBody UserLoginRequest request) {
        UserLoginResult result = userService.login(request);

        return new BaseResponse<>("로그인 성공", result);
    }

    // 답변 저장
    @PostMapping("/{sessionId}/{questionId}/answers")
    public BaseResponse<String> saveUserAnswers(
            @PathVariable Long sessionId,
            @PathVariable Long questionId,
            @RequestBody UserAnswerRequest request) {

        userService.saveUserAnswers(sessionId, questionId, request);
        return new BaseResponse<>("답변 저장 및 판정 완료", null);
    }

    // 결과 분석
    @GetMapping("/{sessionId}/result")
    public BaseResponse<UserResultResponse> getUserResult(@PathVariable Long sessionId) {
        UserResultResponse result = userService.getUserResult(sessionId);

        return new BaseResponse<>("게임 결과 분석 완료", result);
    }

}
