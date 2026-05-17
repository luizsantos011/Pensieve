package me.luiz.penseira.contracts;

import java.util.List;

public interface ILembrancaRepository {
    void salvar(String conteudo);
    List<String> listar();
    void saveFileMetadata(String content);
    List<String> listFileMetadata();
}
