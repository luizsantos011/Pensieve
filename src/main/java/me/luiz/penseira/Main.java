package me.luiz.penseira;

import me.luiz.penseira.bot.TelegramBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Main {
    public static void main(String[] args) {
        try{
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TelegramBot());
            System.out.println("As águas da penseira estão calmas… prontas para receber novas memórias.");
        }catch (TelegramApiException e){
            e.printStackTrace();
        }
    }
}
