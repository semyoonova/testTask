package aqa.client;

import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.given;

import aqa.config.Config;
import aqa.enums.ActionType;

public class BaseRequest {

    public RequestSpecification baseRequest(String token, String action, String contentType, String apiKey, String accept, boolean sendAsForm) {
    RequestSpecification spec = given().baseUri(Config.get("baseUri"));

    if (contentType != null) spec.contentType(contentType);
    if (apiKey != null)      spec.header("X-Api-Key", apiKey);
    if (accept != null)      spec.accept(accept);

    if (sendAsForm) {
        spec.formParam("token", token).formParam("action", action);
    } else {
        spec.queryParam("token", token).queryParam("action", action);
    }
    return spec;
}

 public RequestSpecification baseRequest(String token, ActionType action, String contentType, String apiKey, String accept, boolean sendAsForm) {
    RequestSpecification spec = given().baseUri(Config.get("baseUri"));

    if (contentType != null) spec.contentType(contentType);
    if (apiKey != null)      spec.header("X-Api-Key", apiKey);
    if (accept != null)      spec.accept(accept);

    if (sendAsForm) {
        spec.formParam("token", token).formParam("action", action.getValue());
    } else {
        spec.queryParam("token", token).queryParam("action", action.getValue());
    }
    return spec;
}

    public RequestSpecification baseRequest(String token, String action) {
        return baseRequest(token, action,
                Config.get("contentType"), Config.get("apiKey"), Config.get("accept"), false);
    }

    public RequestSpecification baseRequest(String token, ActionType action) {
        return baseRequest(token, action.getValue(),
                Config.get("contentType"), Config.get("apiKey"), Config.get("accept"), false);
    }
}
