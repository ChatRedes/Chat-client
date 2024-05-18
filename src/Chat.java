import java.util.ArrayList;

public class Chat {
    private String nome;
    private ArrayList<String> users;
    private ArrayList<String> mensagens;
    
    public Chat (String nome) {
        this.nome = nome;
    }

    public void addMessage(String user, String mensagem) {
        users.add(user);
        mensagens.add(mensagem);
    }

    public void printMessages() {
        for (String mensagem : mensagens) {
            System.out.printf("%s: %s\n", nome, mensagem);
        }
    }

    public String getNome() {
        return nome;
    }
}
