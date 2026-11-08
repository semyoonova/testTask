package aqa.tests;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import aqa.client.BaseRequest;
import aqa.data.TokenGenerator;
import io.qameta.allure.Epic;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;

@Epic("Тело запроса") 
public class BodyValidationTests extends BaseTest{

    record BodyScenario(String description, String token, String action) {
        @Override
        public String toString() {
            return description; 
        }
    }
    
    static Stream<BodyScenario> bodyScenarios() {
        return Stream.of(
            new BodyScenario("Отсутствуют и token, и action", null, null),
            new BodyScenario("Пустые token и action", "", ""),
            new BodyScenario("token отсутствует, action есть", null, "LOGIN"),
            new BodyScenario("token есть, action отсутствует", newToken(), null),
            new BodyScenario("token короче на 1 символ", newToken().substring(0, tokenLength - 1), "LOGIN"),
            new BodyScenario("token длиннее на 1 символ", newToken() + "1", "LOGIN"),
            new BodyScenario("token с символами A-Z 0-9", TokenGenerator.invalidToken(), "LOGIN"),
            new BodyScenario("token с недопустимым символом '/'",newToken().substring(0, tokenLength - 1) + "/", "LOGIN"),
            new BodyScenario("token с недопустимым символом ':'", newToken().substring(0, tokenLength - 1) + ":", "LOGIN"),
            new BodyScenario("Неизвестный action LOGINN", newToken(), "LOGINN"),
            new BodyScenario("action в нижнем регистре 'login'", newToken(), "login"),
            new BodyScenario("action с опечаткой 'Loguot'", newToken(), "Loguot"),
            new BodyScenario("action с цифрой 'LOGOUT1'", newToken(), "LOGOUT1"),
            new BodyScenario("action с пробелом 'ac tion'", newToken(), "ac tion"),
            new BodyScenario("action 'ActioN' в смешанном регистре", newToken(), "ActioN")
        );
    }

    @DisplayName("Невалидное тело запроса должно отклоняться. ")
    @Severity(SeverityLevel.CRITICAL)
    @ParameterizedTest(name = "{0}")
    @MethodSource("bodyScenarios")
    void invalidBodyIsRejected(BodyScenario scenario) {
        sendRequest(scenario.token, scenario.action, 400);
        checkRequestInMock(auth, 0);
    }

    @Test
    @DisplayName("Валидное тело запроса должно приниматься")
    @Severity(SeverityLevel.CRITICAL)
    void validBodyIsAccepted() {
        sendRequest(newToken(), "LOGIN", 200);
        checkRequestInMock(auth, 1);
    }

    @Step("Отправляем запрос, ожидаем статус {expectedCode}")
    private void sendRequest(String token, String action, int expectedCode) {
        new BaseRequest()
            .baseRequest(token, action)
            .when()
            .post(endpoint)
            .then()
            .statusCode(expectedCode);
    }
}