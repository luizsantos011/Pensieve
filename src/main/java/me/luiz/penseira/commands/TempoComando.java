package me.luiz.penseira.commands;

import me.luiz.penseira.contracts.IComando;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TempoComando implements IComando {

    @Override
    public void executar(SendMessage msg, Update update) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formattedTime = now.format(timeFormatter);
        msg.setText("Data e hora atuais: " + formattedTime);
    }
}
