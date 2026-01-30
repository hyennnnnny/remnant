package OHoh.remnant.service;

import OHoh.remnant.domain.AnswerKey;
import OHoh.remnant.domain.Question;
import OHoh.remnant.dto.request.StaffAnswerRequest;
import OHoh.remnant.dto.response.StaffAnswerResult;
import OHoh.remnant.repository.AnswerKeyRepository;
import OHoh.remnant.repository.QuestionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StaffService {

    private final QuestionRepository questionRepository;
    private final AnswerKeyRepository answerKeyRepository;

    public StaffAnswerResult saveAnswers(StaffAnswerRequest request) {
        // 1. 새로운 문제(Question) 생성 및 저장
        // (이미지 경로나 난이도는 나중에 추가 가능하도록 기본값 세팅)
        Question question = new Question();
        question.setLevel(1);
        Question savedQuestion = questionRepository.save(question);

        // 2. 받은 정답 좌표들을 AnswerKey로 변환하여 저장
        List<AnswerKey> answerKeys = request.getAnswer().stream()
                .map(detail -> {
                    AnswerKey answerKey = new AnswerKey();
                    answerKey.setQuestion(savedQuestion);
                    answerKey.setX(detail.getX());
                    answerKey.setY(detail.getY());
                    return answerKey;
                })
                .collect(Collectors.toList());

        answerKeyRepository.saveAll(answerKeys);

        // 3. 생성된 문제 ID 반환
        return new StaffAnswerResult(savedQuestion.getId());
    }
}
