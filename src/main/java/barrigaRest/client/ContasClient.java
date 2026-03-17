package barrigaRest.client;

import barrigaRest.model.Conta;
import io.restassured.response.Response;
import static barrigaRest.client.BaseClient.requestLogado;
import static barrigaRest.client.BaseClient.requestSpec;
import static io.restassured.RestAssured.given;

public class ContasClient {
    public Response inserirConta(Conta conta){
        return given()
                .spec(requestLogado())
                .body(conta)
        .when()
                .post("/contas")
        ;
    }

    public Response listarSemToken(){
        return given()
                .spec(requestSpec)
        .when()
                .get("/contas")
        ;
    }

    public Integer extraiId(Conta conta){
        return inserirConta(conta)
         .then()
                .extract().path("id")
         ;
    }

    public Response alterarConta(Integer id, Conta conta){
        return given()
                .spec(requestLogado())
                .body(conta)
         .when()
                .put("contas/" + id)
         ;
    }

    public Response removeConta(Integer id){
        return given()
                .spec(requestLogado())
        .when()
                .delete("/contas/" + id)
        ;
    }
}
