package me.luiz.penseira.commands;

import me.luiz.penseira.contracts.IComando;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class ListarComando implements IComando {
    @Override
    public void executar(SendMessage mensagem, Update update) {
    }
}
