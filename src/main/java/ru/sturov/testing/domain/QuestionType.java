package ru.sturov.testing.domain;

/**
 * Тип вопроса в тесте.
 *
 * <p>Тип определяет, должен ли пользователь выбрать один вариант ответа
 * или несколько вариантов.</p>
 */
public enum QuestionType {
    /**
     * Вопрос с одним правильным вариантом ответа.
     */
    SINGLE_CHOICE("Один правильный ответ"),

    /**
     * Вопрос с несколькими правильными вариантами ответа.
     */
    MULTIPLE_CHOICE("Несколько правильных ответов");

    private final String displayName;

    QuestionType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Возвращает русскоязычное название типа вопроса для отображения в HTML.
     *
     * @return отображаемое название типа вопроса
     */
    public String getDisplayName() {
        return displayName;
    }
}
