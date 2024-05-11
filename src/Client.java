import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket client_socket;

    private BufferedReader buffered_reader;
    private BufferedWriter buffered_writer;

    private String username;
    private String server_port;

    public Client(Socket client_socket, String username)
    {
        try {
            this.client_socket = client_socket;
            this.buffered_writer = new BufferedWriter(new OutputStreamWriter(client_socket.getOutputStream()));
            this.buffered_reader = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
            this.username = username;
            System.out.println("Error: Failed to start the server on port " + client_socket.getPort());

        } catch (IOException e) {
            System.err.println("Error: Failed to start the server on port " + client_socket.getPort());
            e.printStackTrace();
        }
    }

    public void sendMessage() {
        try {
            buffered_writer.write(username);
            buffered_writer.newLine();
            buffered_writer.flush();
            Scanner scanner = new Scanner(System.in);
            while (client_socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                buffered_writer.write(username + ": " + messageToSend);
                buffered_writer.newLine();
                buffered_writer.flush();
            }

        } catch (IOException e) {
            System.err.println("Error: Failed to start the server on port " + client_socket.getPort());
        }
    }

    public void listen_message() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String messageFromGroupChat;
                while (client_socket.isConnected()) {
                    try {
                        messageFromGroupChat = buffered_reader.readLine();
                        System.out.println(messageFromGroupChat);
                    } catch (IOException e) {
                        System.err.println("Error: Failed to listen server on port " + client_socket.getPort());
                    }
                }
            }
        }).start();
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter username: ");
        String username = scanner.nextLine();
        Socket clientSocket = new Socket("localhost", 1234);
        Client client = new Client(clientSocket, username);
        client.listen_message();
        client.sendMessage();

    }
}
