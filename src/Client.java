import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    private Socket clientSocket;
    private ArrayList<Chat> chats;
    
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    
    private String username;
    private Boolean logedIn = false;

    public Scanner scanner;
    
    public static void main(String[] args) {
        Client client = new Client();
        client.searchServer();
        client.setBuffers();

        while (client.logedIn == false) {
            client.register_client();
        }

        client.receiveMessage();
        client.gui();
    }

    public Client()
    {
        this.scanner = new Scanner(System.in);
    }

    private void register_client() {
        System.out.println("Enter username: ");
        username = scanner.nextLine();

        String request = "REGISTRO " + username;
        try {
            sendRequest(request);

            String serverResponse = bufferedReader.readLine();
            String[] response = serverResponse.split(" ", 2);

            if (response[0].equals("REGISTRO_OK")) {
                logedIn = true;
                System.out.println("Usuário registrado com sucesso");
                return;
            }

            if (response[0].equals("ERRO")) {
                System.out.println("Erro: " + response[1]);
                return;
            }

        } catch (IOException e) {
            System.err.println("Erro: " + e.getMessage());
            close_client();
        }

    }

    private void receiveMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String serverMessage;
                try {
                    while ((serverMessage = bufferedReader.readLine()) != null) {
                        System.out.println(serverMessage);
                        parse_response(serverMessage);
                    }
                } catch (IOException e) {
                    System.err.println("Error receiving message: " + e.getMessage());
                    close_client();
                }
            }
        }).start();
    }

    private void add_chat_message(String server_Message) {
        String[] parameters = server_Message.split(" ", 3);
        int chat_index = find_chat_index(parameters[0]);
        if (chat_index == -1) {
            return;
        }
        String usuario = parameters[1];
        String mensagem = parameters[2];
        Chat chat = chats.get(chat_index);
        chat.addMessage(usuario, mensagem);
    }

    private int find_chat_index(String chat_name) {
        for (Chat chat : chats) {
            if (chat.getNome().equals(chat_name)) {
                return chats.indexOf(chat);
            }
        }
        return -1;
    }

    private void parse_response(String serverMessage) {
        String[] response = serverMessage.split(" ", 2);
        if (response[0].equals("MENSAGEM")) {
            add_chat_message(response[1]);
            return;
        }

        if (response[0].equals("REGISTRO_OK")) {
            System.out.println("Registration successful!");
            logedIn = true;
            return;
        }
        
        if (response[0].equals("ERRO")) {
            System.out.println("Erro: " + response[1]);
            return;
        }

        return;
    }

    private void close_client() {
        try {
            System.out.println("Closing client...");
            bufferedWriter.close();
            bufferedReader.close();
            clientSocket.close();
            System.out.println("Client closed.");
            System.exit(0);
        } catch (IOException e) {
            System.err.println("Error closing client: " + e.getMessage());
        }
    }

    private Boolean searchServer() {
        System.out.println("Enter the host to connect: ");
        String host = scanner.nextLine();

        System.out.println("Enter the port to connect: ");
        String port = scanner.nextLine();

        try {
            clientSocket = new Socket(host, Integer.parseInt(port));
            return true;
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
        return false;
    }

    private void setBuffers() {
        try {
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (Exception e) {
            System.err.println("Error: Failed to start the client: " + e.getMessage());
            e.printStackTrace();
            close_client();
        }
    }

    private void gui() {
        while (true) {
            System.out.printf("\n\n%s\n", username);
            System.out.println("Enter option: ");
            System.out.println("Option 1 - Search Chats");
            System.out.println("Option 2 - Enter Chat");
            System.out.println("Option 3 - Sent message");
            System.out.println("Option 4 - Create Chat");
            System.out.println("Option 5 - List Your Chats");
            System.out.println("Option 6 - Quit Chat");
            System.out.println("Option 7 - Exit");

            String option = scanner.nextLine();
            optionHandler(option);
        }
    }

    private void optionHandler(String Option) {
        switch (Option) {
            case "1":
                searchChats();
                break;
            
            case "2":
                enterChat();
                break;

            case "3":
                sendMessage();
                break;

            case "4":
                createChat();
                break;

            case "5":
                listYourChats();
                break;

            case "6":
                quitChat();
                break;

            case "7":
                close_client();
                break;
        
            default:
                break;
        }
    }

    private void searchChats() {
        System.out.println("Searching chats...");
        String request = "LISTAR_SALAS";
        sendRequest(request);
    }

    private void enterChat() {
        String request = "ENTRAR_SALA ";

        System.out.println("Enter chat name: ");
        String chatName = scanner.nextLine();

        request += chatName;

        System.out.println("Want to enter a password? (Y/N)");
        String senha = scanner.nextLine();

        if (senha.toLowerCase().equals("y")) {
            System.out.println("Enter password: ");
            senha = scanner.nextLine();
            request += " " + createHash(senha);
        }

        sendRequest(request);
    }
    
    private void sendMessage() {
        System.out.println("Enter chat: ");
        String chat = scanner.nextLine();
        System.out.println("Enter message: ");
        String messageToSend = scanner.nextLine();

        String request = "ENVIAR_MENSAGEM " + chat + " " + messageToSend;
        sendRequest(request);
    }

    private void createChat() {
        System.out.println("Enter chat name: ");
        String chatName = scanner.next();
        scanner.nextLine();

        String chatType = "";
        while (!chatType.equals("public") && !chatType.equals("private")) {
            System.out.println("The chat will be public or private?");
            chatType = scanner.nextLine();
            chatType = chatType.toLowerCase();
            System.out.println("Chat_type:" + chatType);
        }

        String request = null;
        if (chatType.equals("public")) {
            request = createPublicChat(chatName);
            sendRequest(request);
            return;
        }

        if (chatType.equals("private")) {
            request = createPrivateChat(chatName);
            sendRequest(request);
            return;
        }

    }

    private String createPublicChat(String chatName) {
        String request;

        request = "CRIAR_SALA PUBLICA " + chatName;
        sendRequest(request);
        return request;
    }

    private String createPrivateChat(String chatName) {
        String request;
        request = "CRIAR_SALA PRIVADA " + chatName;

        String senha = "";

        while (senha == "" || senha.length() < 3) {
            System.out.println("Enter password: (Minimum size of 3 characters)");
            senha = scanner.nextLine();
        }

        request += " " + createHash(senha);
        return request;
    }

    private String createHash(String senha) {

        return senha;
    }

    private void listYourChats() {
        if (chats.size() == 0) {
            System.out.println("You don't have any chats yet");
            return;
        }

        int index = 1;
        for (Chat chat : chats) {
            System.out.printf("[%d] %s\n", index, chat.getNome());
            index++;
        }
        System.out.println("[0] Cancel");

        System.out.println("Enter chat index: ");
        int chatIndex = scanner.nextInt();
        scanner.nextLine();

        if (chatIndex == 0) {
            return;
        }

        chatIndex -= 1; // Subtrair 1 pois o index começa em 1
        if (chatIndex < 0 || chatIndex >= chats.size()) {
            System.out.println("Invalid chat index");
            return;
        }

        listChatMessages(chatIndex);
        return;
    }

    private void listChatMessages(int index) {
        Chat chat = chats.get(index); // Obter o chat selecionado.
        chat.printMessages();
    }

    private void quitChat() {
        String request = "SAIR_SALA ";

        System.out.println("Enter chat: ");
        String chatName = scanner.nextLine();
    
        request += chatName;

        sendRequest(request);
    }

    private void sendRequest(String request) {
        try {
            bufferedWriter.write(request);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            System.err.println("Error sending request: " + e.getMessage());
            close_client();
        }
    }
}
