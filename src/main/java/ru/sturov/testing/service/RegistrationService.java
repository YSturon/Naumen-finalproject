package ru.sturov.testing.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sturov.testing.domain.UserAccount;
import ru.sturov.testing.domain.UserRole;
import ru.sturov.testing.dto.RegistrationForm;
import ru.sturov.testing.repository.UserAccountRepository;

/**
 * Сервис регистрации пользователей.
 *
 * <p>Проверяет уникальность логина, кодирует пароль и создает
 * учетную запись с ролью обычного пользователя.</p>
 */
@Service
public class RegistrationService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Регистрирует нового пользователя.
     *
     * @param form данные формы регистрации
     * @return сохраненная учетная запись
     * @throws IllegalArgumentException если логин уже занят
     */
    @Transactional
    public UserAccount register(RegistrationForm form) {
        String username = form.getUsername().trim();
        if (userAccountRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }

        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setFullName(form.getFullName().trim());
        account.setPassword(passwordEncoder.encode(form.getPassword()));
        account.setRole(UserRole.USER);
        account.setEnabled(true);
        return userAccountRepository.save(account);
    }
}
