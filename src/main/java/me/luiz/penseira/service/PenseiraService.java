package me.luiz.penseira.service;

import me.luiz.penseira.contracts.ILembrancaRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

public class PenseiraService implements ILembrancaRepository {
    private final Path arquivoMemorias;

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
}
