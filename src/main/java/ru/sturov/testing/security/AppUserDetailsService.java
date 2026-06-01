package ru.sturov.testing.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.sturov.testing.domain.UserAccount;
import ru.sturov.testing.repository.UserAccountRepository;

/**
 * Адаптер между таблицей пользователей приложения и Spring Security.
 *
 * <p>Сервис загружает пользователя по логину и преобразует его в
 * {@link UserDetails}, который используется механизмом авторизации.</p>
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    public AppUserDetailsService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * Загружает пользователя для Spring Security.
     *
     * @param username логин пользователя
     * @return данные пользователя с ролью и статусом активности
     * @throws UsernameNotFoundException если пользователь с таким логином не найден
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount account = userAccountRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        return User.withUsername(account.getUsername())
                .password(account.getPassword())
                .roles(account.getRole().name())
                .disabled(!account.isEnabled())
                .build();
    }
}
