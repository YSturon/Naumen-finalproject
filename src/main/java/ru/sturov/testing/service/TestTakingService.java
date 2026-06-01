package ru.sturov.testing.service;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.sturov.testing.domain.AnswerOption;
import ru.sturov.testing.domain.AttemptAnswer;
import ru.sturov.testing.domain.KnowledgeTest;
import ru.sturov.testing.domain.Question;
import ru.sturov.testing.domain.TestAttempt;
import ru.sturov.testing.domain.UserAccount;
import ru.sturov.testing.repository.KnowledgeTestRepository;
import ru.sturov.testing.repository.QuestionRepository;
import ru.sturov.testing.repository.TestAttemptRepository;
import ru.sturov.testing.repository.UserAccountRepository;

/**
 * Сервис прохождения тестов.
 *
 * <p>Отвечает за создание попытки, загрузку вопросов, проверку ответов,
 * расчет результата и контроль доступа пользователя к своим попыткам.</p>
 */
@Service
public class TestTakingService {

    private final KnowledgeTestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final TestAttemptRepository attemptRepository;
    private final UserAccountRepository userAccountRepository;

    public TestTakingService(
            KnowledgeTestRepository testRepository,
            QuestionRepository questionRepository,
            TestAttemptRepository attemptRepository,
            UserAccountRepository userAccountRepository
    ) {
        this.testRepository = testRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * Создает новую попытку прохождения опубликованного теста.
     *
     * @param testId идентификатор теста
     * @param username логин текущего пользователя
     * @return созданная попытка
     */
    @Transactional
    public TestAttempt startAttempt(Long testId, String username) {
        KnowledgeTest test = testRepository.findWithTopicAndQuestionsById(testId)
                .orElseThrow(() -> notFound("Тест не найден"));
        if (!test.isPublished()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Тест не опубликован");
        }
        if (test.getQuestions().isEmpty()) {
            throw new IllegalStateException("В тесте нет вопросов");
        }

        UserAccount user = userAccountRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> notFound("Пользователь не найден"));

        TestAttempt attempt = new TestAttempt();
        attempt.setUser(user);
        attempt.setTest(test);
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setMaxScore(test.getMaxScore());
        attempt.setTotalQuestions(test.getQuestionCount());
        return attemptRepository.save(attempt);
    }

    /**
     * Возвращает попытку текущего пользователя без детальной загрузки ответов.
     *
     * @param attemptId идентификатор попытки
     * @param username логин текущего пользователя
     * @return попытка пользователя
     */
    @Transactional(readOnly = true)
    public TestAttempt getAttemptForCurrentUser(Long attemptId, String username) {
        TestAttempt attempt = attemptRepository.findWithTestAndUserById(attemptId)
                .orElseThrow(() -> notFound("Попытка не найдена"));
        checkOwner(attempt, username);
        return attempt;
    }

    /**
     * Возвращает детальные данные попытки текущего пользователя.
     *
     * @param attemptId идентификатор попытки
     * @param username логин текущего пользователя
     * @return попытка с ответами и выбранными вариантами
     */
    @Transactional(readOnly = true)
    public TestAttempt getAttemptDetailsForCurrentUser(Long attemptId, String username) {
        TestAttempt attempt = attemptRepository.findDetailsById(attemptId)
                .orElseThrow(() -> notFound("Попытка не найдена"));
        checkOwner(attempt, username);
        return attempt;
    }

    /**
     * Возвращает детальные данные попытки для административного просмотра.
     *
     * @param attemptId идентификатор попытки
     * @return попытка с ответами и выбранными вариантами
     */
    @Transactional(readOnly = true)
    public TestAttempt getAttemptDetails(Long attemptId) {
        return attemptRepository.findDetailsById(attemptId)
                .orElseThrow(() -> notFound("Попытка не найдена"));
    }

