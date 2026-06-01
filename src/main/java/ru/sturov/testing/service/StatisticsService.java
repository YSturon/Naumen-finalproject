package ru.sturov.testing.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sturov.testing.domain.KnowledgeTest;
import ru.sturov.testing.domain.TestAttempt;
import ru.sturov.testing.dto.TestStatistics;
import ru.sturov.testing.repository.KnowledgeTestRepository;
import ru.sturov.testing.repository.TestAttemptRepository;

/**
 * Сервис расчета статистики по тестам.
 *
 * <p>Используется административным разделом для отображения количества попыток,
 * среднего процента выполнения и процента успешного прохождения.</p>
 */
@Service
public class StatisticsService {

    private final KnowledgeTestRepository testRepository;
    private final TestAttemptRepository attemptRepository;

    public StatisticsService(KnowledgeTestRepository testRepository, TestAttemptRepository attemptRepository) {
        this.testRepository = testRepository;
        this.attemptRepository = attemptRepository;
    }

    /**
     * Возвращает статистику по всем тестам.
     *
     * @return список строк статистики
     */
    @Transactional(readOnly = true)
    public List<TestStatistics> getTestStatistics() {
        List<KnowledgeTest> tests = testRepository.findAllWithTopicAndQuestions();
        Map<Long, List<TestAttempt>> attemptsByTest = attemptRepository.findByFinishedAtIsNotNullOrderByFinishedAtDesc()
                .stream()
                .collect(Collectors.groupingBy(attempt -> attempt.getTest().getId()));

        return tests.stream()
                .map(test -> toStatistics(test, attemptsByTest.getOrDefault(test.getId(), List.of())))
                .toList();
    }

    /**
     * Формирует строку статистики по одному тесту.
     *
     * @param test тест, по которому считается статистика
     * @param attempts завершенные попытки этого теста
     * @return рассчитанная строка статистики
     */
    private TestStatistics toStatistics(KnowledgeTest test, List<TestAttempt> attempts) {
        long count = attempts.size();
        double averagePercent = attempts.stream()
                .mapToInt(TestAttempt::getPercent)
                .average()
                .orElse(0);
        double successRate = attempts.stream()
                .filter(TestAttempt::isPassed)
                .count() * 100.0 / Math.max(count, 1);

        return new TestStatistics(
                test.getId(),
                test.getTitle(),
                test.getTopic().getName(),
                count,
                averagePercent,
                successRate
        );
    }
}
