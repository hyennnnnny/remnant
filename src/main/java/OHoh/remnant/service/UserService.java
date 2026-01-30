package OHoh.remnant.service;

import OHoh.remnant.domain.*;
import OHoh.remnant.dto.request.UserAnswerRequest;
import OHoh.remnant.dto.request.UserLoginRequest;
import OHoh.remnant.dto.response.UserLoginResult;
import OHoh.remnant.dto.response.UserResultResponse;
import OHoh.remnant.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final GameSessionRepository gameSessionRepository;
    private final QuestionRepository questionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final AnswerKeyRepository answerKeyRepository;
    private final GameResultRepository gameResultRepository;

    private static final double AGE_CORRECTION_FACTOR = 1.8; // 70대 보정 계수 (\alpha)

    // 1. 정확도 (Accuracy): 높을수록 좋음
    private static final double REF_20S_ACC_MEAN = 98.0;
    private static final double REF_20S_ACC_SD = 2.0;

    // 2. 안정성 (Stability/SD): 낮을수록 좋음
    private static final double REF_20S_STAB_MEAN = 250.0;
    private static final double REF_20S_STAB_SD = 50.0;

    // 3. 실행력 (Execution/Latency): 낮을수록 좋음
    private static final double REF_20S_EXEC_MEAN = 1200.0;
    private static final double REF_20S_EXEC_SD = 300.0;

    // 4. 억제력 (Inhibition/Duplicates): 낮을수록 좋음
    private static final double REF_20S_INHIB_MEAN = 0.05;
    private static final double REF_20S_INHIB_SD = 0.02;

    // 5. 효율성 (Efficiency/Path): 높을수록 좋음
    private static final double REF_20S_EFFI_MEAN = 95.0;
    private static final double REF_20S_EFFI_SD = 5.0;

    public UserLoginResult login(UserLoginRequest request) {
        // 1. 이름으로 기존 유저 조회, 없으면 새로 저장
        User user = userRepository.findByName(request.getName())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setName(request.getName());
                    newUser.setGuardianId(100L);
                    return userRepository.save(newUser);
                });

        // 2. 새로운 게임 세션 시작
        GameSession session = new GameSession();
        session.setUser(user);
        session.setDifficultyLevel(request.getLevel());
        gameSessionRepository.save(session);

        // 3. 결과 반환
        return UserLoginResult.builder()
                .userId(user.getId())
                .name(user.getName())
                .sessionId(session.getId())
                .build();
    }

    // 2. 답변 저장 및 판정 로직
    public void saveUserAnswers(Long sessionId, Long questionId, UserAnswerRequest request) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없습니다."));
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("문제를 찾을 수 없습니다."));

        // 난이도별 판정 반경 설정
        // 1단계: 0.1 (10%), 2단계: 0.05 (5%), 3단계: 0.03 (3%)
        double threshold;
        switch (session.getDifficultyLevel()) {
            case 1:  threshold = 0.1;  break;
            case 2:  threshold = 0.05; break;
            case 3:  threshold = 0.03; break;
            default: threshold = 0.05; // 기본값
        }

        // 해당 문제의 실제 정답들 가져오기
        List<AnswerKey> correctAnswers = answerKeyRepository.findByQuestionId(questionId);

        List<UserAnswer> userAnswers = request.getAnswer().stream()
                .map(detail -> {
                    UserAnswer ua = new UserAnswer();
                    ua.setSession(session);
                    ua.setQuestion(question);
                    ua.setClickOrder(detail.getClickOrder());
                    ua.setMilisec(detail.getMilisec());
                    ua.setX(detail.getX());
                    ua.setY(detail.getY());

                    // 유클리드 거리 공식 적용: d = \sqrt{(x_1 - x_2)^2 + (y_1 - y_2)^2}
                    boolean correct = correctAnswers.stream().anyMatch(key ->
                            Math.sqrt(Math.pow(key.getX() - detail.getX(), 2) +
                                    Math.pow(key.getY() - detail.getY(), 2)) < threshold
                    );

                    ua.setIsCorrect(correct);
                    return ua;
                })
                .collect(Collectors.toList());

        userAnswerRepository.saveAll(userAnswers);
    }

    public UserResultResponse getUserResult(Long sessionId) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없습니다."));
        List<UserAnswer> answers = userAnswerRepository.findBySessionIdOrderByClickOrderAsc(sessionId);

        if (answers.isEmpty()) return createEmptyResponse();

        // 1. 각 지표별 원시 데이터(Raw Data) 추출
        double rawAcc = (answers.stream().filter(UserAnswer::getIsCorrect).count() / (double) answers.size()) * 100;
        double rawStab = calculateRawStabilityCorrected(answers);
        double rawExec = answers.stream()
                .filter(a -> a.getClickOrder() == 1)
                .mapToDouble(UserAnswer::getMilisec)
                .average()
                .orElse(answers.get(0).getMilisec());
        double rawInhib = calculateRawInhibition(answers);
        double rawEffi = rawAcc;

        // 2. Z-Score 기반 점수 산출 (20대 기준 -> 70대 보정 적용)
        double accScore = convertToStandardScore(rawAcc, REF_20S_ACC_MEAN, REF_20S_ACC_SD, true);
        double execScore = convertToStandardScore(rawExec, REF_20S_EXEC_MEAN * AGE_CORRECTION_FACTOR, REF_20S_EXEC_SD * AGE_CORRECTION_FACTOR, false);
        double stabScore = convertToStandardScore(rawStab, REF_20S_STAB_MEAN * AGE_CORRECTION_FACTOR, REF_20S_STAB_SD * AGE_CORRECTION_FACTOR, false);
        double inhibScore = convertToStandardScore(rawInhib, 0.05, 0.02, false);
        double effiScore = convertToStandardScore(rawEffi, 95.0, 5.0, true);
        // 3. 가중치 합산 (Total Score)
        int totalScore = (int) Math.round(
                (accScore * 0.30) + (stabScore * 0.25) + (execScore * 0.15) + (inhibScore * 0.20) + (effiScore * 0.10)
        );

        // 4. 결과 저장
        saveResult(session, totalScore, (int)accScore, (int)stabScore, (int)execScore, (int)inhibScore, (int)effiScore);

        return UserResultResponse.builder()
                .totalScore(totalScore)
                .agility((int) execScore)
                .judgment((int) accScore)
                .graph(getRecentScoreGraph(session.getUser()))
                .build();
    }

    /**
     * Z-Score를 기반으로 한 T-Score(100점 만점 변환) 산출 로직
     * 과학적 근거: 평균(Z=0)을 70점으로 설정, 표준편차 1단위당 10점 가감
     */
    private double convertToStandardScore(double raw, double mean, double sd, boolean higherIsBetter) {
        if (sd == 0) return 70.0;
        double zScore = (raw - mean) / sd;
        if (!higherIsBetter) zScore = -zScore; // 시간이 길거나 중복이 많으면 감점

        // T-Score 공식: 70 + (10 * Z)
        // 예: Z가 -1.5(MCI 경계선)일 때 -> 70 - 15 = 55점
        double tScore = 70 + (zScore * 10);
        return Math.max(0, Math.min(100, tScore));
    }

    private double calculateRawStabilityCorrected(List<UserAnswer> answers) {
        List<Double> intervals = new ArrayList<>();

        for (int i = 0; i < answers.size(); i++) {
            UserAnswer current = answers.get(i);

            if (current.getClickOrder() == 1) {
                // 문제의 첫 클릭은 그 자체의 latency를 간격으로 포함
                intervals.add(current.getMilisec());
            } else {
                // 같은 문제 내에서의 이전 클릭과의 차이 계산
                UserAnswer previous = answers.get(i - 1);
                if (current.getQuestion().getId().equals(previous.getQuestion().getId())) {
                    intervals.add(current.getMilisec() - previous.getMilisec());
                }
            }
        }

        if (intervals.isEmpty()) return REF_20S_STAB_MEAN * AGE_CORRECTION_FACTOR;

        double avg = intervals.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        return Math.sqrt(intervals.stream().mapToDouble(v -> Math.pow(v - avg, 2)).average().orElse(0.0));
    }

    private double calculateRawInhibition(List<UserAnswer> answers) {
        long duplicates = 0;
        for (int i = 1; i < answers.size(); i++) {
            UserAnswer c = answers.get(i), p = answers.get(i-1);
            if (Math.abs(c.getX()-p.getX()) < 0.01 && Math.abs(c.getY()-p.getY()) < 0.01) duplicates++;
        }
        return (double) duplicates / answers.size();
    }

    private long correctCount(List<UserAnswer> answers) {
        return answers.stream().filter(UserAnswer::getIsCorrect).count();
    }

    private void saveResult(GameSession s, int total, int acc, int stab, int exec, int inhib, int effi) {
        // 1. 기존 결과가 있으면 가져오고 없으면 새로 생성
        GameResult res = gameResultRepository.findBySessionId(s.getId()).orElse(new GameResult());

        res.setSession(s);
        res.setTotalScore(total);

        // 2. 5대 지표 개별 점수 저장
        res.setAccuracyScore(acc);
        res.setStabilityScore(stab);
        res.setExecutionScore(exec);
        res.setInhibitionScore(inhib);
        res.setEfficiencyScore(effi);

        // 3. 위험도 및 코멘트 로직 (여기서 미리 처리해서 저장)
        res.setRiskLevel(determineRiskLevel(total));
        res.setComment(generateComment(exec, stab));

        gameResultRepository.save(res);
    }

    // 위험도 판정 기준
    private int determineRiskLevel(int totalScore) {
        if (totalScore >= 70) return 0; // 정상
        if (totalScore >= 55) return 1; // 주의
        return 2; // 위험
    }

    // 코멘트 생성 기준
    private String generateComment(int exec, int stab) {
        if (exec < 60) return "새로운 과제에 대한 실행 지연이 관찰됩니다.";
        if (stab < 60) return "인지 반응의 기복이 관찰되니 주의가 필요합니다.";
        return "전반적으로 인지 건강 상태가 양호합니다.";
    }

    private List<Integer> getRecentScoreGraph(User user) {
        List<Integer> scores = gameResultRepository.findTop7BySessionUserOrderByCreatedAtDesc(user)
                .stream().map(GameResult::getTotalScore).collect(Collectors.toList());
        Collections.reverse(scores);
        return scores;
    }

    private UserResultResponse createEmptyResponse() {
        return UserResultResponse.builder().totalScore(0).agility(0).judgment(0).graph(new ArrayList<>()).build();
    }



}