    /**
     * Загружает вопросы для попытки.
     *
     * @param attempt попытка прохождения теста
     * @return вопросы теста вместе с вариантами ответов
     */
    @Transactional(readOnly = true)
    public List<Question> getQuestionsForAttempt(TestAttempt attempt) {
        return questionRepository.findByTestIdOrderByIdAsc(attempt.getTest().getId());
    }

    /**
     * Завершает попытку и рассчитывает результат.
     *
     * <p>Ответ считается правильным только тогда, когда выбранный набор вариантов
     * полностью совпадает с набором правильных вариантов.</p>
     *
     * @param attemptId идентификатор попытки
     * @param username логин текущего пользователя
     * @param selectedOptionIds выбранные варианты по идентификаторам вопросов
     * @return завершенная попытка с рассчитанными баллами и оценкой
     */
    @Transactional
    public TestAttempt submitAttempt(Long attemptId, String username, Map<Long, List<Long>> selectedOptionIds) {
        TestAttempt attempt = attemptRepository.findWithTestAndUserById(attemptId)
                .orElseThrow(() -> notFound("Попытка не найдена"));
        checkOwner(attempt, username);

        if (attempt.isFinished()) {
            return attempt;
        }

        List<Question> questions = questionRepository.findByTestIdOrderByIdAsc(attempt.getTest().getId());
        int score = 0;
        int correctAnswers = 0;

        attempt.getAnswers().clear();
        for (Question question : questions) {
            Set<Long> selectedIds = new LinkedHashSet<>(selectedOptionIds.getOrDefault(question.getId(), List.of()));
            Set<Long> correctIds = question.getOptions().stream()
                    .filter(AnswerOption::isCorrect)
                    .map(AnswerOption::getId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            Map<Long, AnswerOption> optionsById = question.getOptions().stream()
                    .collect(Collectors.toMap(AnswerOption::getId, Function.identity()));

            Set<AnswerOption> selectedOptions = selectedIds.stream()
                    .map(optionsById::get)
                    .filter(option -> option != null)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            boolean answerCorrect = !correctIds.isEmpty() && correctIds.equals(selectedIds);
            if (answerCorrect) {
                score += question.getPoints();
                correctAnswers++;
            }

            AttemptAnswer answer = new AttemptAnswer();
            answer.setAttempt(attempt);
            answer.setQuestion(question);
            answer.setSelectedOptions(selectedOptions);
            answer.setCorrect(answerCorrect);
            attempt.getAnswers().add(answer);
        }

        int maxScore = questions.stream().mapToInt(Question::getPoints).sum();
        int percent = maxScore == 0 ? 0 : (int) Math.round(score * 100.0 / maxScore);

        attempt.setFinishedAt(LocalDateTime.now());
        attempt.setScore(score);
        attempt.setMaxScore(maxScore);
        attempt.setCorrectAnswers(correctAnswers);
        attempt.setTotalQuestions(questions.size());
        attempt.setPercent(percent);
        attempt.setPassed(percent >= attempt.getTest().getPassingScorePercent());
        attempt.setGrade(toGrade(percent));

        return attemptRepository.save(attempt);
    }

    /**
     * Проверяет, что попытка принадлежит текущему пользователю.
     *
     * @param attempt проверяемая попытка
     * @param username логин текущего пользователя
     */
    private void checkOwner(TestAttempt attempt, String username) {
        if (!attempt.getUser().getUsername().equalsIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа к попытке");
        }
    }

    /**
     * Преобразует процент выполнения в школьную оценку.
     *
     * @param percent процент набранных баллов
     * @return оценка от {@code 2} до {@code 5}
     */
    private String toGrade(int percent) {
        if (percent >= 90) {
            return "5";
        }
        if (percent >= 75) {
            return "4";
        }
        if (percent >= 60) {
            return "3";
        }
        return "2";
    }

    /**
     * Создает исключение HTTP 404.
     *
     * @param message сообщение ошибки
     * @return исключение для Spring MVC
     */
    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
