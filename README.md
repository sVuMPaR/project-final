## [REST API](http://localhost:8080/doc)

## Концепция:

- Spring Modulith
    - [Spring Modulith: достигли ли мы зрелости модульности](https://habr.com/ru/post/701984/)
    - [Introducing Spring Modulith](https://spring.io/blog/2022/10/21/introducing-spring-modulith)
    - [Spring Modulith - Reference documentation](https://docs.spring.io/spring-modulith/docs/current-SNAPSHOT/reference/html/)

```
При ошибке Access denied "ошибка mkdir C:\Windows\system32\pgdata:"
cd C:\Users\<твой_пользователь>\.docker
Bash
docker run -p 5432:5432 --name postgres-db -e POSTGRES_USER=jira -e POSTGRES_PASSWORD=JiraRush -e POSTGRES_DB=jira -e PGDATA=/var/lib/postgresql/data/pgdata -v ./pgdata:/var/lib/postgresql/data -d postgres

Bash-test
docker run -p 5433:5432 --name postgres-db-test -e POSTGRES_USER=jira -e POSTGRES_PASSWORD=JiraRush -e POSTGRES_DB=jira-test -e PGDATA=/var/lib/postgresql/data/pgdata -v ./pgdata-test:/var/lib/postgresql/data -d postgres
```


```
  url: jdbc:postgresql://localhost:5432/jira
  username: jira
  password: JiraRush
```

- Есть 2 общие таблицы, на которых не fk
    - _Reference_ - справочник. Связь делаем по _code_ (по id нельзя, тк id привязано к окружению-конкретной базе)
    - _UserBelong_ - привязка юзеров с типом (owner, lead, ...) к объекту (таска, проект, спринт, ...). FK вручную будем
      проверять

## Аналоги

- https://java-source.net/open-source/issue-trackers

## Тестирование

- https://habr.com/ru/articles/259055/

Список выполненных задач (номера как в [задании](ru.javarush.spring.presentation.level25.html)):

- **1.** Onboarding — разобрался со структурой проекта
- **2.** Удалены социальные сети VK и Yandex (OAuth2)
- **3.** Чувствительные данные вынесены в `config/_application-secret.yaml`, значения читаются из переменных окружения (`${JIRA_DB_PASSWORD}` и т.д.)
- **5.** Написаны тесты для `ProfileRestController` (14 тестов: GET/PUT, success и error paths)

  Запуск только этих тестов: `mvn test -Dtest=ProfileRestControllerTest`