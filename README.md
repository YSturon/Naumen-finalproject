# Система тестирования и оценки знаний

Техническое задание:

https://docs.google.com/document/d/1AfFSFMnhOYjHg3V6ZNztnWfr_LrA6RDL5ZdBbPMMhqQ/edit?usp=drive_link

Демонстрация работы:

https://youtu.be/tZqwhs6FHiw

## Запуск проекта

PostgreSQL через Docker Compose:

```
docker compose up -d
```
Запуск приложения:
```mvn spring-boot:run```

Адресс хоста:
```http://localhost:8080```

admin:
```admin```:```admin123```
user:
```user```:```user123```


P.S.
Признаюсь честно, я совсем забыл про проект и делал все в последний день, поэтому прошу не бить за дизайн и за халтуру с миграциями. 
Основная логика приложения работает, допишу нормальные тесты, если проект не успеют проверить раньше.
