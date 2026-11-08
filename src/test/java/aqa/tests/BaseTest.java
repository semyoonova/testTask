package aqa.tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import aqa.client.BaseRequest;
import aqa.config.Config;
import aqa.data.TokenGenerator;
import aqa.enums.ActionType;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import static org.awaitility.Awaitility.await;

@WireMockTest(httpPort = 8888)
abstract class BaseTest {

    protected static final String endpoint = Config.get("mainEndpoint");
    protected static final String auth = Config.get("authEndpoint");
    protected static final String doAction = Config.get("doActionEndpoint");
    protected static final String apiKey = Config.get("apiKey");
    protected static final String accept = Config.get("accept");
    protected static final String contentType = Config.get("contentType");
    protected static final String incorrectContentType = Config.get("incorrectContentType");
    protected static final String baseUri = Config.get("baseUri");
    protected static final int tokenLength = Config.getInt("tokenLength");
    protected static final List<String> usedTokens = new ArrayList<>();

    @BeforeAll
    static void aq(){
        Allure.step("Запуск приложения", () -> {
            await().atMost(10, java.util.concurrent.TimeUnit.SECONDS)
            .pollInterval(500, java.util.concurrent.TimeUnit.MILLISECONDS)
            .until(() -> {
                try (SocketChannel sc = SocketChannel.open()) {
                    return sc.connect(new InetSocketAddress("localhost", 8080));
                } catch (Exception e) {
                    return false;
                }
            });
        });
    }

    @BeforeEach 
    void setUpMock(){
        Allure.step("Настройка мока", () -> {
            stubFor(post(Config.get("authEndpoint")).willReturn(ok()));
            stubFor(post(Config.get("doActionEndpoint")).willReturn(ok()));
        });
    }

    protected static String newToken(){
        var token = TokenGenerator.validToken();
        usedTokens.add(token);
        return token;
    }

    @AfterEach
    void logoutUsedTokens(){
        Allure.step("Отчистка токена из памяти", () -> {
            usedTokens.forEach(token -> {
                new BaseRequest().baseRequest(token, ActionType.LOGOUT)
                .when()
                .post(endpoint)
                .thenReturn();
            });
        });

        usedTokens.clear();
    }

    @Step("Проверяем появление запроса в журнале мока {path}")
    protected void checkRequestInMock(String path){
        verify(postRequestedFor(urlEqualTo(path)));
    }

    @Step("Проверяем появление запроса в журнале мока {path} ровно {times} раз")
    protected void checkRequestInMock(String path, int times){
        verify(exactly(times), postRequestedFor(urlEqualTo(path)));
    }

    @Step("Выполняем {type}: ожидаем статус {expectedCode}")
    protected void doActionType(String token, ActionType type, int expectedCode){
        new BaseRequest().baseRequest(token, type)
            .when()
            .post(endpoint)
            .then()
            .statusCode(expectedCode);
    }
}