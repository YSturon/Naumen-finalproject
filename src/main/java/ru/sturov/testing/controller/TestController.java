package ru.sturov.testing.controller;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import ru.sturov.testing.domain.KnowledgeTest;
import ru.sturov.testing.domain.Question;
import ru.sturov.testing.domain.TestAttempt;
import ru.sturov.testing.repository.KnowledgeTestRepository;
import ru.sturov.testing.repository.TestAttemptRepository;
import ru.sturov.testing.repository.TopicRepository;
import ru.sturov.testing.service.TestTakingService;

/**
 * Пользовательский контроллер тестов и попыток.
 *
 * <p>Обслуживает каталог тестов, просмотр теста, начало прохождения,
 * отправку ответов, результат и историю попыток текущего пользователя.</p>
 */
@Controller
public class TestController {

    private final TopicRepository topicRepository;
    private final KnowledgeTestRepository testRepository;
    private final TestAttemptRepository attemptRepository;
    private final TestTakingService testTakingService;

    public TestController(
            TopicRepository topicRepository,
            KnowledgeTestRepository testRepository,
            TestAttemptRepository attemptRepository,
            TestTakingService testTakingService
    ) {
        this.topicRepository = topicRepository;
        this.testRepository = testRepository;
        this.attemptRepository = attemptRepository;
        this.testTakingService = testTakingService;
    }

    /**
     * Показывает список опубликованных тестов.
     *
     * @param topicId необязательный фильтр по теме
     * @param model модель страницы
     * @return имя шаблона каталога тестов
     */
    @GetMapping("/tests")
    public String tests(@RequestParam(name = "topic", required = false) Long topicId, Model model) {
        List<KnowledgeTest> tests = topicId == null
                ? testRepository.findPublishedWithTopicAndQuestions()
                : testRepository.findPublishedWithTopicAndQuestionsByTopicId(topicId);

        model.addAttribute("topics", topicRepository.findAllByOrderByNameAsc());
        model.addAttribute("tests", tests);
        model.addAttribute("selectedTopicId", topicId);
        return "tests/list";
    }

    /**
     * Показывает краткую информацию об опубликованном тесте.
     *
     * @param id идентификатор теста
     * @param model модель страницы
     * @return имя шаблона карточки теста
     */
    @GetMapping("/tests/{id}")
    public String testDetails(@PathVariable Long id, Model model) {
        KnowledgeTest test = testRepository.findWithTopicAndQuestionsById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Тест не найден"));
        if (!test.isPublished()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Тест не опубликован");
        }
        model.addAttribute("test", test);
        return "tests/detail";
    }

    /**
     * Создает попытку и перенаправляет пользователя на страницу прохождения.
     *
     * @param id идентификатор теста
     * @param principal текущий пользователь
     * @return redirect на созданную попытку
     */
    @PostMapping("/tests/{id}/start")
    public String start(@PathVariable Long id, Principal principal) {
        TestAttempt attempt = testTakingService.startAttempt(id, principal.getName());
        return "redirect:/attempts/" + attempt.getId();
    }

    /**
     * Показывает страницу прохождения теста.
     *
     * <p>Если попытка уже завершена или время истекло, пользователь
     * перенаправляется на страницу результата.</p>
     *
     * @param id идентификатор попытки
     * @param principal текущий пользователь
     * @param model модель страницы
     * @return имя шаблона прохождения или redirect на результат
     */
    @GetMapping("/attempts/{id}")
    public String attempt(@PathVariable Long id, Principal principal, Model model) {
        TestAttempt attempt = testTakingService.getAttemptForCurrentUser(id, principal.getName());
        if (attempt.isFinished()) {
            return "redirect:/attempts/" + id + "/result";
        }
        if (LocalDateTime.now().isAfter(attempt.getDeadline())) {
            testTakingService.submitAttempt(id, principal.getName(), Map.of());
            return "redirect:/attempts/" + id + "/result";
        }

        List<Question> questions = testTakingService.getQuestionsForAttempt(attempt);
        long secondsRemaining = Math.max(0, Duration.between(LocalDateTime.now(), attempt.getDeadline()).toSeconds());

        model.addAttribute("attempt", attempt);
        model.addAttribute("questions", questions);
        model.addAttribute("secondsRemaining", secondsRemaining);
        return "attempts/take";
    }

    /**
     * Принимает ответы пользователя и завершает попытку.
     *
     * @param id идентификатор попытки
     * @param params параметры HTML-формы с выбранными вариантами
     * @param principal текущий пользователь
     * @return redirect на страницу результата
     */
    @PostMapping("/attempts/{id}/submit")
    public String submit(
            @PathVariable Long id,
            @RequestParam MultiValueMap<String, String> params,
            Principal principal
    ) {
        testTakingService.submitAttempt(id, principal.getName(), parseAnswers(params));
        return "redirect:/attempts/" + id + "/result";
    }

    /**
     * Показывает итоговый результат попытки.
     *
     * @param id идентификатор попытки
     * @param principal текущий пользователь
     * @param model модель страницы
     * @return имя шаблона результата
     */
    @GetMapping("/attempts/{id}/result")
    public String result(@PathVariable Long id, Principal principal, Model model) {
        TestAttempt attempt = testTakingService.getAttemptDetailsForCurrentUser(id, principal.getName());
        model.addAttribute("attempt", attempt);
        return "attempts/result";
    }

    /**
     * Показывает историю попыток текущего пользователя.
     *
     * @param principal текущий пользователь
     * @param model модель страницы
     * @return имя шаблона истории
     */
    @GetMapping("/history")
    public String history(Principal principal, Model model) {
        model.addAttribute("attempts", attemptRepository.findByUserUsernameIgnoreCaseOrderByStartedAtDesc(principal.getName()));
        return "attempts/history";
    }

    /**
     * Показывает детали выбранной попытки текущего пользователя.
     *
     * @param id идентификатор попытки
     * @param principal текущий пользователь
     * @param model модель страницы
     * @return имя шаблона деталей попытки
     */
    @GetMapping("/history/{id}")
    public String historyDetails(@PathVariable Long id, Principal principal, Model model) {
        TestAttempt attempt = testTakingService.getAttemptDetailsForCurrentUser(id, principal.getName());
        model.addAttribute("attempt", attempt);
        return "attempts/detail";
    }

    /**
     * Преобразует параметры HTML-формы в структуру выбранных ответов.
     *
     * @param params параметры запроса вида {@code answers[questionId]=optionId}
     * @return выбранные варианты, сгруппированные по вопросам
     */
    private Map<Long, List<Long>> parseAnswers(MultiValueMap<String, String> params) {
        Map<Long, List<Long>> result = new HashMap<>();
        params.forEach((key, values) -> {
            if (!key.startsWith("answers[") || !key.endsWith("]")) {
                return;
            }
            Long questionId = Long.valueOf(key.substring("answers[".length(), key.length() - 1));
            List<Long> optionIds = values.stream()
                    .filter(value -> value != null && !value.isBlank())
                    .map(Long::valueOf)
                    .toList();
            result.put(questionId, optionIds);
        });
        return result;
    }
}
