package aqa.tests;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.specification.RequestSpecification;
import static org.junit.jupiter.api.Assertions.assertEquals;
import aqa.client.BaseRequest;
import aqa.enums.ActionType;

public class HeadersTests extends BaseTest{

    public record TestScenario(String description, String contentType, String apiKey, String accept, int expectedStatusCode, int numOfRequestsInMock) {
        @Override
        public String toString() {
            return description; 
        }
    }

    static Stream<TestScenario> headerScenarios() {
        return Stream.of(
            new TestScenario("некорректный Content-Type — отклоняется",
                "text/plain", apiKey, accept, 500, 0),
            new TestScenario("без X-Api-Key — отклоняется",
                contentType, null, accept, 401, 0),
            new TestScenario("пустой X-Api-Key — отклоняется",
                contentType, "", accept, 401, 0),
            new TestScenario("некорректный X-Api-Key — отклоняется",
                contentType, "wrong-key", accept, 401, 0),
            new TestScenario("с Accept — принимается",
                contentType, apiKey, accept, 200, 1),
            new TestScenario("без Accept — принимается",
                contentType, apiKey, null,200, 1)
        );
    }

    @DisplayName("Запрос ")
    @Severity(SeverityLevel.CRITICAL)
    @ParameterizedTest(name = "{0}")
    @MethodSource("headerScenarios")
    void shouldValidateHeaders(TestScenario scenario) {
        RequestSpecification requestSpec = new BaseRequest().baseRequest(
            newToken(),
            ActionType.LOGIN,
            scenario.contentType(),
            scenario.apiKey(),
            scenario.accept(),
            true
        );

        requestSpec
            .log().ifValidationFails()
            .when()
            .post(endpoint)
            .then()
            .statusCode(scenario.expectedStatusCode());
        
            checkMock(auth, scenario.numOfRequestsInMock);
    }

    @Test
    @DisplayName("Запрос без заголовка Content-Type возращает код 500")
    @Severity(SeverityLevel.CRITICAL)
    void shouldRejectRequestWithoutContentType() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        var uri = String.format("%s%s?token=%s&action=%s", baseUri, endpoint, newToken(), ActionType.LOGIN);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .header("X-Api-Key", apiKey)
            .header("Accept", accept)
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(500, response.statusCode());
    checkMock(auth, 0);
    }
    
    @Test
    @DisplayName("Запрос с корректными заголовками возвращает код 200")
    @Severity(SeverityLevel.CRITICAL)
    void correctRequest(){
        var token = newToken();
        doActionType(token, ActionType.LOGIN, 200);
        checkMock(auth);
    }
}