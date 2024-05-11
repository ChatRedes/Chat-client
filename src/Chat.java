import java.util.ArrayList;

public class Chat {
    private String nome;
    private ArrayList<String> mensagens;
    
    public Chat (String nome) {
        this.nome = nome;
    }

    public void addMessage(String mensagem) {
        mensagens.add(mensagem);
    }

    public void printMessages() {
        for (String mensagem : mensagens) {
            System.out.println(mensagem);
        }
    }

    public String getNome() {
        return nome;
    }
}
