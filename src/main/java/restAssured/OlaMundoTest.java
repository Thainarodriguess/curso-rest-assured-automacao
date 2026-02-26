package restAssured;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


public class OlaMundoTest {

    public static RequestSpecification  requisicaoPadrao;
    public static ResponseSpecification respostaPadrao;

    @BeforeAll
    public static void setup(){
        requisicaoPadrao = new RequestSpecBuilder()
                .setBaseUri("https://restapi.wcaquino.me")
                .build();

        respostaPadrao = new ResponseSpecBuilder()
                .expectStatusCode(200)
                .build();
    }
    
    @Test
    public void testDeveValidarBody(){
        given()
             .spec(requisicaoPadrao)
       .when()
             .get("/ola")
       .then()
            .spec(respostaPadrao)
            .body(is("Ola Mundo!"))//Verifica se o corpo é EXATAMENTE igual a "Ola Mundo!" (case-sensitive)
            .body(containsString("Mundo"))//Verifica se o texto "Mundo" existe em QUALQUER parte do corpo da resposta
            .body(is(not(nullValue())))//Verifica se o valor NÃO é nulo (garante que a API retornou algum conteúdo)
       ;
    }

    @Test
    public void deveVerificarPrimeiroNivelJson(){
        given()
                .spec(requisicaoPadrao)
        .when()
             .get("/users/1")
        .then()
             .spec(respostaPadrao)
             .body("id", is(1))
             .body("name", containsString("da Silva"))
             .body("age", is(30))
        ;
    }

    @Test
    public void deveVerificarSegundoNivelJson(){
        given()
              .spec(requisicaoPadrao)
        .when()
             .get("/users/2")
        .then()
             .spec(respostaPadrao)
             .body("name", containsString("Maria Joaquina"))//Verifica primeiro nível do Json
             .body("endereco.rua", is("Rua dos bobos"))//Verifica segundo nível do Json
        ;
    }

    @Test
    public void deveVerificarLista(){
        given()
                .spec(requisicaoPadrao)
        .when()
                .get("/users/3")
        .then()
                .spec(respostaPadrao)
                .body("name", containsString("Ana Júlia"))//Verifica primeiro nível do Json
                .body("filhos", hasSize(2))//Verifica se a lista tem dois registros
                .body("filhos[0].name", is("Zezinho"))//Faz a verificação dentro da lista filhos na posição 0
                .body("filhos[1].name", is("Luizinho"))//Faz a verificação dentro da lista filhos na posição 1
        ;
    }

    @Test
    public void deveRetornarErroUsuarioInexistente(){
        given()
                .spec(requisicaoPadrao)
        .when()
                .get("/users/4")
        .then()
                .statusCode(404)
                .body("error", is("Usuário inexistente"))
        ;
    }

    @Test
    public void deveVerificarListaRaiz(){
        given()
                .spec(requisicaoPadrao)
        .when()
                .get("/users")
        .then()
                .spec(respostaPadrao)
                .body("$", hasSize(3))//$ é uma convenção, não é obrigatório(pode deixar também vazio "") e faz a busca na raiz da lista
                .body("name", hasItems("João da Silva", "Maria Joaquina", "Ana Júlia" ))//Busca atributo pelo nome
                .body("age[1]", is(25))//Busca pela idade no index 1
                .body("filhos.name", hasItems(Arrays.asList("Zezinho", "Luizinho")))//Busca por uma lista dentro de outra lista
                .body("salary", contains(1234.5678f, 2500, null))//Busca pelo salário
                ;

    }

    @Test
    public void deveFazerVerificacoesAvancadas(){
        given()
                .spec(requisicaoPadrao)
        .when()
                .get("/users")
        .then()
                .spec(respostaPadrao)
                .body("$", hasSize(3))
                .body("age.findAll{it <= 25}.size()", is(2))//Busca por idades menores ou igual a 25
                .body("age.findAll{it <= 25 && it > 20}.size()", is(1))//Busca por usuários com mais de 20 anos e até 25
                .body("findAll{it.age <= 25 && it.age > 20}.name", hasItem("Maria Joaquina"))//Busca pelo nome dos usuários que passaram pelo filtro
                .body("findAll{it.age <= 25}[0].name", is("Maria Joaquina"))//Busca pelo primeiro elemento da lista que tem menos de 25 anos
                .body("find{it.age <= 25}.name", is("Maria Joaquina"))//FindAll traz tudo e Find traz apenas um
                .body("findAll{it.name.contains('n')}.name", hasItems("Maria Joaquina", "Ana Júlia"))//Busca todos os elementos que contém a letra n
                .body("findAll{it.name.length() > 10}.name", hasItems("João da Silva", "Maria Joaquina"))//Busca por elementos que possuem um tamanho maior que 10 caracteres
                .body("name.collect{it.toUpperCase()}", hasItem("MARIA JOAQUINA"))//Itera a lista e faz uma transformação na lista. Passa para UperCase
        ;
    }

}
