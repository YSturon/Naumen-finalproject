package ru.sturov.testing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.sturov.testing.domain.QuestionType;

/**
 * Форма создания и редактирования вопроса.
 *
 * <p>Варианты ответов передаются отдельно списками параметров, а эта форма
 * отвечает за основные свойства вопроса.</p>
 */
public class QuestionForm {

    @NotBlank(message = "Введите текст вопроса")
    @Size(max = 2000, message = "Текст вопроса должен быть не длиннее 2000 символов")
    private String text;

    @NotNull(message = "Выберите тип вопроса")
    private QuestionType type = QuestionType.SINGLE_CHOICE;

    @Min(value = 1, message = "Баллы должны быть не меньше 1")
    private int points = 1;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
