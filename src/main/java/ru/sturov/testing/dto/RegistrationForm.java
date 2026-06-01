package ru.sturov.testing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Форма регистрации нового пользователя.
 *
 * <p>Используется контроллером регистрации для приема и валидации
 * логина, пароля и ФИО.</p>
 */
public class RegistrationForm {

    @NotBlank(message = "Введите логин")
    @Size(min = 3, max = 64, message = "Логин должен быть от 3 до 64 символов")
    private String username;

    @NotBlank(message = "Введите пароль")
    @Size(min = 6, max = 100, message = "Пароль должен быть не короче 6 символов")
    private String password;

    @NotBlank(message = "Введите ФИО")
    @Size(max = 120, message = "ФИО должно быть не длиннее 120 символов")
    private String fullName;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
