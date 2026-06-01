package ru.sturov.testing.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Ответ пользователя на конкретный вопрос в рамках попытки.
 *
 * <p>Хранит выбранные варианты и признак правильности ответа.</p>
 */
@Entity
@Table(name = "attempt_answers")
public class AttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private TestAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToMany
    @JoinTable(
            name = "attempt_answer_options",
            joinColumns = @JoinColumn(name = "attempt_answer_id"),
            inverseJoinColumns = @JoinColumn(name = "answer_option_id")
    )
    private Set<AnswerOption> selectedOptions = new LinkedHashSet<>();

    private boolean correct;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TestAttempt getAttempt() {
        return attempt;
    }

    public void setAttempt(TestAttempt attempt) {
        this.attempt = attempt;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Set<AnswerOption> getSelectedOptions() {
        return selectedOptions;
    }

    public void setSelectedOptions(Set<AnswerOption> selectedOptions) {
        this.selectedOptions = selectedOptions;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}
