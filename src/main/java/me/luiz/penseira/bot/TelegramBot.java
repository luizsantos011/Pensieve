package me.luiz.penseira.bot;

import io.github.cdimascio.dotenv.Dotenv;
import me.luiz.penseira.Exceptions.AcessoNegadoException;
import me.luiz.penseira.Exceptions.AltaFrequenciaDeMensagensException;
import me.luiz.penseira.Exceptions.ArquivoInvalidoException;
import me.luiz.penseira.Exceptions.TamanhoMensagemInvalidoException;
import me.luiz.penseira.contracts.IComando;
import me.luiz.penseira.contracts.ILembrancaRepository;
import me.luiz.penseira.contracts.ILogger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.Document;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class TelegramBot extends TelegramLongPollingBot {
    private final ILogger logger;
    private final ILembrancaRepository lembrancaRepository;
    private Map<String, IComando> commandsMap = new HashMap<>();

    private final Dotenv dotenv = Dotenv.load();
    private final String channelId = dotenv.get("TELEGRAM_CHANNEL_ID");
    private final List<Long> whitelist = loadWhitelist();
    private final Map<Long, Long> lastMessageTimestampByUser = new HashMap<>();
    private int messageCounter = 0;

    public TelegramBot(ILogger logger,  ILembrancaRepository lembrancaRepository, Map<String, IComando> commandsMap) {
        this.commandsMap = commandsMap;
        this.logger = logger;
        this.lembrancaRepository = lembrancaRepository;
    }

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
        try {
            processUpdate(update);
        } catch (RuntimeException e) {
            logger.registrarLog("Tentativa de acesso por usuario não autorizado.");
        } catch (Exception e) {
            logger.registrarLog("Erro inesperado." + e.getMessage());
        }
    }

    private void processUpdate(Update update) {
        try{
            if (!update.hasMessage()) return;

            messageCounter++;
            logger.registrarLog("Mensagem recebida!");

            var receivedMessage = update.getMessage();
            long userId = receivedMessage.getFrom().getId();
            String chatId = receivedMessage.getChatId().toString();

            validateUser(userId);
            validateFrequency(userId);

            if(receivedMessage.hasText()) {
                handleText(chatId, receivedMessage.getText(), update);
            }else if(receivedMessage.hasDocument()) {
                String userCaption = receivedMessage.getCaption();
                handleDocument(receivedMessage.getDocument(), chatId, userId, userCaption);
            }

        }catch (AcessoNegadoException | AltaFrequenciaDeMensagensException | TamanhoMensagemInvalidoException e){
            logger.registrarLog("erro de negocio");
            throw new RuntimeException(e.getMessage());
        }catch (Exception e){
            logger.registrarLog("Erro inesperado." + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private void handleText(String chatId, String messageText, Update update) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        if(messageText.length() > 500){
            throw new TamanhoMensagemInvalidoException("Tamanho da mensagem excede o limite permitido.");
        }
        String commandKey = messageText.split(" ")[0];
        IComando comando = commandsMap.get(commandKey);
        if(comando != null) {
            comando.executar(msg, update);
            executeResponse(msg);
        }else {
            lembrancaRepository.salvar(messageText);
            msg.setText("Lembrança salva com sucesso!");
            executeResponse(msg);
        }
    }

    private void handleDocument(Document document, String chatId,  long userId, String userCaption) {
        String fileName = document.getFileName();
        List<String> allowedExtensions = Arrays.asList(".txt", ".docx", ".pdf");
        boolean allowed = allowedExtensions.stream().anyMatch(ext -> fileName.toLowerCase().endsWith(ext));
        if(allowed){
            String fileId = document.getFileId();
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(channelId);
            sendDocument.setDocument(new org.telegram.telegrambots.meta.api.objects.InputFile(fileId));
            String folder = (userCaption != null && !userCaption.isBlank()) ? userCaption : "Geral";
            sendDocument.setCaption(folder + " | Dono: " + userId + " | arquivo " + document.getFileName());
            try{
                execute(sendDocument);
                String metadataLine = userId + ";" + folder + ";" + fileName + ";" + document.getFileId();
                lembrancaRepository.saveFileMetadata(metadataLine);
            }catch (TelegramApiException e){
                throw new RuntimeException("Erro ao tentar criar o arquivo guardado!");
            }
            SendMessage msg = new SendMessage();
            msg.setChatId(chatId);
            msg.setText("Documento recebido e armazenado com sucesso!");
            executeResponse(msg);
        }else {
            throw new ArquivoInvalidoException("Tipo de arquivo não permitido. Envie um arquivo com extensão .txt, .docx ou .pdf.");
        }
    }

    private void executeResponse(SendMessage msg) {
        try{
            execute(msg);
        }catch (TelegramApiException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    private void validateUser(long userId) {
        if(!whitelist.contains(userId)) throw new AcessoNegadoException("Acesso negado para o usuário: " + userId);
    }

    private void validateFrequency(long userId) {
        long now = System.currentTimeMillis();
        if(lastMessageTimestampByUser.containsKey(userId)){
            long lastMessageTime = lastMessageTimestampByUser.get(userId);
            if(now - lastMessageTime < 5000){
                throw new AltaFrequenciaDeMensagensException("Alta frequência de mensagens detectada para o usuário: " + userId);
            }
        }
        lastMessageTimestampByUser.put(userId, now);
    }

    private List<Long> loadWhitelist() {
        String allIds = dotenv.get("BOT_USER_ID");
        return Arrays.stream(allIds.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    private enum Command {
        START("/start"),
        STATUS("/status"),
        TEMPO("/tempo"),
        BUSCAR("/buscar"),
        LIMPAR("/limpar"),
        AJUDA("/ajuda");

        private final String commandValue;

        Command(String commandValue) {
            this.commandValue = commandValue;
        }
    }

    public int getMessageCounter() {
        return messageCounter;
    }
}