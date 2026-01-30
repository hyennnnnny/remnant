package OHoh.remnant.controller;

import OHoh.remnant.dto.request.StaffAnswerRequest;
import OHoh.remnant.dto.response.BaseResponse;
import OHoh.remnant.dto.response.StaffAnswerResult;
import OHoh.remnant.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @PostMapping("/answers")
    public BaseResponse<StaffAnswerResult> saveAnswers(@RequestBody StaffAnswerRequest request) {
        StaffAnswerResult result = staffService.saveAnswers(request);

        // 커스텀 BaseResponse 생성자 사용
        return new BaseResponse<>("정답 등록 성공", result);
    }
}
