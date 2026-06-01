package ru.sturov.testing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Форма создания и редактирования темы.
 *
 * <p>Содержит поля, которые администратор заполняет в HTML-форме темы.</p>
 */
public class TopicForm {

    @NotBlank(message = "Введите название темы")
    @Size(max = 120, message = "Название должно быть не длиннее 120 символов")
    private String name;

    @Size(max = 1000, message = "Описание должно быть не длиннее 1000 символов")
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
