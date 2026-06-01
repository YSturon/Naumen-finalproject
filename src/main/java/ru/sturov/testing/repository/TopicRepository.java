package ru.sturov.testing.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.sturov.testing.domain.Topic;

/**
 * Репозиторий тем тестирования.
 */
public interface TopicRepository extends JpaRepository<Topic, Long> {

    /**
     * Возвращает все темы, отсортированные по названию.
     *
     * @return список тем
     */
    List<Topic> findAllByOrderByNameAsc();

    /**
     * Проверяет существование темы с заданным названием без учета регистра.
     *
     * @param name название темы
     * @return {@code true}, если тема уже существует
     */
    boolean existsByNameIgnoreCase(String name);
}
