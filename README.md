## Документация и требования

- [Требования и тест-кейсы (Google Docs)] https://docs.google.com/document/d/1BZVD8VPMeXmDRGkR3Ga9WnTA40oD0vl8sqdJuDxnFy8/edit?usp=sharing 

# AQA Tests: API Automation on Java

Набор автотестов для REST API на стеке Java + JUnit5 + RestAssured + WireMock + Allure.  
Проект демонстрирует параметризованные тесты, мокирование внешних зависимостей, генерацию структурированных отчётов и работу с конфигурациями.

# Запуск тестов

mvn clean test

# Генерация отчёта Allure

mvn allure:serve