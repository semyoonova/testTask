package aqa.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import aqa.enums.ActionType;
import io.qameta.allure.Epic;
import io.qameta.allure.Step;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Epic("Внешний сервис") 
public class ExternalApiTests extends BaseTest{
    
    @DisplayName("Если внешний сервис при обращении вернул ошибку вход не выполняется и токен не сохраняется")
    @ParameterizedTest(name = "Mock: {displayName}")
    @ValueSource(ints = {500, 504, 404, 400})
    void api(int statusCode) {
        stubFor(post(auth).willReturn(status(statusCode)));
        var token = newToken();

        doActionType(token, ActionType.LOGIN, 500);
        checkRequestInMock(auth);

        doActionType(token, ActionType.ACTION, 403);
        checkRequestInMock(doAction, 0);
    }

    @DisplayName("Если внешний сервис при обращении к /doAction после авторизации вернул ошибку токен сохраняется")
    @ParameterizedTest(name = "Mock: {displayName}")
    @ValueSource(ints = {500, 504, 404, 400})
    void api1(int statusCode) {
        stubFor(post(doAction).willReturn(status(statusCode)));
        var token = newToken();

        doActionType(token, ActionType.LOGIN, 200);
        checkRequestInMock(auth);
        
        doActionType(token, ActionType.ACTION, 500);
        checkRequestInMock(doAction);

        doActionType(token, ActionType.LOGIN, 409);
        checkRequestInMock(auth, 1);
    }

    @DisplayName("Если внешний сервис вернул код в диапазоне от 200 до 400 запрос возвращает код 200")
    @ParameterizedTest(name = "Mock: {displayName}")
    @ValueSource(ints = {200, 201, 299, 300, 399})
    void api4(int statusCode) {
        stubFor(post("/doAction").willReturn(status(statusCode)));

        var token = newToken();
        doActionType(token, ActionType.LOGIN, 200);

        checkRequestInMock(auth);

        doActionType(token, ActionType.ACTION, 200);

        checkRequestInMock(doAction);
    }

    @Test
    @DisplayName("На внешний сервис передаются корректные заголовки Accept и Content-Type, в теле передается параметр token")
    void api2headers() {

        var token = newToken();
        doActionType(token, ActionType.LOGIN, 200);

        checkRequestHeadersInMock(auth, "Content-Type", contentType);
        checkRequestHeadersInMock(auth, "Accept", accept);
        checkRequestBodyInMock(auth, token);

        doActionType(token, ActionType.ACTION, 200);

        checkRequestHeadersInMock(doAction, "Content-Type", contentType);
        checkRequestHeadersInMock(doAction, "Accept", accept);
        checkRequestBodyInMock(doAction, token);
    }

    @Step("Проверяем появление запроса в журнале мока {endpoint} и заголовок {header}")
    private void checkRequestHeadersInMock(String endpoint, String header, String expectedValue){
        verify(postRequestedFor(urlEqualTo(endpoint))
            .withHeader(header, equalTo(expectedValue)));
    }

    @Step("Проверяем тело запроса в журнале мока {endpoint} и заголовки {}")
    private void checkRequestBodyInMock(String endpoint, String token){
        verify(postRequestedFor(urlEqualTo(endpoint))
            .withRequestBody(equalTo("token=" + token)));
    }
}