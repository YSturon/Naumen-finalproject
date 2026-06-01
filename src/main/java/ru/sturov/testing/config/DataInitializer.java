package ru.sturov.testing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.sturov.testing.domain.AnswerOption;
import ru.sturov.testing.domain.KnowledgeTest;
import ru.sturov.testing.domain.Question;
import ru.sturov.testing.domain.QuestionType;
import ru.sturov.testing.domain.Topic;
import ru.sturov.testing.domain.UserAccount;
import ru.sturov.testing.domain.UserRole;
import ru.sturov.testing.repository.KnowledgeTestRepository;
import ru.sturov.testing.repository.TopicRepository;
import ru.sturov.testing.repository.UserAccountRepository;

// Можно было написать нормальные миграции в бд, но забыл про проект и всё в последний день, не бейте)
@Configuration
public class DataInitializer {



    @Bean
    CommandLineRunner seedData(
            UserAccountRepository userRepository,
            TopicRepository topicRepository,
            KnowledgeTestRepository testRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed-demo-data:true}") boolean seedDemoData
    ) {
        return args -> {
            if (!seedDemoData) {
                return;
            }

            createUserIfMissing(userRepository, passwordEncoder, "admin", "admin123", "Администратор", UserRole.ADMIN);
            createUserIfMissing(userRepository, passwordEncoder, "user", "user123", "Демо Пользователь", UserRole.USER);

            if (topicRepository.count() > 0) {
                return;
            }

            Topic javaTopic = new Topic();
            javaTopic.setName("Java");
            javaTopic.setDescription("Основы языка Java и объектно-ориентированного программирования.");
            topicRepository.save(javaTopic);

            Topic springTopic = new Topic();
            springTopic.setName("Spring Boot");
            springTopic.setDescription("Веб-приложения, безопасность и работа с данными в Spring Boot.");
            topicRepository.save(springTopic);

            KnowledgeTest javaTest = new KnowledgeTest();
            javaTest.setTitle("Основы Java");
            javaTest.setDescription("Короткий тест по базовым понятиям Java.");
            javaTest.setTopic(javaTopic);
            javaTest.setTimeLimitMinutes(15);
            javaTest.setPassingScorePercent(60);
            javaTest.setPublished(true);
            addQuestion(javaTest, "Какой тип данных используется для хранения целых чисел?", QuestionType.SINGLE_CHOICE, 1,
                    option("int", true),
                    option("String", false),
                    option("boolean", false),
                    option("double", false));
            addQuestion(javaTest, "Какие модификаторы доступа существуют в Java?", QuestionType.MULTIPLE_CHOICE, 2,
                    option("public", true),
                    option("private", true),
                    option("protected", true),
                    option("package", false));
            testRepository.save(javaTest);

            KnowledgeTest springTest = new KnowledgeTest();
            springTest.setTitle("Spring Boot и MVC");
            springTest.setDescription("Проверка знаний по базовым аннотациям и слоям приложения.");
            springTest.setTopic(springTopic);
            springTest.setTimeLimitMinutes(20);
            springTest.setPassingScorePercent(60);
            springTest.setPublished(true);
            addQuestion(springTest, "Какая аннотация объявляет класс MVC-контроллером?", QuestionType.SINGLE_CHOICE, 1,
                    option("@Controller", true),
                    option("@Entity", false),
                    option("@Repository", false),
                    option("@Autowired", false));
            addQuestion(springTest, "Какие компоненты относятся к Spring Data JPA?", QuestionType.MULTIPLE_CHOICE, 2,
                    option("JpaRepository", true),
                    option("@Entity", true),
                    option("HttpServletRequest", false),
                    option("Hibernate", true));
            testRepository.save(springTest);
        };
    }


    private void createUserIfMissing(
            UserAccountRepository userRepository,
            PasswordEncoder passwordEncoder,
            String username,
            String password,
            String fullName,
            UserRole role
    ) {
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            return;
        }
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setFullName(fullName);
        account.setRole(role);
        account.setEnabled(true);
        userRepository.save(account);
    }


    private void addQuestion(KnowledgeTest test, String text, QuestionType type, int points, AnswerOption... options) {
        Question question = new Question();
        question.setText(text);
        question.setType(type);
        question.setPoints(points);
        question.setTest(test);
        for (AnswerOption option : options) {
            option.setQuestion(question);
            question.getOptions().add(option);
        }
        test.getQuestions().add(question);
    }


    private AnswerOption option(String text, boolean correct) {
        AnswerOption option = new AnswerOption();
        option.setText(text);
        option.setCorrect(correct);
        return option;
    }
}
