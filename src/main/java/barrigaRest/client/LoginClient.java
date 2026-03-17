package barrigaRest.client;

import barrigaRest.model.Credenciais;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

public class LoginClient extends BaseClient{
    public Response logar(Credenciais credenciais){
        return given()
                .spec(requestSpec)
                .body(credenciais)
        .when()
                .post("/signin")
        ;
    }

    public String geraToken(Credenciais credenciais){
        return given()
                .spec(requestSpec)
                .body(credenciais)
        .when()
                .post("/signin")
        .then()
                .statusCode(200)
                .extract().path("token")
        ;
    }
}
