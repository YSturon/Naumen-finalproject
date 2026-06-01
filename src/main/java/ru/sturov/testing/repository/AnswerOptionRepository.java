package ru.sturov.testing.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.sturov.testing.domain.AnswerOption;

/**
 * Репозиторий вариантов ответов.
 */
public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {

    /**
     * Возвращает варианты ответов по набору идентификаторов.
     *
     * @param ids идентификаторы вариантов ответов
     * @return список найденных вариантов
     */
    List<AnswerOption> findByIdIn(Collection<Long> ids);
}
