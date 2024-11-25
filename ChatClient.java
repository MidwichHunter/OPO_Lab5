import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    private JTextArea chatArea;
    private JTextField inputField;
    private PrintWriter out;

    public ChatClient() {
        JFrame frame = new JFrame("Клиент чата");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        inputField = new JTextField();
        inputField.addActionListener(this::sendMessage);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputField, BorderLayout.SOUTH);
        frame.setVisible(true);

        connectToServer();
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Ввод имени пользователя
            String name = JOptionPane.showInputDialog(null, "Введите ваше имя:", "Имя пользователя", JOptionPane.QUESTION_MESSAGE);
            if (name == null || name.trim().isEmpty()) {
                name = "Безымянный";
            }
            out.println(name);

            // Чтение сообщений от сервера в отдельном потоке
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        chatArea.append(message + "\n");
                    }
                } catch (IOException e) {
                    System.err.println("Соединение с сервером потеряно: " + e.getMessage());
                }
            }).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Не удалось подключиться к серверу", "Ошибка", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void sendMessage(ActionEvent event) {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            out.println(message);
            inputField.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClient::new);
    }
}