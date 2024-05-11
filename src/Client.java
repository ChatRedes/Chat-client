import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket client_socket;

    private BufferedReader buffered_reader;
    private BufferedWriter buffered_writer;

    private String username;

    public Client(Socket client_socket, String username)
    {
        try {
            this.client_socket = client_socket;
            this.buffered_writer = new BufferedWriter(new OutputStreamWriter(client_socket.getOutputStream()));
            this.buffered_reader = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
            this.username = username;
        } catch (IOException e) {
            System.err.println("Error: Failed to start the client: " + e.getMessage());
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
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    public void receiveMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String messageFromServer;
                try {
                    while ((messageFromServer = buffered_reader.readLine()) != null) {
                        System.out.println(messageFromServer);
                    }
                } catch (IOException e) {
                    System.err.println("Error receiving message: " + e.getMessage());
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter username: ");
        String username = scanner.nextLine();

        try {
            Socket clientSocket = new Socket("localhost", 8080); // Connecting to Server's port
            Client client = new Client(clientSocket, username);
            client.receiveMessage();
            client.sendMessage();
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }
}
