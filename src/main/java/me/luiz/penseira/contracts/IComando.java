package me.luiz.penseira.contracts;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface IComando {
    void executar(SendMessage mensagem, Update update);
}
