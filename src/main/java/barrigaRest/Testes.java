package barrigaRest;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class Testes {
    public static RequestSpecification requisicaoPadrao;

    @BeforeAll
    public static void setUp(){
        requisicaoPadrao = new RequestSpecBuilder()
                .setBaseUri("https://barrigarest.wcaquino.me")
                .build();
    }

    @Test
    @DisplayName("[CT - 001] Não deve acessar API sem token")
    public void verificarAusenciaDeToken(){
        given()
                .log().all()
                .spec(requisicaoPadrao)
        .when()
                .get("/contas")
        .then()
                .log().all()
                .statusCode(401)
        ;
    }

    @Test
    @DisplayName("[CT - 002] Deve incluir conta com sucesso")
    public void insereConta(){
        //Faz login e extrai o token
        Credenciais credenciais = new Credenciais("thaina@teste.com", "teste123");

        String token = given()
                .spec(requisicaoPadrao)
                .body(credenciais)
                .contentType(ContentType.JSON)
        .when()
                .post("/signin")
        .then()
                .statusCode(200)
                .extract().path("token")
        ;

        //Inclui a conta
        Conta conta = new Conta("Cartão de credito");

        given()
                .spec(requisicaoPadrao)
                .contentType(ContentType.JSON)
                .header("Authorization", "JWT " + token)
                .body(conta)
        .when()
                .post("/contas")
        .then()
                .statusCode(201)
        ;
    }

    @Test
    @DisplayName("[CT - 003] Deve alterar conta com sucesso")
    public void alteraConta(){
        //Faz login e extrai o token
        Credenciais credenciais = new Credenciais("thaina@teste.com", "teste123");

        String token = given()
                .spec(requisicaoPadrao)
                .body(credenciais)
                .contentType(ContentType.JSON)
        .when()
                .post("/signin")
        .then()
                .statusCode(200)
                .extract().path("token")
        ;

        //Extrai o ID
        Conta conta = new Conta("Conta alterada");

        Integer id = given()
                .spec(requisicaoPadrao)
                .header("Authorization", "JWT " + token)
                .contentType(ContentType.JSON)
                .body(conta)
        .when()
                .post("/contas")
        .then()
                .statusCode(201)
                .extract().path("id")
        ;

        //Altera a conta
        given()
                .spec(requisicaoPadrao)
                .contentType(ContentType.JSON)
                .header("Authorization", "JWT " + token)
                .body(conta)
        .when()
                .put("contas/" + id)
        .then()
                .statusCode(200)
                .body("nome", is("Conta alterada"))
        ;
    }

    @Test
    @DisplayName("[CT - 004] Não deve alterar conta com nome repetido")
    public void contaNomeRepetido(){
        //Faz login e extrai o token
        Credenciais credenciais = new Credenciais("thaina@teste.com", "teste123");

        String token = given()
                .spec(requisicaoPadrao)
                .body(credenciais)
                .contentType(ContentType.JSON)
        .when()
                .post("/signin")
        .then()
                .statusCode(200)
                .extract().path("token")
        ;

        //Extrai o ID da conta
        Conta conta = new Conta("Conta teste");

        Integer id = given()
                .spec(requisicaoPadrao)
                .header("Authorization", "JWT " + token)
                .contentType(ContentType.JSON)
                .body(conta)
        .when()
                .post("/contas")
        .then()
                .statusCode(201)
                .extract().path("id")
        ;

        //Tenta alterar a conta
        Map<String, Object> contaRepetida = new HashMap<>();
        contaRepetida.put("nome", "Conta de luz");

        given()
                .spec(requisicaoPadrao)
                .header("Authorization", "JWT " + token)
                .contentType(ContentType.JSON)
                .body(contaRepetida)
        .when()
                .put("/contas/{id}", id)
        .then()
                .statusCode(400)
                .body("error", is("Já existe uma conta com esse nome!"))
        ;
    }

    @Test
    @DisplayName("[CT - 005] Deve inserir movimentação com sucesso")
    public void insereMovimentacao(){
        //Faz login e extrai o token
        Credenciais credenciais = new Credenciais("thaina@teste.com", "teste123");

        String token = given()
                .spec(requisicaoPadrao)
                .body(credenciais)
                .contentType(ContentType.JSON)
        .when()
                .post("/signin")
        .then()
                .statusCode(200)
                .extract().path("token")
        ;

        //Extrai o ID da conta
        Conta conta = new Conta("Conta teste");

        Integer id = given()
                .spec(requisicaoPadrao)
                .header("Authorization", "JWT " + token)
                .contentType(ContentType.JSON)
                .body(conta)
        .when()
                .post("/contas")
        .then()
                .statusCode(201)
                .extract().path("id")
        ;

        //Insere a movimentação
        Movimentacao movimentacao = new Movimentacao(
                id,
                "Teste",
                "Thainá",
                "DESP",
                "16/03/2026",
                "16/03/2026",
                100.00f,
                true);

        given()
                .spec(requisicaoPadrao)
                .contentType(ContentType.JSON)
                .header("Authorization", "JWT " + token)
                .body(movimentacao)
        .when()
                .post("/transacoes")
        .then()
                .statusCode(201)
        ;
    }

    @Test
    @DisplayName("[CT - 006] Deve validar campos obrigatórios da movimentação")
    public void validaCamposObrigatorios(){
        //Faz login e extrai o token
        Credenciais credenciais = new Credenciais("thaina@teste.com", "teste123");

        String token = given()
                .spec(requisicaoPadrao)
                .body(credenciais)
                .contentType(ContentType.JSON)
        .when()
                .post("/signin")
        .then()
                .statusCode(200)
                .extract().path("token")
        ;

        //Extrai o ID da conta
        Conta conta = new Conta("Testando conta");

        Integer id = given()
                .spec(requisicaoPadrao)
                .header("Authorization", "JWT " + token)
                .contentType(ContentType.JSON)
                .body(conta)
        .when()
                .post("/contas")
        .then()
                .statusCode(201)
                .extract().path("id")
        ;

        //Insere a movimentação
        Movimentacao movimentacao = new Movimentacao(
                id,
                "",
                "",
                "DESP",
                "",
                "",
                100f,
                true);

        given()
                .spec(requisicaoPadrao)
                .contentType(ContentType.JSON)
                .header("Authorization", "JWT " + token)
                .body(movimentacao)
        .when()
                .post("/transacoes")
        .then()
                .statusCode(400)
                .body("msg", hasItems(
                        "Data da Movimentação é obrigatório",
                        "Data do pagamento é obrigatório",
                        "Descrição é obrigatório",
                        "Interessado é obrigatório"
                ))
        ;
    }

    @Test
    @DisplayName("[CT - 007] Não deve cadastrar movimentação com data futura")
    public void movimentacaoDataFutura(){
        //Faz login e extrai o token
        Credenciais credenciais = new Credenciais("thaina@teste.com", "teste123");

        String token = given()
                .spec(requisicaoPadrao)
                .body(credenciais)
                .contentType(ContentType.JSON)
        .when()
                .post("/signin")
        .then()
                .statusCode(200)
                .extract().path("token")
        ;

        //Extrai o ID da conta
        Conta conta = new Conta("Petshop");

        Integer id = given()
                .spec(requisicaoPadrao)
                .header("Authorization", "JWT " + token)
                .contentType(ContentType.JSON)
                .body(conta)
        .when()
                .post("/contas")
        .then()
                .statusCode(201)
                .extract().path("id")
        ;

        //Insere a movimentação
        Movimentacao movimentacao = new Movimentacao(
                id,
                "Compra de ração",
                "Thainá",
                "DESP",
                "16/05/2026",
                "16/05/2026",
                100.00f,
                true);

        given()
                .spec(requisicaoPadrao)
                .contentType(ContentType.JSON)
                .header("Authorization", "JWT " + token)
                .body(movimentacao)
        .when()
                .post("/transacoes")
        .then()
                .statusCode(400)
                .body("msg", hasItem("Data da Movimentação deve ser menor ou igual à data atual"))
        ;
    }

    @Test
    @DisplayName("[CT - 008] Não deve remover conta com movimentação")
    public void naoDeletaConta(){
        //Faz login e extrai o token
        Credenciais credenciais = new Credenciais("thaina@teste.com", "teste123");

        String token = given()
                .spec(requisicaoPadrao)
                .body(credenciais)
                .contentType(ContentType.JSON)
        .when()
                .post("/signin")
        .then()
                .statusCode(200)
                .extract().path("token")
        ;

        //Extrai o ID da conta
        Conta conta = new Conta("Itens para construção");

        Integer id = given()
                .spec(requisicaoPadrao)
                .header("Authorization", "JWT " + token)
                .contentType(ContentType.JSON)
                .body(conta)
        .when()
                .post("/contas")
        .then()
                .statusCode(201)
                .extract().path("id")
        ;

        //Insere a movimentação
        Movimentacao movimentacao = new Movimentacao(
                id,
                "Cimento e areia",
                "Thainá",
                "DESP",
                "16/03/2026",
                "16/03/2026",
                100.00f,
                true);

        given()
                .spec(requisicaoPadrao)
                .contentType(ContentType.JSON)
                .header("Authorization", "JWT " + token)
                .body(movimentacao)
        .when()
                .post("/transacoes")
        .then()
                .statusCode(201)
        ;

        //Tenta exclui a conta
        given()
                .spec(requisicaoPadrao)
                .header("Authorization", "JWT " + token)
                .contentType(ContentType.JSON)
        .when()
                .delete("/contas/{id}", id)
        .then()
                .statusCode(500)
                .body("constraint", is("transacoes_conta_id_foreign"))
        ;
    }

    @Test
    @DisplayName("[CT - 009] Deve calcular saldo das contas")
    public void calculaSaldo(){
        //Faz login e extrai o token
        Credenciais credenciais = new Credenciais("thaina@teste.com", "teste123");

        String token = given()
                .spec(requisicaoPadrao)
                .body(credenciais)
                .contentType(ContentType.JSON)
        .when()
                .post("/signin")
        .then()
                .statusCode(200)
                .extract().path("token")
        ;

        //Extrai o ID da conta
        Conta conta = new Conta("Compras para o meu setup");

        Integer id = given()
                .spec(requisicaoPadrao)
                .header("Authorization", "JWT " + token)
                .contentType(ContentType.JSON)
                .body(conta)
        .when()
                .post("/contas")
        .then()
                .statusCode(201)
                .extract().path("id")
        ;

        //Insere 1° movimentação
        Movimentacao movimentacao1 = new Movimentacao(
                id,
                "Notebook",
                "Thainá",
                "REC",
                "16/03/2026",
                "16/03/2026",
                100.00f,
                true);

        given()
                .spec(requisicaoPadrao)
                .contentType(ContentType.JSON)
                .header("Authorization", "JWT " + token)
                .body(movimentacao1)
        .when()
                .post("/transacoes")
        .then()
                .statusCode(201)
        ;

        //Insere 2° movimentação
        Movimentacao movimentacao2 = new Movimentacao(
                id,
                "Mouse",
                "Thainá",
                "DESP",
                "16/03/2026",
                "16/03/2026",
                30f,
                true);

        given()
                .spec(requisicaoPadrao)
                .contentType(ContentType.JSON)
                .header("Authorization", "JWT " + token)
                .body(movimentacao2)
        .when()
                .post("/transacoes")
        .then()
                .statusCode(201)
        ;

        //Faz o calculo
        given()
                .spec(requisicaoPadrao)
                .header("Authorization", "JWT " + token)
        .when()
                .get("/saldo")
        .then()
                .statusCode(200)
                .body("find{it.conta_id == " + id + "}.saldo", is("70.00"))
        ;
    }

    @Test
    @DisplayName("[CT - 010] Deve remover movimentação")
    public void removeMovimentacao(){
        //Faz login e extrai o token
        Credenciais credenciais = new Credenciais("thaina@teste.com", "teste123");

        String token = given()
                .spec(requisicaoPadrao)
                .body(credenciais)
                .contentType(ContentType.JSON)
        .when()
                .post("/signin")
        .then()
                .statusCode(200)
                .extract().path("token")
        ;

        //Extrai o ID da conta
        Conta conta = new Conta("Compras Shopee");

        Integer idConta = given()
                .spec(requisicaoPadrao)
                .header("Authorization", "JWT " + token)
                .contentType(ContentType.JSON)
                .body(conta)
        .when()
                .post("/contas")
        .then()
                .statusCode(201)
                .extract().path("id")
        ;

        //Insere a movimentação
        Movimentacao movimentacao1 = new Movimentacao(
                idConta,
                "Itens da Shopee",
                "Thainá",
                "REC",
                "16/03/2026",
                "16/03/2026",
                100.00f,
                true);

        //Extrai o ID da movimentação
        Integer idMovimentacao = given()
                .spec(requisicaoPadrao)
                .contentType(ContentType.JSON)
                .header("Authorization", "JWT " + token)
                .body(movimentacao1)
        .when()
                .post("/transacoes")
        .then()
                .statusCode(201)
                .extract().path("id")
        ;

        //Exclui movimentação
        given()
                .spec(requisicaoPadrao)
                .header("Authorization", "JWT " + token)
        .when()
                .delete("/transacoes/{id}", idMovimentacao)
        .then()
                .statusCode(204)
        ;
    }
}
