package me.luiz.penseira.Controller;

import me.luiz.penseira.bot.TelegramBot;
import me.luiz.penseira.commands.StartComando;
import me.luiz.penseira.commands.StatusComando;
import me.luiz.penseira.contracts.IComando;
import me.luiz.penseira.contracts.ILembrancaRepository;
import me.luiz.penseira.contracts.ILogger;
import me.luiz.penseira.service.LogService;
import me.luiz.penseira.service.PenseiraService;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class BotController {
    private final Path caminhoArquivo = Paths.get("memorias.txt");
    private final ILogger logger;
    private final ILembrancaRepository penseiraService;
    private final IComando comando;

    public BotController() {
        this.logger = new LogService(Paths.get("logs.txt"));
        this.comando = new StartComando();
        this.penseiraService = new PenseiraService(caminhoArquivo);
    }

    public void iniciarBot(){
        try{
            Map<String, IComando> comandos = new HashMap<>();
            comandos.put("/start", new StartComando());
            comandos.put("/status", new StatusComando());
            TelegramBot bot = new TelegramBot(logger, penseiraService);
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            logger.registrarLog("Bot iniciado com sucesso.");

        }catch (Exception e){
            logger.registrarLog("Erro ao iniciar o bot: " + e.getMessage());
        }
    }
}
