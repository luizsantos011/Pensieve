    package me.luiz.penseira.bot;

    import io.github.cdimascio.dotenv.Dotenv;
    import org.telegram.telegrambots.bots.TelegramLongPollingBot;
    import org.telegram.telegrambots.meta.api.objects.Update;

    import java.util.*;
    import java.util.stream.Collectors;

    public class TelegramBot extends TelegramLongPollingBot {
        private final Dotenv dotenv = Dotenv.load();
        private final long MEU_ID = Long.parseLong(dotenv.get("BOT_USER_ID"));
        private final Map<Long, Long> ultimaMensagemPorUsuario = new HashMap<>();
        private static final long intervaloMinimo = 5000;

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
                List<Long> whitelist = carregarWhitelist();
                if(!whitelist.contains(userId)){
                    System.out.println("Apenas bruxos autorizados podem acessar esta penseira.");
                    System.out.println(userId);
                    return;
                }
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
                if (message.hasText()) {
                    String mensagem = message.getText();
                    System.out.println();
                }else if (message.hasDocument()) {
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
    }
