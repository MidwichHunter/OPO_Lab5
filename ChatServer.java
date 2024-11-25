import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        System.out.println("Сервер запущен...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        }
    }

    public static void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }

    public static void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }
}

class ClientHandler implements Runnable {
    private final Socket socket;
    private PrintWriter out;
    private String clientName;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        ) {
            out = new PrintWriter(socket.getOutputStream(), true);

            // Читаем имя клиента
            out.println("Введите ваше имя:");
            clientName = reader.readLine();
            ChatServer.broadcast(clientName + " вошел в чат!");

            String message;
            while ((message = reader.readLine()) != null) {
                ChatServer.broadcast(clientName + ": " + message);
            }
        } catch (IOException e) {
            System.err.println("Ошибка клиента: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Не удалось закрыть соединение: " + e.getMessage());
            }
            ChatServer.removeClient(this);
            ChatServer.broadcast(clientName + " покинул чат.");
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}
