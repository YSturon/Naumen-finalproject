package ru.sturov.testing.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Тест для проверки знаний пользователя.
 *
 * <p>Тест связан с темой, содержит список вопросов, ограничение по времени,
 * проходной процент и признак публикации.</p>
 */
@Entity
@Table(name = "knowledge_tests")
public class KnowledgeTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 1200)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(nullable = false)
    private int timeLimitMinutes = 20;

    @Column(nullable = false)
    private int passingScorePercent = 60;

    @Column(nullable = false)
    private boolean published;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Question> questions = new ArrayList<>();

    /**
     * Заполняет дату создания перед первым сохранением теста.
     */
    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Возвращает количество вопросов в тесте.
     *
     * @return количество вопросов
     */
    public int getQuestionCount() {
        return questions.size();
    }

    /**
     * Вычисляет максимальное количество баллов за тест.
     *
     * @return сумма баллов всех вопросов
     */
    public int getMaxScore() {
        return questions.stream().mapToInt(Question::getPoints).sum();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public int getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public void setTimeLimitMinutes(int timeLimitMinutes) {
        this.timeLimitMinutes = timeLimitMinutes;
    }

    public int getPassingScorePercent() {
        return passingScorePercent;
    }

    public void setPassingScorePercent(int passingScorePercent) {
        this.passingScorePercent = passingScorePercent;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
