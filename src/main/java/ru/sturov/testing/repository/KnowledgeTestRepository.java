package ru.sturov.testing.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.sturov.testing.domain.KnowledgeTest;

/**
 * Репозиторий тестов.
 *
 * <p>Содержит запросы с предварительной загрузкой тем и вопросов,
 * чтобы страницы могли работать при выключенном Open Session in View.</p>
 */
public interface KnowledgeTestRepository extends JpaRepository<KnowledgeTest, Long> {

    /**
     * Возвращает опубликованные тесты вместе с темами и вопросами.
     *
     * @return список опубликованных тестов
     */
    @Query("""
            select distinct t
            from KnowledgeTest t
            join fetch t.topic
            left join fetch t.questions
            where t.published = true
            order by t.title
            """)
    List<KnowledgeTest> findPublishedWithTopicAndQuestions();

    /**
     * Возвращает опубликованные тесты выбранной темы вместе с темами и вопросами.
     *
     * @param topicId идентификатор темы
     * @return список опубликованных тестов выбранной темы
     */
    @Query("""
            select distinct t
            from KnowledgeTest t
            join fetch t.topic
            left join fetch t.questions
            where t.published = true and t.topic.id = :topicId
            order by t.title
            """)
    List<KnowledgeTest> findPublishedWithTopicAndQuestionsByTopicId(Long topicId);

    /**
     * Возвращает все тесты вместе с темами и вопросами.
     *
     * @return список всех тестов
     */
    @Query("""
            select distinct t
            from KnowledgeTest t
            join fetch t.topic
            left join fetch t.questions
            order by t.title
            """)
    List<KnowledgeTest> findAllWithTopicAndQuestions();

    /**
     * Ищет тест по идентификатору вместе с темой и вопросами.
     *
     * @param id идентификатор теста
     * @return найденный тест или пустой результат
     */
    @Query("""
            select distinct t
            from KnowledgeTest t
            join fetch t.topic
            left join fetch t.questions
            where t.id = :id
            """)
    Optional<KnowledgeTest> findWithTopicAndQuestionsById(Long id);

    /**
     * Ищет тест по идентификатору вместе с темой без загрузки коллекции вопросов.
     *
     * @param id идентификатор теста
     * @return найденный тест или пустой результат
     */
    @EntityGraph(attributePaths = "topic")
    Optional<KnowledgeTest> findWithTopicById(Long id);

    /**
     * Считает количество опубликованных тестов.
     *
     * @return количество опубликованных тестов
     */
    long countByPublishedTrue();
}
