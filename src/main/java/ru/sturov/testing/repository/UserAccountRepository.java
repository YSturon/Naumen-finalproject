package ru.sturov.testing.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.sturov.testing.domain.UserAccount;

/**
 * Репозиторий пользователей приложения.
 *
 * <p>Предоставляет базовые CRUD-операции и методы поиска по логину.</p>
 */
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    /**
     * Ищет пользователя по логину без учета регистра.
     *
     * @param username логин пользователя
     * @return найденный пользователь или пустой результат
     */
    Optional<UserAccount> findByUsernameIgnoreCase(String username);

    /**
     * Проверяет существование пользователя с заданным логином без учета регистра.
     *
     * @param username логин пользователя
     * @return {@code true}, если логин уже занят
     */
    boolean existsByUsernameIgnoreCase(String username);
}
