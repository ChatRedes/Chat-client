import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    private Socket client_socket;
    private ArrayList<Chat> chats;
    
    private BufferedReader buffered_reader;
    private BufferedWriter buffered_writer;
    
    private String username;

    public static Scanner scanner;

    public Client(Socket client_socket)
    {
        try {
            this.client_socket = client_socket;
            this.buffered_writer = new BufferedWriter(new OutputStreamWriter(client_socket.getOutputStream()));
            this.buffered_reader = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
            register_client();
        } catch (IOException e) {
            System.err.println("Error: Failed to start the client: " + e.getMessage());
            e.printStackTrace();
            close_client();
        }
    }

    private void register_client() {
        System.out.println("Enter username: ");
        username = scanner.nextLine();

        String messageToSend = "REGISTRO " + username;
        try {
            buffered_writer.write(messageToSend);
            buffered_writer.newLine();
            buffered_writer.flush();
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
            close_client();
        }

}

    private void sendMessage() {
        try {
            buffered_writer.write(username);
            buffered_writer.newLine();
            buffered_writer.flush();
            while (client_socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                buffered_writer.write(username + ": " + messageToSend);
                buffered_writer.newLine();
                buffered_writer.flush();
            }

        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
            close_client();
        }
    }

    private void receiveMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String serverMessage;
                try {
                    while ((serverMessage = buffered_reader.readLine()) != null) {
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
        String[] parameters = server_Message.split(" ", 2);
        int chat_index = find_chat_index(parameters[0]);
        if (chat_index == -1) {
            return;
        }
        String mensagem = parameters[1];
        chats.get(chat_index).addMessage(mensagem);
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
        }

        if (response[0].equals("REGISTRO_OK")) {
            System.out.println("Registration successful!");
        }
        
        if (response[0].equals("ERRO") && response[1].equals("USER_ALREDY_EXISTS")) {
            System.out.println("Usuario com este nome ja existe");
            register_client();
        }

        if (response[0].equals("ERRO")) {
            
        }
    }

    private void close_client() {
        try {
            System.out.println("Closing client...");
            buffered_reader.close();
            buffered_writer.close();
            client_socket.close();
            System.out.println("Client closed.");
            System.exit(0);
        } catch (IOException e) {
            System.err.println("Error closing client: " + e.getMessage());
        }
    }

    
    public static void main(String[] args) {

        try {
            Socket clientSocket = new Socket("localhost", 8080); // Connecting to Server's port
            Client client = new Client(clientSocket);
            client.receiveMessage();
            client.sendMessage();
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }
}
