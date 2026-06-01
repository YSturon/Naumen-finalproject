package ru.sturov.testing.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Форма создания и редактирования теста.
 *
 * <p>Передает из HTML-формы название, описание, тему, лимит времени,
 * проходной процент и признак публикации теста.</p>
 */
public class TestForm {

    @NotBlank(message = "Введите название теста")
    @Size(max = 160, message = "Название должно быть не длиннее 160 символов")
    private String title;

    @Size(max = 1200, message = "Описание должно быть не длиннее 1200 символов")
    private String description;

    @NotNull(message = "Выберите тему")
    private Long topicId;

    @Min(value = 1, message = "Ограничение времени должно быть не меньше 1 минуты")
    @Max(value = 240, message = "Ограничение времени должно быть не больше 240 минут")
    private int timeLimitMinutes = 20;

    @Min(value = 1, message = "Проходной процент должен быть от 1 до 100")
    @Max(value = 100, message = "Проходной процент должен быть от 1 до 100")
    private int passingScorePercent = 60;

    private boolean published;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public int getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public void setTimeLimitMinutes(int timeLimitMinutes) {
        this.timeLimitMinutes = timeLimitMinutes;
    }

    public int getPassingScorePercent() {
        return passingScorePercent;
    }

    public void setPassingScorePercent(int passingScorePercent) {
        this.passingScorePercent = passingScorePercent;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }
}
