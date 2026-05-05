package me.luiz.penseira.Services;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class TelegramService extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "MyPensieveHP_bot";
    }

    @Override
    public String getBotToken() {
        return "8299391738:AAE7lS02qQCqXMXZBJ2NWRzlm_uMCWhG70E";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String texto = update.getMessage().getText();
            System.out.println("Mensagem recebida: " + texto);
        }
    }
}
