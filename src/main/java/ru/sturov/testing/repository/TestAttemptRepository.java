package ru.sturov.testing.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.sturov.testing.domain.TestAttempt;

/**
 * Репозиторий попыток прохождения тестов.
 *
 * <p>Содержит запросы для истории пользователя, результатов администратора
 * и детального просмотра попытки.</p>
 */
public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {

    /**
     * Возвращает попытки пользователя от новых к старым.
     *
     * @param username логин пользователя
     * @return список попыток пользователя
     */
    @EntityGraph(attributePaths = {"test", "test.topic", "user"})
    List<TestAttempt> findByUserUsernameIgnoreCaseOrderByStartedAtDesc(String username);

    /**
     * Возвращает все завершенные попытки от новых к старым.
     *
     * @return список завершенных попыток
     */
    @EntityGraph(attributePaths = {"test", "test.topic", "user"})
    List<TestAttempt> findByFinishedAtIsNotNullOrderByFinishedAtDesc();

    /**
     * Ищет попытку вместе с тестом и пользователем.
     *
     * @param id идентификатор попытки
     * @return найденная попытка или пустой результат
     */
    @EntityGraph(attributePaths = {"test", "test.topic", "user"})
    Optional<TestAttempt> findWithTestAndUserById(Long id);

    /**
     * Ищет попытку со всеми данными для детального просмотра.
     *
     * @param id идентификатор попытки
     * @return найденная попытка или пустой результат
     */
    @Query("""
            select distinct a
            from TestAttempt a
            join fetch a.test t
            join fetch t.topic
            join fetch a.user
            left join fetch a.answers aa
            left join fetch aa.question
            left join fetch aa.selectedOptions
            where a.id = :id
            """)
    Optional<TestAttempt> findDetailsById(Long id);

    /**
     * Считает количество завершенных попыток.
     *
     * @return количество завершенных попыток
     */
    long countByFinishedAtIsNotNull();
}
