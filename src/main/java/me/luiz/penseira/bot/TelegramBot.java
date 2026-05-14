package me.luiz.penseira.bot;

import io.github.cdimascio.dotenv.Dotenv;
import me.luiz.penseira.Exceptions.AcessoNegadoException;
import me.luiz.penseira.contracts.IComando;
import me.luiz.penseira.contracts.ILogger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class TelegramBot extends TelegramLongPollingBot {
    private final ILogger logger;
    private final Dotenv dotenv = Dotenv.load();
    private final List<Long> whitelist = loadWhitelist();
    private final Map<Long, Long> lastMessageTimestampByUser = new HashMap<>();
    private final Map<String, IComando> commandsMap = new HashMap<>();
    private static final long MIN_INTERVAL_MS = 5000;
    private int messageCounter = 0;
    private final Map<String, String> dictionaryMap = new HashMap<>();

    public TelegramBot(ILogger logger) {
        initializeCommands();
        initializeDictionary();
        this.logger = logger;
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
        } catch (AcessoNegadoException e) {
            logger.registrarLog("Tentativa de acesso por usuario não autorizado.");
        } catch (Exception e) {
            logger.registrarLog("Erro inesperado." + e.getMessage());
        }
    }

    private void processUpdate(Update update) {
        if (update.hasMessage()) {
            messageCounter++;
            logger.registrarLog("mensagem recebida.");
            var receivedMessage = update.getMessage();
            long userId = receivedMessage.getFrom().getId();
            SendMessage msg = new SendMessage();

            if (!whitelist.contains(userId)) {
                throw new AcessoNegadoException("Apenas bruxos autorizados podem acessar esta penseira.");
            }

            long now = System.currentTimeMillis();
            if (lastMessageTimestampByUser.containsKey(userId)) {
                long lastMessageTime = lastMessageTimestampByUser.get(userId);
                long difference = now - lastMessageTime;
                if (difference < MIN_INTERVAL_MS) {
                    System.out.println("Bruxo " + userId + " está conjurando feitiços rápidos demais! ignorando...");
                    return;
                }
            }
            lastMessageTimestampByUser.put(userId, now);
            System.out.println("Bruxo " + userId + " acessou a penseira.");
            System.out.println("Mensagem recebida de " + userId);

            if (receivedMessage.hasText()) {
                messageCounter++;
                String messageText = receivedMessage.getText();
                String userFirstName = receivedMessage.getFrom().getFirstName();
                msg.setChatId(receivedMessage.getChatId().toString());
                IComando command = commandsMap.get(messageText);

                if (messageText.length() > 500) {
                    System.out.println("Bruxo " + userFirstName + " tentou conjurar um feitiço muito complexo! A Penseira não pode processar mensagens tão longas.");
                    return;
                }

                if (command != null) {
                    command.executar(msg, update);
                } else {
                    String definition = dictionaryMap.get(messageText.toLowerCase());
                    if (definition != null) {
                        msg.setText(definition);
                        try {
                            execute(msg);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        System.out.println("A Penseira recebeu uma instrução que não pôde ser reconhecida.");
                    }
                }
            } else if (receivedMessage.hasDocument()) {
                messageCounter++;
                var doc = receivedMessage.getDocument();
                String fileName = doc.getFileName().toLowerCase();
                List<String> allowedExtensions = Arrays.asList(".txt", ".jpg", ".pdf", "png");
                boolean isSafe = allowedExtensions.stream().anyMatch(fileName::endsWith);
                if (isSafe) {
                    System.out.println("Memória recebida. A penseira a armazenou com sucesso.");
                } else {
                    System.out.println("Não foi possível armazenar esta memória.");
                }
            }
        }
    }

    private List<Long> loadWhitelist() {
        String allIds = dotenv.get("BOT_USER_ID");
        return Arrays.stream(allIds.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    private void initializeCommands() {
        commandsMap.put("/start", (msg, update) -> {
            msg.setText("A Penseira recebeu uma instrução que não pôde ser reconhecida.\n" +
                    "\n" +
                    "Para navegar corretamente por este ambiente, utilize um dos comandos abaixo:\n" +
                    "\n" +
                    "/start - abrir acesso às águas da Penseira\n" +
                    "/guardar — confiar uma nova memória à Penseira\n" +
                    "/memorias — revisitar fragmentos armazenados\n" +
                    "/remover — desfazer o vínculo de uma memória\n" +
                    "/ajuda — consultar instruções disponíveis\n" +
                    "\n" +
                    "A Penseira responde apenas a instruções válidas.\n");
            try {
                execute(msg);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        });
        commandsMap.put("/status", (msg, update) -> {
            msg.setText("A Penseira recebeu " + messageCounter + " mensagens desde que foi ativada.");
            try {
                execute(msg);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        });
        commandsMap.put("/tempo", (msg, update) -> {
            msg.setText("As águas da Penseira se movem com o fluxo do tempo… e agora indicam: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            try {
                execute(msg);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        });
        commandsMap.put("/reescrever", (msg, update) -> {
            String fullMessage = update.getMessage().getText().replace("/reescrever", "").trim();
            String reversedMessage = fullMessage.isEmpty() ? "A Penseira espera uma mensagem para reescrever." : new StringBuilder(fullMessage).reverse().toString();
            msg.setText(reversedMessage);
            try {
                execute(msg);
            } catch (TelegramApiException e) {}
        });
        commandsMap.put("/sorteio", (msg, update) -> {
            String quotes = "O que parece pequeno hoje ecoará em caminhos inesperados;\n" +
                    "O fluxo do dia carrega mais do que os olhos podem compreender;\n" +
                    "Aquilo que você ignora ainda assim permanece ao seu redor;\n" +
                    "O inesperado já começou a se formar antes mesmo de ser notado;\n" +
                    "As escolhas de agora projetam sombras que ainda não se revelaram;\n" +
                    "A clareza virá quando o ruído perder força;\n" +
                    "Um gesto simples poderá alterar a direção de tudo ao redor;\n" +
                    "O que está confuso agora apenas ainda não encontrou sua forma;\n" +
                    "A resposta pode surgir antes mesmo da pergunta ser feita;\n" +
                    "A estabilidade é apenas o intervalo entre mudanças;\n" +
                    "Nem todo caminho visível leva ao destino que parece prometer;\n" +
                    "Algo que você considera fixo já está em movimento;\n" +
                    "O silêncio carrega mais do que palavras ousam revelar;\n" +
                    "Um desvio inesperado interromperá a ordem habitual do dia;\n" +
                    "O que foi ignorado encontrará forma novamente;\n" +
                    "Um encontro aparentemente casual não será casual por completo;\n" +
                    "Decisões pequenas não permanecem pequenas por muito tempo;\n" +
                    "Algo esquecido ainda exerce influência sobre o presente;\n" +
                    "O dia resistirá a seguir um padrão previsível;\n" +
                    "O equilíbrio será ajustado por detalhes quase invisíveis";
            String[] quotesList = quotes.replace("\n", "").split(";");
            String selectedQuote = quotesList[new Random().nextInt(quotesList.length)];
            msg.setText(selectedQuote);
            try {
                execute(msg);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void initializeDictionary() {
        dictionaryMap.put("penseira", "Um recipiente mágico usado para armazenar e revisar memórias.");
        dictionaryMap.put("memória", "Uma lembrança ou experiência que pode ser armazenada na Penseira.");
        dictionaryMap.put("bruxo", "Um indivíduo que possui habilidades mágicas e pode acessar a Penseira.");
        dictionaryMap.put("feitiço", "Uma ação mágica conjurada por um bruxo, que pode afetar a Penseira ou o ambiente ao redor.");
        dictionaryMap.put("sorteio", "Um comando que gera uma frase enigmática para reflexão.");
        dictionaryMap.put("lumos", "feitiço que conjura luz na ponta da varinha, permitindo ao bruxo iluminar o ambiente ao seu redor.");
    }

    private enum Command {
        START("/start"),
        STATUS("/status"),
        TEMPO("/tempo"),
        REESCREVER("/reescrever"),
        SORTEIO("/sorteio"),;

        private final String commandValue;

        Command(String commandValue) {
            this.commandValue = commandValue;
        }
    }
}