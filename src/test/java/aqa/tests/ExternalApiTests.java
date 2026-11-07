package aqa.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import aqa.enums.ActionType;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class ExternalApiTests extends BaseTest{
    
    @DisplayName("Если внешний сервис при обращении вернул ошибку вход не выполняется и токен не сохраняется")
    @ParameterizedTest(name = "Mock: {displayName}")
    @ValueSource(ints = {500, 504, 404, 400})
    void api(int statusCode) {
        stubFor(post(auth).willReturn(status(statusCode)));
        var token = newToken();

        doActionType(token, ActionType.LOGIN, 500);
        checkMock(auth);

        doActionType(token, ActionType.ACTION, 403);
        checkMock(doAction, 0);
    }

    @DisplayName("Если внешний сервис при обращении к /doAction после авторизации вернул ошибку токен сохраняется")
    @ParameterizedTest(name = "Mock: {displayName}")
    @ValueSource(ints = {500, 504, 404, 400})
    void api1(int statusCode) {
        stubFor(post(doAction).willReturn(status(statusCode)));
        var token = newToken();

        doActionType(token, ActionType.LOGIN, 200);
        checkMock(auth);
        
        doActionType(token, ActionType.ACTION, 500);
        checkMock(doAction);

        doActionType(token, ActionType.LOGIN, 409);
        checkMock(auth, 1);
    }

    @DisplayName("Если внешний сервис вернул код в диапазоне от 200 до 400 запрос возвращает код 200")
    @ParameterizedTest(name = "Mock: {displayName}")
    @ValueSource(ints = {200, 201, 299, 300, 399})
    void api4(int statusCode) {
        stubFor(post("/doAction").willReturn(status(statusCode)));

        var token = newToken();
        doActionType(token, ActionType.LOGIN, 200);

        checkMock(auth);

        doActionType(token, ActionType.ACTION, 200);

        checkMock(doAction);
    }

    @Test
    @DisplayName("На внешний сервис передаются корректные заголовки Accept и Content-Type, в теле передается параметр token")
    void api2headers() {

        var token = newToken();
        doActionType(token, ActionType.LOGIN, 200);

        verify(postRequestedFor(urlEqualTo(auth))
            .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
            .withHeader("Accept", equalTo("application/json")));

        verify(postRequestedFor(urlEqualTo(auth))
            .withRequestBody(equalTo("token=" + token)));

        doActionType(token, ActionType.ACTION, 200);

        verify(postRequestedFor(urlEqualTo(doAction))
            .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
            .withHeader("Accept", equalTo("application/json")));

        verify(postRequestedFor(urlEqualTo(doAction))
            .withRequestBody(equalTo("token=" + token)));
    }
}