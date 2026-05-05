package me.luiz.penseira.bot;

import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class TelegramBot extends TelegramLongPollingBot {
    private final Dotenv dotenv = Dotenv.load();
    private final long MEU_ID = Long.parseLong(dotenv.get("BOT_USER_ID"));

    @Override
    public String getBotUsername() {
        return "MyPensieveHP_bot";
    }

    @Override
    public String getBotToken() {
        return dotenv.get("BOT_TOKEN");
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            var message = update.getMessage();
            long userId = message.getFrom().getId();
            if(userId != MEU_ID){
                System.out.println("saia daqui caba safado!!");
                return;
            }
            System.out.println("Mensagem recebida de " + userId);
            if (message.hasText()) {
                String mensagem = message.getText();
                System.out.println();
            }else if (message.hasDocument()) {
                System.out.println("Recebido um documento de " + userId);
            }
        }
    }
}
