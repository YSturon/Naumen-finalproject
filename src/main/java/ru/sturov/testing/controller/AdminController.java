package ru.sturov.testing.controller;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.sturov.testing.domain.AnswerOption;
import ru.sturov.testing.domain.KnowledgeTest;
import ru.sturov.testing.domain.Question;
import ru.sturov.testing.domain.QuestionType;
import ru.sturov.testing.domain.Topic;
import ru.sturov.testing.dto.QuestionForm;
import ru.sturov.testing.dto.TestForm;
import ru.sturov.testing.dto.TopicForm;
import ru.sturov.testing.repository.KnowledgeTestRepository;
import ru.sturov.testing.repository.QuestionRepository;
import ru.sturov.testing.repository.TestAttemptRepository;
import ru.sturov.testing.repository.TopicRepository;
import ru.sturov.testing.repository.UserAccountRepository;
import ru.sturov.testing.service.StatisticsService;
import ru.sturov.testing.service.TestTakingService;

/**
 * Контроллер административного раздела.
 *
 * <p>Предоставляет администратору управление темами, тестами, вопросами,
 * публикацией тестов, пользователями, результатами и статистикой.</p>
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final TopicRepository topicRepository;
    private final KnowledgeTestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final UserAccountRepository userRepository;
    private final TestAttemptRepository attemptRepository;
    private final StatisticsService statisticsService;
    private final TestTakingService testTakingService;

    public AdminController(
            TopicRepository topicRepository,
            KnowledgeTestRepository testRepository,
            QuestionRepository questionRepository,
            UserAccountRepository userRepository,
            TestAttemptRepository attemptRepository,
            StatisticsService statisticsService,
            TestTakingService testTakingService
    ) {
        this.topicRepository = topicRepository;
        this.testRepository = testRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.attemptRepository = attemptRepository;
        this.statisticsService = statisticsService;
        this.testTakingService = testTakingService;
    }

    /**
     * Добавляет в модель список типов вопросов для всех административных форм.
     *
     * @return доступные типы вопросов
     */
    @ModelAttribute("questionTypes")
    public QuestionType[] questionTypes() {
        return QuestionType.values();
    }

    /**
     * Показывает главную страницу административной панели.
     *
     * @param model модель страницы
     * @return имя шаблона панели администратора
     */
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("topicsCount", topicRepository.count());
        model.addAttribute("testsCount", testRepository.count());
        model.addAttribute("publishedTestsCount", testRepository.countByPublishedTrue());
        model.addAttribute("usersCount", userRepository.count());
        model.addAttribute("attemptsCount", attemptRepository.countByFinishedAtIsNotNull());
        return "admin/index";
    }

    /**
     * Показывает список тем.
     *
     * @param model модель страницы
     * @return имя шаблона списка тем
     */
    @GetMapping("/topics")
    public String topics(Model model) {
        model.addAttribute("topics", topicRepository.findAllByOrderByNameAsc());
        return "admin/topics";
    }

    /**
     * Показывает форму создания новой темы.
     *
     * @param model модель страницы
     * @return имя шаблона формы темы
     */
    @GetMapping("/topics/new")
    public String newTopic(Model model) {
        model.addAttribute("topicForm", new TopicForm());
        model.addAttribute("mode", "create");
        return "admin/topic-form";
    }

    /**
     * Создает новую тему.
     *
     * @param topicForm данные формы темы
     * @param bindingResult результат валидации формы
     * @param model модель страницы
     * @return redirect на список тем или форма с ошибками
     */
    @PostMapping("/topics")
    public String createTopic(
            @Valid @ModelAttribute TopicForm topicForm,
            BindingResult bindingResult,
            Model model
    ) {
        if (topicRepository.existsByNameIgnoreCase(topicForm.getName())) {
            bindingResult.rejectValue("name", "topic.exists", "Тема с таким названием уже существует");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "create");
            return "admin/topic-form";
        }

        Topic topic = new Topic();
        applyTopicForm(topic, topicForm);
        topicRepository.save(topic);
        return "redirect:/admin/topics";
    }

    /**
     * Показывает форму редактирования темы.
     *
     * @param id идентификатор темы
     * @param model модель страницы
     * @return имя шаблона формы темы
     */
    @GetMapping("/topics/{id}/edit")
    public String editTopic(@PathVariable Long id, Model model) {
        Topic topic = getTopic(id);
        model.addAttribute("topicForm", toTopicForm(topic));
        model.addAttribute("topic", topic);
        model.addAttribute("mode", "edit");
        return "admin/topic-form";
    }

    /**
     * Обновляет существующую тему.
     *
     * @param id идентификатор темы
     * @param topicForm данные формы темы
     * @param bindingResult результат валидации формы
     * @param model модель страницы
     * @return redirect на список тем или форма с ошибками
     */
    @PostMapping("/topics/{id}")
    public String updateTopic(
            @PathVariable Long id,
            @Valid @ModelAttribute TopicForm topicForm,
            BindingResult bindingResult,
            Model model
    ) {
        Topic topic = getTopic(id);
        if (!topic.getName().equalsIgnoreCase(topicForm.getName())
                && topicRepository.existsByNameIgnoreCase(topicForm.getName())) {
            bindingResult.rejectValue("name", "topic.exists", "Тема с таким названием уже существует");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("topic", topic);
            model.addAttribute("mode", "edit");
            return "admin/topic-form";
        }

        applyTopicForm(topic, topicForm);
        topicRepository.save(topic);
        return "redirect:/admin/topics";
    }

    /**
     * Удаляет тему, если к ней не привязаны тесты.
     *
     * @param id идентификатор темы
     * @param redirectAttributes flash-атрибуты для сообщения об ошибке
     * @return redirect на список тем
     */
    @PostMapping("/topics/{id}/delete")
    public String deleteTopic(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            topicRepository.deleteById(id);
        } catch (DataIntegrityViolationException exception) {
            redirectAttributes.addFlashAttribute("error", "Нельзя удалить тему, пока к ней привязаны тесты");
        }
        return "redirect:/admin/topics";
    }

    /**
     * Показывает список всех тестов.
     *
     * @param model модель страницы
     * @return имя шаблона списка тестов
     */
    @GetMapping("/tests")
    public String tests(Model model) {
        model.addAttribute("tests", testRepository.findAllWithTopicAndQuestions());
        return "admin/tests";
    }

    /**
     * Показывает форму создания нового теста.
     *
     * @param model модель страницы
     * @return имя шаблона формы теста
     */
    @GetMapping("/tests/new")
    public String newTest(Model model) {
        model.addAttribute("testForm", new TestForm());
        addTestFormModel(model, null, "create");
        return "admin/test-form";
    }

    /**
     * Создает новый тест.
     *
     * @param testForm данные формы теста
     * @param bindingResult результат валидации формы
     * @param model модель страницы
     * @return redirect на список тестов или форма с ошибками
     */
    @PostMapping("/tests")
    public String createTest(
            @Valid @ModelAttribute TestForm testForm,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            addTestFormModel(model, null, "create");
            return "admin/test-form";
        }

        KnowledgeTest test = new KnowledgeTest();
        applyTestForm(test, testForm);
        testRepository.save(test);
        return "redirect:/admin/tests";
    }

    /**
     * Показывает форму редактирования теста.
     *
     * @param id идентификатор теста
     * @param model модель страницы
     * @return имя шаблона формы теста
     */
    @GetMapping("/tests/{id}/edit")
    public String editTest(@PathVariable Long id, Model model) {
        KnowledgeTest test = getTest(id);
        model.addAttribute("testForm", toTestForm(test));
        addTestFormModel(model, test, "edit");
        return "admin/test-form";
    }

    /**
     * Обновляет существующий тест.
     *
     * @param id идентификатор теста
     * @param testForm данные формы теста
     * @param bindingResult результат валидации формы
     * @param model модель страницы
     * @return redirect на список тестов или форма с ошибками
     */
    @PostMapping("/tests/{id}")
    public String updateTest(
            @PathVariable Long id,
            @Valid @ModelAttribute TestForm testForm,
            BindingResult bindingResult,
            Model model
    ) {
        KnowledgeTest test = getTest(id);
        if (bindingResult.hasErrors()) {
            addTestFormModel(model, test, "edit");
            return "admin/test-form";
        }

        applyTestForm(test, testForm);
        testRepository.save(test);
        return "redirect:/admin/tests";
    }

    /**
     * Переключает статус публикации теста.
     *
     * @param id идентификатор теста
     * @return redirect на список тестов
     */
    @PostMapping("/tests/{id}/toggle-published")
    public String togglePublished(@PathVariable Long id) {
        KnowledgeTest test = getTest(id);
        test.setPublished(!test.isPublished());
        testRepository.save(test);
        return "redirect:/admin/tests";
    }

    /**
     * Удаляет тест, если по нему нет сохраненных попыток.
     *
     * @param id идентификатор теста
     * @param redirectAttributes flash-атрибуты для сообщения об ошибке
     * @return redirect на список тестов
     */
    @PostMapping("/tests/{id}/delete")
    public String deleteTest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            testRepository.deleteById(id);
        } catch (DataIntegrityViolationException exception) {
            redirectAttributes.addFlashAttribute("error", "Нельзя удалить тест, по которому уже есть попытки");
        }
        return "redirect:/admin/tests";
    }

    /**
     * Показывает вопросы выбранного теста.
     *
     * @param testId идентификатор теста
     * @param model модель страницы
     * @return имя шаблона списка вопросов
     */
    @GetMapping("/tests/{testId}/questions")
    public String questions(@PathVariable Long testId, Model model) {
        KnowledgeTest test = getTest(testId);
        model.addAttribute("test", test);
        model.addAttribute("questions", questionRepository.findByTestIdOrderByIdAsc(testId));
        return "admin/questions";
    }

    /**
     * Показывает форму создания вопроса.
     *
     * @param testId идентификатор теста
     * @param model модель страницы
     * @return имя шаблона формы вопроса
     */
    @GetMapping("/tests/{testId}/questions/new")
    public String newQuestion(@PathVariable Long testId, Model model) {
        KnowledgeTest test = getTest(testId);
        model.addAttribute("test", test);
        model.addAttribute("questionForm", new QuestionForm());
        addQuestionFormModel(model, blankOptionSlots(), List.of(), "create");
        return "admin/question-form";
    }

    /**
     * Создает вопрос и варианты ответов для теста.
     *
     * @param testId идентификатор теста
     * @param questionForm данные формы вопроса
     * @param bindingResult результат валидации формы
     * @param optionTexts тексты вариантов ответа
     * @param correctIndexes индексы правильных вариантов
     * @param model модель страницы
     * @return redirect на список вопросов или форма с ошибками
     */
    @PostMapping("/tests/{testId}/questions")
    public String createQuestion(
            @PathVariable Long testId,
            @Valid @ModelAttribute QuestionForm questionForm,
            BindingResult bindingResult,
            @RequestParam(name = "optionTexts", required = false) List<String> optionTexts,
            @RequestParam(name = "correctIndexes", required = false) List<Integer> correctIndexes,
            Model model
    ) {
        KnowledgeTest test = getTest(testId);
        validateOptions(questionForm, optionTexts, correctIndexes, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("test", test);
            addQuestionFormModel(model, optionTexts, correctIndexes, "create");
            return "admin/question-form";
        }

        Question question = new Question();
        question.setTest(test);
        applyQuestionForm(question, questionForm, optionTexts, correctIndexes);
        questionRepository.save(question);
        return "redirect:/admin/tests/" + testId + "/questions";
    }

    /**
     * Показывает форму редактирования вопроса.
     *
     * @param id идентификатор вопроса
     * @param model модель страницы
     * @return имя шаблона формы вопроса
     */
    @GetMapping("/questions/{id}/edit")
    public String editQuestion(@PathVariable Long id, Model model) {
        Question question = getQuestion(id);
        model.addAttribute("test", question.getTest());
        model.addAttribute("question", question);
        model.addAttribute("questionForm", toQuestionForm(question));
        addQuestionFormModel(model, optionTexts(question), correctIndexes(question), "edit");
        return "admin/question-form";
    }

    /**
     * Обновляет вопрос и его варианты ответов.
     *
     * @param id идентификатор вопроса
     * @param questionForm данные формы вопроса
     * @param bindingResult результат валидации формы
     * @param optionTexts тексты вариантов ответа
     * @param correctIndexes индексы правильных вариантов
     * @param model модель страницы
     * @return redirect на список вопросов или форма с ошибками
     */
    @PostMapping("/questions/{id}")
    public String updateQuestion(
            @PathVariable Long id,
            @Valid @ModelAttribute QuestionForm questionForm,
            BindingResult bindingResult,
            @RequestParam(name = "optionTexts", required = false) List<String> optionTexts,
            @RequestParam(name = "correctIndexes", required = false) List<Integer> correctIndexes,
            Model model
    ) {
        Question question = getQuestion(id);
        validateOptions(questionForm, optionTexts, correctIndexes, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("test", question.getTest());
            model.addAttribute("question", question);
            addQuestionFormModel(model, optionTexts, correctIndexes, "edit");
            return "admin/question-form";
        }

        applyQuestionForm(question, questionForm, optionTexts, correctIndexes);
        questionRepository.save(question);
        return "redirect:/admin/tests/" + question.getTest().getId() + "/questions";
    }

    /**
     * Удаляет вопрос, если он не используется в истории попыток.
     *
     * @param id идентификатор вопроса
     * @param redirectAttributes flash-атрибуты для сообщения об ошибке
     * @return redirect на список вопросов теста
     */
    @PostMapping("/questions/{id}/delete")
    public String deleteQuestion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Question question = getQuestion(id);
        Long testId = question.getTest().getId();
        try {
            questionRepository.delete(question);
        } catch (DataIntegrityViolationException exception) {
            redirectAttributes.addFlashAttribute("error", "Нельзя удалить вопрос, который есть в истории попыток");
        }
        return "redirect:/admin/tests/" + testId + "/questions";
    }

    /**
     * Показывает список пользователей.
     *
     * @param model модель страницы
     * @return имя шаблона списка пользователей
     */
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll(Sort.by("username")));
        return "admin/users";
    }

    /**
     * Показывает завершенные попытки пользователей.
     *
     * @param model модель страницы
     * @return имя шаблона результатов
     */
    @GetMapping("/results")
    public String results(Model model) {
        model.addAttribute("attempts", attemptRepository.findByFinishedAtIsNotNullOrderByFinishedAtDesc());
        return "admin/results";
    }

    /**
     * Показывает детали попытки для администратора.
     *
     * @param id идентификатор попытки
     * @param model модель страницы
     * @return имя шаблона деталей попытки
     */
    @GetMapping("/results/{id}")
    public String resultDetails(@PathVariable Long id, Model model) {
        model.addAttribute("attempt", testTakingService.getAttemptDetails(id));
        return "attempts/detail";
    }

    /**
     * Показывает статистику по тестам.
     *
     * @param model модель страницы
     * @return имя шаблона статистики
     */
    @GetMapping("/statistics")
    public String statistics(Model model) {
        model.addAttribute("statistics", statisticsService.getTestStatistics());
        return "admin/statistics";
    }

    /**
     * Загружает тему по идентификатору.
     *
     * @param id идентификатор темы
     * @return найденная тема
     */
    private Topic getTopic(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Тема не найдена"));
    }

    /**
     * Загружает тест по идентификатору вместе с темой и вопросами.
     *
     * @param id идентификатор теста
     * @return найденный тест
     */
    private KnowledgeTest getTest(Long id) {
        return testRepository.findWithTopicAndQuestionsById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Тест не найден"));
    }

    /**
     * Загружает вопрос по идентификатору вместе с вариантами ответов.
     *
     * @param id идентификатор вопроса
     * @return найденный вопрос
     */
    private Question getQuestion(Long id) {
        return questionRepository.findWithOptionsById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вопрос не найден"));
    }

    /**
     * Преобразует сущность темы в форму редактирования.
     *
     * @param topic тема
     * @return заполненная форма темы
     */
    private TopicForm toTopicForm(Topic topic) {
        TopicForm form = new TopicForm();
        form.setName(topic.getName());
        form.setDescription(topic.getDescription());
        return form;
    }

    /**
     * Переносит данные формы в сущность темы.
     *
     * @param topic изменяемая тема
     * @param form данные формы
     */
    private void applyTopicForm(Topic topic, TopicForm form) {
        topic.setName(form.getName().trim());
        topic.setDescription(trimToNull(form.getDescription()));
    }

    /**
     * Преобразует сущность теста в форму редактирования.
     *
     * @param test тест
     * @return заполненная форма теста
     */
    private TestForm toTestForm(KnowledgeTest test) {
        TestForm form = new TestForm();
        form.setTitle(test.getTitle());
        form.setDescription(test.getDescription());
        form.setTopicId(test.getTopic().getId());
        form.setTimeLimitMinutes(test.getTimeLimitMinutes());
        form.setPassingScorePercent(test.getPassingScorePercent());
        form.setPublished(test.isPublished());
        return form;
    }

    /**
     * Переносит данные формы в сущность теста.
     *
     * @param test изменяемый тест
     * @param form данные формы
     */
    private void applyTestForm(KnowledgeTest test, TestForm form) {
        Topic topic = getTopic(form.getTopicId());
        test.setTitle(form.getTitle().trim());
        test.setDescription(trimToNull(form.getDescription()));
        test.setTopic(topic);
        test.setTimeLimitMinutes(form.getTimeLimitMinutes());
        test.setPassingScorePercent(form.getPassingScorePercent());
        test.setPublished(form.isPublished());
    }

    /**
     * Преобразует сущность вопроса в форму редактирования.
     *
     * @param question вопрос
     * @return заполненная форма вопроса
     */
    private QuestionForm toQuestionForm(Question question) {
        QuestionForm form = new QuestionForm();
        form.setText(question.getText());
        form.setType(question.getType());
        form.setPoints(question.getPoints());
        return form;
    }

    /**
     * Переносит данные формы в сущность вопроса и пересоздает варианты ответов.
     *
     * @param question изменяемый вопрос
     * @param form данные формы вопроса
     * @param optionTexts тексты вариантов ответа
     * @param correctIndexes индексы правильных вариантов
     */
    private void applyQuestionForm(
            Question question,
            QuestionForm form,
            List<String> optionTexts,
            List<Integer> correctIndexes
    ) {
        Set<Integer> correct = new HashSet<>(correctIndexes == null ? List.of() : correctIndexes);
        question.setText(form.getText().trim());
        question.setType(form.getType());
        question.setPoints(form.getPoints());
        question.getOptions().clear();

        for (int i = 0; i < safeList(optionTexts).size(); i++) {
            String text = safeList(optionTexts).get(i);
            if (text == null || text.isBlank()) {
                continue;
            }
            AnswerOption option = new AnswerOption();
            option.setQuestion(question);
            option.setText(text.trim());
            option.setCorrect(correct.contains(i));
            question.getOptions().add(option);
        }
    }

    /**
     * Проверяет корректность списка вариантов ответа.
     *
     * @param form форма вопроса
     * @param optionTexts тексты вариантов ответа
     * @param correctIndexes индексы правильных вариантов
     * @param bindingResult объект для регистрации ошибок валидации
     */
    private void validateOptions(
            QuestionForm form,
            List<String> optionTexts,
            List<Integer> correctIndexes,
            BindingResult bindingResult
    ) {
        List<String> texts = safeList(optionTexts);
        Set<Integer> correct = new HashSet<>(correctIndexes == null ? List.of() : correctIndexes);
        long filledOptions = texts.stream().filter(text -> text != null && !text.isBlank()).count();
        long correctOptions = IntStream.range(0, texts.size())
                .filter(index -> texts.get(index) != null && !texts.get(index).isBlank())
                .filter(correct::contains)
                .count();

        if (filledOptions < 2) {
            bindingResult.reject("options.min", "Добавьте минимум два варианта ответа");
        }
        if (correctOptions == 0) {
            bindingResult.reject("options.correct", "Отметьте минимум один правильный вариант");
        }
        if (form.getType() == QuestionType.SINGLE_CHOICE && correctOptions > 1) {
            bindingResult.reject("options.single", "Для вопроса с одним ответом можно отметить только один правильный вариант");
        }
    }

    /**
     * Добавляет в модель общие данные для формы теста.
     *
     * @param model модель страницы
     * @param test редактируемый тест или {@code null} при создании
     * @param mode режим формы: создание или редактирование
     */
    private void addTestFormModel(Model model, KnowledgeTest test, String mode) {
        model.addAttribute("test", test);
        model.addAttribute("topics", topicRepository.findAllByOrderByNameAsc());
        model.addAttribute("mode", mode);
    }

    /**
     * Добавляет в модель общие данные для формы вопроса.
     *
     * @param model модель страницы
     * @param optionTexts тексты вариантов ответа
     * @param correctIndexes индексы правильных вариантов
     * @param mode режим формы: создание или редактирование
     */
    private void addQuestionFormModel(
            Model model,
            List<String> optionTexts,
            List<Integer> correctIndexes,
            String mode
    ) {
        model.addAttribute("optionTexts", ensureOptionSlots(optionTexts));
        model.addAttribute("correctIndexes", correctIndexes == null ? List.of() : correctIndexes);
        model.addAttribute("mode", mode);
    }

    /**
     * Извлекает тексты вариантов ответа из вопроса.
     *
     * @param question вопрос
     * @return список текстов вариантов
     */
    private List<String> optionTexts(Question question) {
        return question.getOptions().stream()
                .map(AnswerOption::getText)
                .toList();
    }

    /**
     * Определяет индексы правильных вариантов ответа.
     *
     * @param question вопрос
     * @return индексы правильных вариантов
     */
    private List<Integer> correctIndexes(Question question) {
        List<AnswerOption> options = question.getOptions();
        return IntStream.range(0, options.size())
                .filter(index -> options.get(index).isCorrect())
                .boxed()
                .toList();
    }

    /**
     * Создает пустые поля вариантов ответа для нового вопроса.
     *
     * @return список пустых строк для формы
     */
    private List<String> blankOptionSlots() {
        return ensureOptionSlots(List.of());
    }

    /**
     * Дополняет список вариантов пустыми строками до минимального количества полей формы.
     *
     * @param optionTexts исходные тексты вариантов
     * @return список с гарантированным количеством полей
     */
    private List<String> ensureOptionSlots(List<String> optionTexts) {
        List<String> result = new ArrayList<>(safeList(optionTexts));
        while (result.size() < 6) {
            result.add("");
        }
        return result;
    }

    /**
     * Возвращает пустой список вместо {@code null}.
     *
     * @param source исходный список
     * @return исходный список или пустой список
     */
    private List<String> safeList(List<String> source) {
        return source == null ? List.of() : source;
    }

    /**
     * Обрезает строку и заменяет пустое значение на {@code null}.
     *
     * @param value исходная строка
     * @return очищенная строка или {@code null}
     */
    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
