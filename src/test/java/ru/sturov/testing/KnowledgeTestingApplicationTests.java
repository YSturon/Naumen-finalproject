package ru.sturov.testing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.sturov.testing.domain.AnswerOption;
import ru.sturov.testing.domain.KnowledgeTest;
import ru.sturov.testing.domain.Question;
import ru.sturov.testing.domain.TestAttempt;
import ru.sturov.testing.repository.KnowledgeTestRepository;
import ru.sturov.testing.service.TestTakingService;

/**
 * Интеграционные тесты приложения.
 *
 * <p>Проверяют, что Spring-контекст поднимается, демонстрационные данные
 * создаются, а пользователь может пройти тест и получить результат.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
class KnowledgeTestingApplicationTests {

    @Autowired
    private KnowledgeTestRepository testRepository;

    @Autowired
    private TestTakingService testTakingService;

    /**
     * Проверяет успешное прохождение демонстрационного теста с максимальным баллом.
     */
    @Test
    void userCanCompleteDemoTestWithMaximumScore() {
        KnowledgeTest test = testRepository.findPublishedWithTopicAndQuestions().getFirst();
        TestAttempt attempt = testTakingService.startAttempt(test.getId(), "user");
        List<Question> questions = testTakingService.getQuestionsForAttempt(attempt);

        Map<Long, List<Long>> correctAnswers = questions.stream()
                .collect(Collectors.toMap(
                        Question::getId,
                        question -> question.getOptions().stream()
                                .filter(AnswerOption::isCorrect)
                                .map(AnswerOption::getId)
                                .toList()
                ));

        TestAttempt result = testTakingService.submitAttempt(attempt.getId(), "user", correctAnswers);

        assertThat(result.isFinished()).isTrue();
        assertThat(result.getScore()).isEqualTo(result.getMaxScore());
        assertThat(result.getPercent()).isEqualTo(100);
        assertThat(result.getGrade()).isEqualTo("5");
        assertThat(result.isPassed()).isTrue();
    }
}
