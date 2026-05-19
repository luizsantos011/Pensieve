package me.luiz.penseira.Controller;

import me.luiz.penseira.bot.TelegramBot;
import me.luiz.penseira.commands.*;
import me.luiz.penseira.commands.TempoComando;
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
    private final TelegramBot bot;
    private final ILembrancaRepository lembrancaRepository;
    private final Map<String, IComando> comandos = new HashMap<>();

    public BotController() {
        this.logger = new LogService(Paths.get("logs.txt"));
        this.lembrancaRepository = new PenseiraService(caminhoArquivo);
        this.bot = new TelegramBot(logger, lembrancaRepository, comandos);
    }

    public void iniciarBot(){
        try{
            inicializarComandos();
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            logger.registrarLog("Bot iniciado com sucesso.");
        }catch (Exception e){
            logger.registrarLog("Erro ao iniciar o bot: " + e.getMessage());
        }
    }

    private void inicializarComandos(){
        comandos.put("/start", new StartComando());
        comandos.put("/status", new StatusComando(bot));
        comandos.put("/tempo", new TempoComando());
        comandos.put("/ajuda", new AjudaComando());
        comandos.put("/limpar", new LimparComando());

        comandos.put("/buscar", new BuscarComando(lembrancaRepository, (chatId, fileId) -> {
            try {
                org.telegram.telegrambots.meta.api.methods.send.SendDocument sendDocument =
                        new org.telegram.telegrambots.meta.api.methods.send.SendDocument();
                sendDocument.setChatId(chatId);
                sendDocument.setDocument(new org.telegram.telegrambots.meta.api.objects.InputFile(fileId));

                bot.execute(sendDocument);
            } catch (Exception e) {
                logger.registrarLog("Erro ao disparar documento via Lambda no BuscarComando: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }));
    }
}
