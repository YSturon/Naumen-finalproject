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
 * Попытка прохождения теста пользователем.
 *
 * <p>Сущность хранит время начала и завершения, набранные баллы,
 * процент, итоговую оценку, статус прохождения и ответы пользователя.</p>
 */
@Entity
@Table(name = "test_attempts")
public class TestAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_id", nullable = false)
    private KnowledgeTest test;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private int maxScore;

    @Column(nullable = false)
    private int correctAnswers;

    @Column(nullable = false)
    private int totalQuestions;

    @Column(nullable = false)
    private int percent;

    @Column(nullable = false, length = 20)
    private String grade = "Не завершено";

    @Column(nullable = false)
    private boolean passed;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<AttemptAnswer> answers = new ArrayList<>();

    /**
     * Заполняет время начала попытки перед первым сохранением.
     */
    @PrePersist
    void prePersist() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }

    /**
     * Проверяет, завершена ли попытка.
     *
     * @return {@code true}, если у попытки заполнено время завершения
     */
    public boolean isFinished() {
        return finishedAt != null;
    }

    /**
     * Вычисляет крайний срок завершения попытки.
     *
     * @return дата и время, когда истекает лимит прохождения теста
     */
    public LocalDateTime getDeadline() {
        return startedAt.plusMinutes(test.getTimeLimitMinutes());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public KnowledgeTest getTest() {
        return test;
    }

    public void setTest(KnowledgeTest test) {
        this.test = test;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public List<AttemptAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AttemptAnswer> answers) {
        this.answers = answers;
    }
}
