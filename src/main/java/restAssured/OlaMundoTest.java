package restAssured;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
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

    @Test
    public void deveSalvarUsuario(){
        given()
                .spec(requisicaoPadrao)
                .contentType("application/json")
                .body("{ \"name\": \"Thainá\", \"age\": 28 }")
        .when()
                .post("/users")
        .then()
                .statusCode(201)
        ;
    }

    @Test
    public void deveAlterarUsuario(){
        given()
                .spec(requisicaoPadrao)
                .contentType("application/json")
                .body("{ \"name\": \"João Silva\"}")
        .when()
                .put("/users/1")
        .then()
                .spec(respostaPadrao)
        ;
    }

    //Serialização(Converte um MAP para JSON)
    @Test
    public void deveSalvarUsuarioUsandoMap(){
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "Usuário via Map");
        params.put("age", 18);

        given()
                .spec(requisicaoPadrao)
                .contentType("application/json")
                .body(params)
        .when()
                .post("/users")
        .then()
                .statusCode(201)
                .body("id", is(notNullValue()))
                .body("name", is("Usuário via Map"))
                .body("age", is(18))
        ;
    }

    //Serialização(Converte um objeto para JSON)
    @Test
    public void deveSalvarUsuarioUsandoObjeto(){
        User user = new User("Usuário via objeto", 25);
        given()
                .spec(requisicaoPadrao)
                .contentType("application/json")
                .body(user)
        .when()
                .post("/users")
        .then()
                .statusCode(201)
                .body("id", is(notNullValue()))
                .body("name", is("Usuário via objeto"))
                .body("age", is(25))
        ;
    }

    //Desserialização
    @Test
    public void deveDesserializarObjetoAoSalvarUsuario(){
        User user = new User("Usuário desserializado", 55);
        User usuarioInserido = given()
                .spec(requisicaoPadrao)
                .contentType("application/json")
                .body(user)
        .when()
                .post("/users")
        .then()
                .statusCode(201)
                .extract().body().as(User.class)
        ;

        Assertions.assertEquals("Usuário desserializado", usuarioInserido.getName());
        assertThat(usuarioInserido.getAge(), is(55));
    }

    //Query
    @Test
    public void deveEnviarValorViaQuery(){
        given()
        .when()
                .get("https://restapi.wcaquino.me/v2/users?format=json")
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
        ;
    }

    @Test
    public void deveEnviarValorViaQueryParam(){
        given()
                .queryParam("format", "json")
        .when()
                .get("https://restapi.wcaquino.me/v2/users")
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
        ;
    }

    //Header
    @Test
    public void deveEnviarValorViaHeader(){
        given()
                .accept(ContentType.JSON)
        .when()
                .get("https://restapi.wcaquino.me/v2/users")
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
        ;
    }

    //Upload
    @Test
    public void deveFazerUploadArquivo(){
        given()
                .multiPart("arquivo", new File("src/main/java/restAssured/img.png"))
        .when()
                .get("https://restapi.wcaquino.me/v2/users")
        .then()
                .statusCode(200)

        ;
    }

    @Test
    public void naoDeveFazerUploadArquivoAcimaDeUmSegundo(){
        given()
                .multiPart("arquivo", new File("src/main/img.png"))
        .when()
                .post("https://restapi.wcaquino.me/v2/upload")
        .then()
                .time(lessThan(1000L))
                .statusCode(200)
        ;
    }

    //Acessando API pública
    @Test
    public void deveAcessarSWAPI(){
        given()
        .when()
                .get("https://swapi.dev/api/people/1")
        .then()
                .statusCode(200)
                .body("name", is("Luke Skywalker"))
        ;
    }

    //Acessando API com chave(chave: 5a560e3737d45063db32a63c6eceab71)
    /*@Test
    public void deveObterClima(){
        given()
                .queryParam("q", "Fortaleza")
                .queryParam("appid", "5a560e3737d45063db32a63c6eceab71")
        .when()
                .get("https://api.openweathermap.org/data/2.5/weather")
        .then()
                .statusCode(200)
                .body("name", is("Fortaleza"))
        ;
    }*/

    //Autenticação básica
    @Test
    public void naoDeveAcessarSemSenha(){
        given()
        .when()
                .get("https://restapi.wcaquino.me/basicauth")
        .then()
                .statusCode(401)
        ;
    }

    @Test
    public void deveFazerAutenticacaoBasica(){
        given()
        .when()
                .get("https://admin:senha@restapi.wcaquino.me/basicauth")
        .then()
                .statusCode(200)
                .body("status", is("logado"))
        ;
    }

    @Test
    public void deveFazerAutenticacaoBasica2(){
        given()
                .auth().basic("admin", "senha")
        .when()
                .get("https://restapi.wcaquino.me/basicauth")
        .then()
                .statusCode(200)
                .body("status", is("logado"))
        ;
    }

    //Autenticação com token JWT
    @Test
    public void deveFazerAutenticacaoTokenJWT(){
        Map<String, Object> login = new HashMap<String, Object>();
        login.put("email", "thaina@teste.com");
        login.put("senha", "teste123");

        //Faz login na API e extrai o token
        String token = given()
                .body(login)
                .contentType(ContentType.JSON)
        .when()
                .post("https://barrigarest.wcaquino.me/signin")
        .then()
                .statusCode(200)
                .extract().path("token")
        ;

        //Obtém as contas
        given()
                .header("Authorization", "JWT " + token)
        .when()
                .get("https://barrigarest.wcaquino.me/contas")
        .then()
                .statusCode(200)
                .body("nome", hasItem("conta teste"))
        ;
    }
}