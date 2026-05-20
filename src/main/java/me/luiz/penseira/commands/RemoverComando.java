package me.luiz.penseira.commands;

import me.luiz.penseira.contracts.IComando;
import me.luiz.penseira.contracts.ILembrancaRepository;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

public class RemoverComando implements IComando {
    private final ILembrancaRepository lembrancaRepository;

    public RemoverComando(ILembrancaRepository lembrancaRepository) {
        this.lembrancaRepository = lembrancaRepository;
    }

    @Override
    public void executar(SendMessage msg, Update update) {
        long userId = update.getMessage().getFrom().getId();
        String query = update.getMessage().getText().replace("/remover", "").trim().toLowerCase();

        if (query.isEmpty()) {
            msg.setText("Por favor, forneça um termo para remover. Uso: /remover [termo]");
            return;
        }

        List<String> allLines = lembrancaRepository.listFileMetadata();
        List<String> linesToKeep = new ArrayList<>();
        boolean encontrou = false;

        for (String line : allLines) {
            String[] data = line.split(";");
            long idOwner = Long.parseLong(data[0]);
            String folder = data[1].toLowerCase();
            String fileName = data[2].toLowerCase();

            // Mesma lógica de validação do buscar
            if (idOwner == userId && (fileName.contains(query) || folder.contains(query))) {
                encontrou = true; // Achou o arquivo, então NÃO adiciona na lista de mantidos (deleta)
            } else {
                linesToKeep.add(line); // Não bateu com a busca, mantém o arquivo no TXT
            }
        }

        if (!encontrou) {
            msg.setText("Nenhum arquivo encontrado para remover com o termo: " + query);
        } else {
            lembrancaRepository.saveFileMetadata(linesToKeep); // Sobrescreve o arquivo com apenas os arquivos que devem ser mantidos
            msg.setText("Arquivo(s) relacionado(s) a '" + query + "' removido(s) com sucesso!");
        }
    }
}
