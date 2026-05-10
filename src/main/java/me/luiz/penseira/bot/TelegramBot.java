    package me.luiz.penseira.bot;

    import io.github.cdimascio.dotenv.Dotenv;
    import me.luiz.penseira.contracts.IComando;
    import me.luiz.penseira.contracts.ILogger;
    import org.telegram.telegrambots.bots.TelegramLongPollingBot;
    import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
    import org.telegram.telegrambots.meta.api.objects.Update;
    import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

    import java.io.File;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.time.LocalDateTime;
    import java.time.format.DateTimeFormatter;
    import java.util.*;
    import java.util.stream.Collectors;

    public class TelegramBot extends TelegramLongPollingBot {
        private final ILogger logger;
        private final Dotenv dotenv = Dotenv.load();
        private final List<Long> whitelist = carregarWhitelist();
        private final Map<Long, Long> ultimaMensagemPorUsuario = new HashMap<>();
        private final Map<String, IComando> comandosMap = new HashMap<>();
        private static final long intervaloMinimo = 5000;
        private int contadorMensagens = 0;

        public TelegramBot(ILogger logger) {
            inicializarComandos();
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
            if (update.hasMessage()) {
                contadorMensagens++;
                logger.registrarLog("mensagem recebida.");
                var message = update.getMessage();
                long userId = message.getFrom().getId();
                SendMessage msg = new SendMessage();
                //verificação de usuários autorizados
                if(!whitelist.contains(userId)){
                    System.out.println("Apenas bruxos autorizados podem acessar esta penseira.");
                    System.out.println(userId);
                    return;
                }
                //verificação de frequência de mensagens
                long agora = System.currentTimeMillis();
                if(ultimaMensagemPorUsuario.containsKey(userId)){
                    long tempoUltimaMensagem = ultimaMensagemPorUsuario.get(userId);
                    long diferenca = agora - tempoUltimaMensagem;
                    if(diferenca < intervaloMinimo){
                        System.out.println("Bruxo " + userId + " está conjurando feitiços rápidos demais! ignorando...");
                        return;
                    }
                }
                ultimaMensagemPorUsuario.put(userId, agora);
                System.out.println("Bruxo " + userId + " acessou a penseira.");
                System.out.println("Mensagem recebida de " + userId);
                //verificação do tipo da mensagem
                if (message.hasText()) {
                    contadorMensagens++;
                    String mensagem = message.getText();
                    String usuarioNome = message.getFrom().getFirstName();
                    msg.setChatId(message.getChatId().toString());
                    IComando comando = comandosMap.get(mensagem);
                    if(mensagem.length() > 500){
                        System.out.println("Bruxo " + usuarioNome + " tentou conjurar um feitiço muito complexo! A Penseira não pode processar mensagens tão longas.");
                        return;
                    }
                    if(comando != null){
                        comando.executar(msg, update);
                    } else{
                        System.out.println("A Penseira recebeu uma instrução que não pôde ser reconhecida.");
                    }
                }else if (message.hasDocument()) {
                    contadorMensagens++;
                    var doc = message.getDocument();
                    String nomeArquivo = doc.getFileName().toLowerCase();
                    List<String> extensoesPermitidas = Arrays.asList(".txt", ".jpg", ".pdf", "png");
                    boolean ehSeguro = extensoesPermitidas.stream().anyMatch(nomeArquivo::endsWith);
                    if(ehSeguro){
                        System.out.println("Memória recebida. A penseira a armazenou com sucesso.");
                    } else{
                        System.out.println("Não foi possível armazenar esta memória.");
                    }
                }
            }
        }

        private List<Long> carregarWhitelist() {
            String todosIDS = dotenv.get("BOT_USER_ID");
            return Arrays.stream(todosIDS.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }
        private void inicializarComandos() {
            comandosMap.put("/start", (msg, update) -> {
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
            comandosMap.put("/status", (msg, update) -> {
                msg.setText("A Penseira recebeu " + contadorMensagens + " mensagens desde que foi ativada.");
                try {
                    execute(msg);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            });
            comandosMap.put("/tempo", (msg, update) -> {
                msg.setText("As águas da Penseira se movem com o fluxo do tempo… e agora indicam: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                try {
                    execute(msg);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            });
            comandosMap.put("/reescrever", (msg, update) -> {
                String mensagemCompleta = update.getMessage().getText().replace("/reescrever", "").trim();
                String mensagemRevertida = mensagemCompleta.isEmpty() ? "A Penseira espera uma mensagem para reescrever." : new StringBuilder(mensagemCompleta).reverse().toString();
                msg.setText(mensagemRevertida);
                try {
                    execute(msg);
                } catch (TelegramApiException e) {}
            });
            comandosMap.put("/sorteio", (msg, update) -> {
                String frases = "O que parece pequeno hoje ecoará em caminhos inesperados;\n" +
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
                String[] frasesLista = frases.replace("\n", "").split(";");
                String fraseSorteada = frasesLista[new Random().nextInt(frasesLista.length)];
                msg.setText(fraseSorteada);
                try {
                    execute(msg);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        private enum Comando {
            START("/start"),
            STATUS("/status"),
            TEMPO("/tempo"),
            REESCREVER("/reescrever"),
            SORTEIO("/sorteio"),;

            private final String comando;

            Comando(String comando) {
                this.comando = comando;
            }
        }
    }
