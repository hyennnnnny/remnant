package OHoh.remnant.service;

import OHoh.remnant.domain.GameResult;
import OHoh.remnant.domain.User;
import OHoh.remnant.dto.response.FamilyLoginResult;
import OHoh.remnant.dto.response.FamilyReportResponse;
import OHoh.remnant.repository.GameResultRepository;
import OHoh.remnant.repository.GameSessionRepository;
import OHoh.remnant.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FamilyService {

    private final UserRepository userRepository;
    private final GameSessionRepository gameSessionRepository;
    private final GameResultRepository gameResultRepository;

    public FamilyLoginResult login(Long guardianId) {
        // 1. User 테이블에서 해당 guardianId를 가진 어르신을 바로 찾음
        User targetUser = userRepository.findByGuardianId(guardianId)
                .orElseThrow(() -> new RuntimeException("해당 보호자와 매칭된 어르신이 없습니다."));

        // 2. 어르신의 게임 기록이 있는 '월' 목록 가져오기 (기존 쿼리 그대로 사용)
        List<Integer> reportMonths = gameSessionRepository.findDistinctMonthsByUserId(targetUser.getId());

        // 3. 결과 반환
        return FamilyLoginResult.builder()
                .userId(targetUser.getId())
                .lists(reportMonths)
                .build();
    }

    public FamilyReportResponse getMonthlyReport(Long userId, int month) {
        // 1. 기간 설정 (데모용으로 2026년 고정)
        LocalDateTime start = LocalDateTime.of(2026, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusNanos(1);

        LocalDateTime prevStart = start.minusMonths(1);
        LocalDateTime prevEnd = start.minusNanos(1);

        // 2. 이번 달 & 지난달 GameResult 리스트 가져오기
        List<GameResult> currentResults = gameResultRepository.findAllByUserIdAndMonth(userId, start, end);
        List<GameResult> previousResults = gameResultRepository.findAllByUserIdAndMonth(userId, prevStart, prevEnd);

        // 3. 레이더 차트용 지표 평균 계산
        FamilyReportResponse.RadarData currentRadar = currentResults.isEmpty() ? null : calculateAvgRadar(currentResults);
        FamilyReportResponse.RadarData previousRadar = previousResults.isEmpty() ? null : calculateAvgRadar(previousResults);

        // 4. 종합 점수 및 위험도 계산
        int currentAvg = (int) currentResults.stream().mapToInt(GameResult::getTotalScore).average().orElse(0);
        int prevAvg = (int) previousResults.stream().mapToInt(GameResult::getTotalScore).average().orElse(0);

        // 5. 가장 취약한 지표를 찾아 코멘트 생성
        String analysisComment = generateAnalysisComment(currentRadar);

        return FamilyReportResponse.builder()
                .targetMonth("2026-" + String.format("%02d", month))
                .totalPlayCount((long) currentResults.size())
                .summary(FamilyReportResponse.Summary.builder()
                        .avgTotalScore(currentAvg)
                        .scoreChange(currentAvg - prevAvg)
                        .riskLevel(determineRiskLevel(currentAvg))
                        .comment(analysisComment)
                        .build())
                .currentRadar(currentRadar)
                .previousRadar(previousRadar)
                .scoreTrend(currentResults.stream()
                        .sorted(Comparator.comparing(GameResult::getCreatedAt))
                        .map(GameResult::getTotalScore)
                        .collect(Collectors.toList()))
                .build();
    }

    private FamilyReportResponse.RadarData calculateAvgRadar(List<GameResult> results) {
        if (results.isEmpty()) {
            return FamilyReportResponse.RadarData.builder().accuracy(0).stability(0).execution(0).inhibition(0).efficiency(0).build();
        }
        return FamilyReportResponse.RadarData.builder()
                .accuracy((int) results.stream().mapToInt(GameResult::getAccuracyScore).average().orElse(0))
                .stability((int) results.stream().mapToInt(GameResult::getStabilityScore).average().orElse(0))
                .execution((int) results.stream().mapToInt(GameResult::getExecutionScore).average().orElse(0))
                .inhibition((int) results.stream().mapToInt(GameResult::getInhibitionScore).average().orElse(0))
                .efficiency((int) results.stream().mapToInt(GameResult::getEfficiencyScore).average().orElse(0))
                .build();
    }

    private int determineRiskLevel(int score) {
        if (score >= 70) return 0; // 정상
        if (score >= 55) return 1; // 주의
        return 2; // 위험
    }

    private String generateAnalysisComment(FamilyReportResponse.RadarData radar) {
        // 가장 낮은 점수 지표를 찾아 맞춤형 코멘트 생성
        if (radar.getExecution() < 60) return "전반적인 기능은 양호하나, 새로운 과제 시작 시 실행 지연이 반복됩니다.";
        if (radar.getStability() < 60) return "인지 반응의 기복이 심해 인지 피로도가 높아 보입니다. 충분한 휴식이 필요합니다.";
        if (radar.getInhibition() < 60) return "확인한 곳을 반복 클릭하는 경향이 있어 주의 집중력 저하가 의심됩니다.";
        return "모든 인지 지표가 균형 있게 유지되고 있습니다. 현재처럼 꾸준한 활동을 추천합니다.";
    }
}