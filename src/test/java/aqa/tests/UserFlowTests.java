package aqa.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import aqa.enums.ActionType;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

public class UserFlowTests extends BaseTest{

    @DisplayName("Неавторизованный пользователь: возвращает 403 при вызове")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Доступ без авторизации запрещён")
    @ParameterizedTest()
    @EnumSource(value = ActionType.class, names = {"ACTION", "LOGOUT"})
    void unauthorizedUserGetsForbidden(ActionType type) {
        doActionType(newToken(), type, 403);
    }

    @DisplayName("Авторизованный пользователь: возвращает 200 при вызове")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Доступ с авторизацией разрешен")
    @ParameterizedTest()
    @EnumSource(value = ActionType.class, names = {"ACTION", "LOGOUT"})
    void authorizedUserPerformsAction(ActionType type){
        var token = newToken();
        doActionType(token, ActionType.LOGIN, 200);
        doActionType(token, type, 200);
    }

    @DisplayName("После логаута возвращает 403 при вызове")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Токен удаляется из памяти после логаута")
    @ParameterizedTest()
    @EnumSource(value = ActionType.class, names = {"ACTION", "LOGOUT"})
    void actionAfterLogoutIsForbidden(ActionType type){
        var token = newToken();
        doActionType(token, ActionType.LOGIN, 200);
        doActionType(token, ActionType.LOGOUT, 200);
        doActionType(token, type, 403); 
    }

    @Test
    @DisplayName("Повторный LOGIN после логаута возвращает 200")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Повторная авторизация после логаута")
    void reLoginAfterLogoutSucceeds(){
        var token = newToken();
        doActionType(token, ActionType.LOGIN, 200);
        doActionType(token, ActionType.LOGOUT, 200);
        doActionType(token, ActionType.LOGIN, 200);  
    }

    @Test
    @DisplayName("Повторный ACTION в одной сессии возвращает 200")
    @Severity(SeverityLevel.NORMAL)
    @Story("Повторное действие в одной сессии")
    void repeatedActionInSameSessionSucceeds(){
        var token = newToken();
        doActionType(token, ActionType.LOGIN, 200);
        checkMock(auth);
        doActionType(token, ActionType.ACTION, 200);
        checkMock(doAction);
        doActionType(token, ActionType.ACTION, 200); 
        checkMock(doAction); 
    }
}
