package me.luiz.penseira.service;

import me.luiz.penseira.contracts.ILembrancaRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

public class PenseiraService implements ILembrancaRepository {
    private final Path arquivoMemorias;
    private final Path fileMetadata = Paths.get("file_metadata.txt");

    public PenseiraService(Path arquivoMemorias) {
        this.arquivoMemorias = arquivoMemorias;
    }
    @Override
    public void salvar(String conteudo) {
        try{
            Files.writeString(arquivoMemorias, conteudo + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> listar() {
        try {
            if(!Files.exists(arquivoMemorias)) return Collections.emptyList();
            return Files.readAllLines(arquivoMemorias);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveFileMetadata(String content) {
        try{
            Files.writeString(fileMetadata, content + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> listFileMetadata() {
        try {
            if(!Files.exists(fileMetadata)) return Collections.emptyList();
            return Files.readAllLines(fileMetadata);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void atualizarMetadata(List<String> linhas) {
        try {
            java.nio.file.Files.write(
                    this.fileMetadata,
                    linhas,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (java.io.IOException e) {
            throw new RuntimeException("Erro ao atualizar o arquivo de metadados: " + e.getMessage());
        }
    }
}
