package ru.sturov.testing.dto;

/**
 * Строка статистики по одному тесту.
 *
 * @param testId идентификатор теста
 * @param testTitle название теста
 * @param topicName название темы
 * @param attempts количество завершенных попыток
 * @param averagePercent средний процент выполнения
 * @param successRate процент успешных попыток
 */
public record TestStatistics(
        Long testId,
        String testTitle,
        String topicName,
        long attempts,
        double averagePercent,
        double successRate
) {
}
