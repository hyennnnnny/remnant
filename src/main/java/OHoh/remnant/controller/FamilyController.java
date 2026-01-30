package OHoh.remnant.controller;

import OHoh.remnant.dto.response.BaseResponse;
import OHoh.remnant.dto.response.FamilyLoginResult;
import OHoh.remnant.dto.response.FamilyReportResponse;
import OHoh.remnant.service.FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/family")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    @GetMapping("/login/{guardianId}")
    public BaseResponse<FamilyLoginResult> guardianLogin(@PathVariable Long guardianId) {
        FamilyLoginResult result = familyService.login(guardianId);

        return new BaseResponse<>("보호자 로그인 및 리포트 가능 월 목록 조회 성공", result);
    }

    @GetMapping("/{userId}/reports/{month}")
    public BaseResponse<FamilyReportResponse> getMonthlyReport(
            @PathVariable Long userId,
            @PathVariable int month) {

        FamilyReportResponse result = familyService.getMonthlyReport(userId, month);
        return new BaseResponse<>("월간 리포트 분석 완료", result);
    }
}
