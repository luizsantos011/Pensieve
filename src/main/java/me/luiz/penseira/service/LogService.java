package me.luiz.penseira.service;

import me.luiz.penseira.contracts.ILogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogService implements ILogger {
    private final Path caminhoLog;

    public LogService(Path caminhoLog) {
        this.caminhoLog = criarArquivo(caminhoLog);
    }

    @Override
    public void registrarLog(String mensagem) {
        try(var writer = Files.newBufferedWriter(caminhoLog, java.nio.file.StandardOpenOption.APPEND)) {
            String dataEHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            writer.write("[" + dataEHora + "] "  + mensagem);
            writer.newLine();
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    private Path criarArquivo(Path caminhoLog) {
        try {
            if (caminhoLog.getParent()  != null) {
                Files.createDirectory(caminhoLog.getParent());
            }
            if (!Files.exists(caminhoLog)) {
                Files.createFile(caminhoLog);
            }
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar o arquivo de log.", e);
        }
        return caminhoLog;
    }
}
