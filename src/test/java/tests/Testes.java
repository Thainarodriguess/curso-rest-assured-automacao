package tests;

import barrigaRest.client.ContasClient;
import barrigaRest.client.LoginClient;
import barrigaRest.client.BaseClient;
import barrigaRest.client.MovimentacaoClient;
import barrigaRest.model.Conta;
import barrigaRest.model.Credenciais;
import barrigaRest.model.Movimentacao;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.*;

public class Testes extends BaseClient{

    private static LoginClient loginClient = new LoginClient();
    private static ContasClient contasClient = new ContasClient();
    private static MovimentacaoClient movimentacaoClient = new MovimentacaoClient();

    @BeforeAll
    public static void login(){
       Credenciais credenciais = new Credenciais("thaina@teste.com", "teste123");
       TOKEN = loginClient.geraToken(credenciais);
    }

    @Test
    @DisplayName("[CT - 001] Não deve acessar API sem token")
    public void verificarAusenciaDeToken(){
        contasClient.listarSemToken()
        .then()
                .statusCode(401)
        ;
    }

    @Test
    @DisplayName("[CT - 002] Deve incluir conta com sucesso")
    public void insereConta(){
        Conta conta = new Conta("Cartão de crédito");

        contasClient.inserirConta(conta)
            .then()
                .statusCode(201)
            ;
    }

   @Test
    @DisplayName("[CT - 003] Deve alterar conta com sucesso")
    public void alteraConta(){
        //Inserindo uma nova conta
        Conta conta = new Conta("Cartão de débito");

        //Extrai o id
       Integer id = contasClient.extraiId(conta);

       //Altera a conta
       conta.setNome("Conta alterada");
       contasClient.alterarConta(id, conta)
            .then()
                .statusCode(200)
                .body("nome", is("Conta alterada"))
            ;
    }

    @Test
    @DisplayName("[CT - 004] Não deve alterar conta com nome repetido")
    public void contaNomeRepetido(){
        //Cria uma nova conta
        Conta conta = new Conta("Boletos");
        contasClient.inserirConta(conta);

        //Cria conta para tentar ser alterada e extrai o ID
        Conta contaAlterada = new Conta("Testando alteração");
        Integer id = contasClient.extraiId(contaAlterada);

        //Tentativa de alteração
        contaAlterada.setNome("Boletos");

        contasClient.alterarConta(id, contaAlterada)
             .then()
                .statusCode(400)
                .body("error", is("Já existe uma conta com esse nome!"))
             ;
    }

    @Test
    @DisplayName("[CT - 005] Deve inserir movimentação com sucesso")
    public void insereMovimentacao(){
        //Extrai o ID da conta
        Conta conta = new Conta("Nova conta");
        Integer id = contasClient.extraiId(conta);

        //Insere a movimentação
        Movimentacao movimentacao = new Movimentacao(
                id,
                "Teste",
                "Thainá",
                "DESP",
                "17/03/2026",
                "17/03/2026",
                1000.00f,
                true);
        movimentacaoClient.insereMovimentacao(movimentacao)
        .then()
                .statusCode(201)
        ;
    }

    @Test
    @DisplayName("[CT - 006] Deve validar campos obrigatórios da movimentação")
    public void validaCamposObrigatorios(){
        //Extrai o ID da conta
        Conta conta = new Conta("Construção da minha casa");
        Integer id = contasClient.extraiId(conta);

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

        movimentacaoClient.insereMovimentacao(movimentacao)
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
        //Extrai o ID da conta
        Conta conta = new Conta("Conta Petshop");
        Integer id = contasClient.extraiId(conta);

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

       movimentacaoClient.insereMovimentacao(movimentacao)
        .then()
                .statusCode(400)
                .body("msg", hasItem("Data da Movimentação deve ser menor ou igual à data atual"))
        ;
    }

    @Test
    @DisplayName("[CT - 008] Não deve remover conta com movimentação")
    public void naoDeletaConta(){
        //Extrai o ID da conta
        Conta conta = new Conta("Boleto bancário");
        Integer id = contasClient.extraiId(conta);

        //Insere a movimentação
        Movimentacao movimentacao = new Movimentacao(
                id,
                "Pagar banco",
                "Thainá",
                "DESP",
                "17/03/2026",
                "17/03/2026",
                50.00f,
                true);

        movimentacaoClient.insereMovimentacao(movimentacao);

        //Tenta exclui a conta
       contasClient.removeConta(id)
        .then()
                .statusCode(500)
                .body("constraint", is("transacoes_conta_id_foreign"))
        ;
    }


    @Test
    @DisplayName("[CT - 009] Deve remover movimentação")
    public void removeMovimentacao(){
        //Extrai o ID da conta
        Conta conta = new Conta("Roupas");
        Integer idConta = contasClient.extraiId(conta);

        //Insere a movimentação
        Movimentacao movimentacao = new Movimentacao(
                idConta,
                "Itens da Shopee",
                "Thainá",
                "REC",
                "17/03/2026",
                "17/03/2026",
                100.00f,
                true);

        //Extrai o ID da movimentação
        Integer idMovimentacao = movimentacaoClient.extraiIdMovimentacao(movimentacao);

        //Exclui movimentação
        movimentacaoClient.removeMovimentacao(idMovimentacao)
        .then()
                .statusCode(204)
        ;
    }
}
