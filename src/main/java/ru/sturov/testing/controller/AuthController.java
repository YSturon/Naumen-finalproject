package ru.sturov.testing.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.sturov.testing.dto.RegistrationForm;
import ru.sturov.testing.service.RegistrationService;

/**
 * Контроллер авторизации и регистрации.
 *
 * <p>Отвечает за страницы входа, регистрации и перенаправление
 * с корневого URL на список тестов.</p>
 */
@Controller
public class AuthController {

    private final RegistrationService registrationService;

    public AuthController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /**
     * Перенаправляет пользователя с главной страницы на каталог тестов.
     *
     * @return redirect на страницу тестов
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/tests";
    }

    /**
     * Показывает форму входа.
     *
     * @return имя шаблона страницы входа
     */
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    /**
     * Показывает форму регистрации.
     *
     * @param model модель страницы
     * @return имя шаблона страницы регистрации
     */
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "auth/register";
    }

    /**
     * Обрабатывает отправку формы регистрации.
     *
     * @param registrationForm данные регистрации
     * @param bindingResult результат валидации формы
     * @return redirect на вход при успехе или форма регистрации при ошибках
     */
    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute RegistrationForm registrationForm,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            registrationService.register(registrationForm);
        } catch (IllegalArgumentException exception) {
            bindingResult.rejectValue("username", "username.exists", exception.getMessage());
            return "auth/register";
        }

        return "redirect:/login?registered";
    }
}
