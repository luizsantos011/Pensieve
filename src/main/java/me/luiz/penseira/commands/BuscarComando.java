package me.luiz.penseira.commands;

import me.luiz.penseira.contracts.IComando;
import me.luiz.penseira.contracts.ILembrancaRepository;
import me.luiz.penseira.service.PenseiraService;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Locale.filter;

public class BuscarComando implements IComando {
    private final ILembrancaRepository lembrancaRepository;
    private final BiConsumer<String, String> documentSender;

    public BuscarComando(ILembrancaRepository lembrancaRepository, BiConsumer<String, String> documentSender) {
        this.lembrancaRepository = lembrancaRepository;
        this.documentSender = documentSender;
    }

    @Override
    public void executar(SendMessage msg, Update update) {
        long userId = update.getMessage().getFrom().getId();
        String query = update.getMessage().getText().replace("/buscar", "").trim();
        if (query.isEmpty()) {
            msg.setText("Por favor, forneça um termo para buscar. Uso: /buscar [termo]");
            return;
        }
        List<String> allLines = lembrancaRepository.listFileMetadata();
        StringBuilder results = new StringBuilder();
        for (String line : allLines) {
            String[] data = line.split(";");
            long idOwner =Long.parseLong(data[0]);
            String folder = data[1].toLowerCase();
            String fileName = data[2].toLowerCase();
            String fileid = data[3];
            if(idOwner == userId && (fileName.contains(query) || folder.contains(query))) {
                results.append("📁 ").append(data[1]).append(" - ").append(data[2]).append("\n");
                SendDocument sendDoc = new SendDocument();
                sendDoc.setChatId(msg.getChatId());
                sendDoc.setDocument(new InputFile(fileid));
                sendDoc.setCaption("Arquivo encontrado: " + data[2]);
                documentSender.accept(msg.getChatId(), fileid);
            }
        }
        if(results.length() == 0){
            msg.setText("Nenhum arquivo encontrado para o termo: " + query);
        }else{
            msg.enableMarkdown(true);
            msg.setText("Resultados encontrados" + results.toString());
        }
    }
}
