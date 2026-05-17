package me.luiz.penseira.commands;

import me.luiz.penseira.contracts.IComando;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class StartComando implements IComando {

    @Override
    public void executar(SendMessage msg, Update update) {
        String menu = "Olá! Eu sou o Pensieve, seu assistente pessoal de notas e lembretes.\n\n" +
                "Aqui estão os comandos disponíveis:\n" +
                "/start   - Inicializa o assistente e exibe este menu\n" +
                "/status  - Mostra o status do sistema e o contador de mensagens\n" +
                "/tempo   - Exibe a data e hora atual do servidor\n" +
                "/buscar  - Exibe todas as notas ou lembretes\n" +
                "/limpar  - Remove notas ou lembretes\n" +
                "/ajuda   - Use se precisar de ajuda em relação aos comandos\n\n" +
                "Se você enviar qualquer texto comum (sem /), eu o salvarei automaticamente como uma nova nota.";

        msg.setText(menu);
    }
}
