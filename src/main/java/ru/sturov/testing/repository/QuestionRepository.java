package ru.sturov.testing.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.sturov.testing.domain.Question;

/**
 * Репозиторий вопросов.
 *
 * <p>Методы репозитория загружают варианты ответов и связанный тест,
 * потому что эти данные нужны на страницах прохождения и администрирования.</p>
 */
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * Возвращает вопросы теста в порядке добавления.
     *
     * @param testId идентификатор теста
     * @return список вопросов теста
     */
    @EntityGraph(attributePaths = {"options", "test", "test.topic"})
    List<Question> findByTestIdOrderByIdAsc(Long testId);

    /**
     * Ищет вопрос вместе с вариантами ответов и тестом.
     *
     * @param id идентификатор вопроса
     * @return найденный вопрос или пустой результат
     */
    @EntityGraph(attributePaths = {"options", "test", "test.topic"})
    Optional<Question> findWithOptionsById(Long id);

    /**
     * Считает количество вопросов в тесте.
     *
     * @param testId идентификатор теста
     * @return количество вопросов
     */
    long countByTestId(Long testId);
}
