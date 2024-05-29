import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Scanner;
import Utils.*;

public class Client {
    private Socket clientSocket;
    private Clientcripto cripto;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    private String username;
    private Boolean logedIn = false;
    private Boolean waitingResponse = false;

    public Scanner scanner;

    public static void main(String[] args) {
        Client client = new Client();
        client.searchServer();
        client.setBuffers();

        while (client.logedIn == false) {
            client.register_client();
        }

        client.authenticateClient();

        client.receiveMessage();
        client.gui();
    }

    public Client() {
        this.scanner = new Scanner(System.in);
        this.cripto = new Clientcripto();
    }

    private void register_client() {
        System.out.println("Enter username: ");
        username = scanner.nextLine();

        String request = "REGISTRO " + username;
        try {
            unsafeRequest(request);

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

    private void authenticateClient() {
        unsafeRequest("AUTENTICACAO " + username);
        try {
            String serverResponse = bufferedReader.readLine();
            String[] parsedResponse = serverResponse.split(" ");

            if (parsedResponse.length != 2) {
                System.err.println("Invalid response from server: " + serverResponse);
                close_client();
            }

            if (!parsedResponse[0].equals("CHAVE_PUBLICA")) {
                System.err.println("Authentication failed");
                close_client();
            }

            String publicKey = parsedResponse[1];
            String request = cripto.setSimetricKey(publicKey);
            unsafeRequest(request);

        } catch (Exception e) {
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
                        parse_response(serverMessage);
                    }
                } catch (IOException e) {
                    System.err.println("Error receiving message: " + e.getMessage());
                    close_client();
                }
            }
        }).start();
    }

    private void parse_response(String serverMessage) {
        String message;
        try {
            message = cripto.decryptMessageFromClient(serverMessage);
        } catch (Exception e) {
            message = "ERRO Não foi possível decifrar a mensagem";
        }

        String[] response = message.split(" ", 2);
        if (response[0].equals("MENSAGEM")) {
            String[] receivedMessage = response[1].split(" ", 3);
            String chatName = receivedMessage[0];
            String username = receivedMessage[1];
            String chatMessage = receivedMessage[2];
            System.out.println(chatName + " - " + username + ": " + chatMessage);
            return;
        }
        
        if (response[0].equals("CRIAR_SALA_OK")) {
            System.out.println("Chat created successfully!");
            waitingResponse = false;
            return;
        }

        if (response[0].equals("SALAS")) {
            String[] salas = response[1].split(" ");
            for (String sala : salas) {
                System.out.println(sala);
            }
            waitingResponse = false;
            return;
        }
        
        if (response[0].equals("ENTRAR_SALA_OK")) {
            System.out.println("Chat joined successfully!");
            waitingResponse = false;
            return;
        }
        
        if (response[0].equals("ENTROU")) {
            String[] parsedMessage = response[1].split(" ");
            System.out.println("The user " + parsedMessage[1] + " joined the chat " + parsedMessage[0]);
            waitingResponse = false;
            return;
        }

        if (response[0].equals("SAIR_SALA_OK")) {
            System.out.println("Chat left successfully!");
            waitingResponse = false;
            return;
        }

        if (response[0].equals("SAIU")) {
            String[] parsedMessage = response[1].split(" ");
            System.out.println("The user " + parsedMessage[1] + " left the chat " + parsedMessage[0]);
            return;
        }

        if (response[0].equals("FECHAR_SALA_OK")) {

            waitingResponse = false;
            return;
        }

        if (response[0].equals("SALA_FECHADA")) {

            waitingResponse = false;
            return;
        }

        if (response[0].equals("ERRO")) {
            System.out.println("Erro: " + response[1]);
            waitingResponse = false;
            return;
        }

        if (response[0].equals("BANIMENTO_OK")) {
            System.out.println("You've banned user successfully");
            return;
        }

        if (response[0].equals("BANIDO_DA_SALA")) {
            String[] parsedResponse = response[1].split(" ");
            System.out.println("You were banned from chat " + parsedResponse[0]);
            return;
        }

        System.out.println("Mensagem recebida não reconhecida: " + message);
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
            while (waitingResponse) {
                // do nothing
                try {
                    wait(1);
                } catch (Exception e) {
                }
            }
            System.out.printf("\n\n%s\n", username);
            System.out.println("Enter option: ");
            System.out.println("Option 1 - Search Chats");
            System.out.println("Option 2 - Enter Chat");
            System.out.println("Option 3 - Sent message");
            System.out.println("Option 4 - Create Chat");
            System.out.println("Option 5 - Quit Chat");
            System.out.println("Option 6 - Ban user");
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
                quitChat();
                break;

            case "6":
                banUser();
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
        waitingResponse = true;
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

        waitingResponse = true;
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
            waitingResponse = true;
            sendRequest(request);
            return;
        }

        if (chatType.equals("private")) {
            request = createPrivateChat(chatName);
            waitingResponse = true;
            sendRequest(request);
            return;
        }
    }

    private String createPublicChat(String chatName) {
        String request;

        request = "CRIAR_SALA PUBLICA " + chatName;
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

    public static String createHash(String senha) {
        try {
            MessageDigest algorithm = MessageDigest.getInstance("SHA-256");
            byte hash[] = algorithm.digest(senha.getBytes("UTF-8"));

            StringBuilder texto = new StringBuilder();
            for (byte b : hash) {
                texto.append(String.format("%02X", 0xFF & b));
            }
            return texto.toString();
        } catch (Exception e) {
            System.out.println("Error: Failed to create hash: " + e.getMessage());
            return null;
        }
    }

    private void quitChat() {
        String request = "SAIR_SALA ";

        System.out.println("Enter chat: ");
        String chatName = scanner.nextLine();

        request += chatName;

        waitingResponse = true;
        sendRequest(request);
    }
    
    private void banUser() {
        System.out.println("Enter chat: ");
        String chatName = scanner.nextLine();

        System.out.println("Enter user to be banned: ");
        String userName = scanner.nextLine();

        String request = "BANIR_USUARIO " + chatName + " " + userName;
        waitingResponse = true;
        sendRequest(request);
    }

    private void sendRequest(String request) {
        try {
            String encryptedRequest = cripto.encryptedMessage(request);
            bufferedWriter.write(encryptedRequest);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (Exception e) {
            System.err.println("Error sending request: " + e.getMessage());
            close_client();
        }
    }

    private void unsafeRequest(String request) {
        try {
            bufferedWriter.write(request);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (Exception e) {
            System.err.println("Error sending request: " + e.getMessage());
            close_client();
        }
    }
}
