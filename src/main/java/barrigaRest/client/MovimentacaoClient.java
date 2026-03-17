package barrigaRest.client;

import barrigaRest.model.Movimentacao;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

public class MovimentacaoClient extends BaseClient {
    public Response insereMovimentacao(Movimentacao movimentacao){
        return given()
                .spec(requestLogado())
                .body(movimentacao)
            .when()
                .post("/transacoes")
            ;
    }

    public Integer extraiIdMovimentacao(Movimentacao movimentacao){
        return insereMovimentacao(movimentacao)
            .then()
                .extract().path("id")
           ;
    }

    public Response removeMovimentacao(Integer id){
        return given()
                .spec(requestLogado())
           .when()
                .delete("/transacoes/" + id)
           ;
    }
}
