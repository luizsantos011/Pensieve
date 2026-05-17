package me.luiz.penseira.commands;

import me.luiz.penseira.bot.TelegramBot;
import me.luiz.penseira.contracts.IComando;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class StatusComando implements IComando {
    private final TelegramBot bot;

    public StatusComando(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public void executar(SendMessage msg, Update update) {
        msg.setText("Mensagens processadas: " + bot.getMessageCounter());
    }
}
