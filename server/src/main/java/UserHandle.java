import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Представляет собой контекст конкретного пользователя, храня в себе сокет соединения с пользователем.
 */
public class UserHandle implements Runnable {
    public boolean isRegistered = false;
    public Integer id = null;
    public String username;
    public Socket userSocket;
    public ConcurrentLinkedQueue<IServerUserMessage> messages;
    public PrintWriter pw;
    public BufferedReader br;
    public UserHandle(Socket socket, ConcurrentLinkedQueue<IServerUserMessage> messages) throws IOException {
        this.messages = messages;
        username = socket.getInetAddress().toString() + ":" + socket.getPort();
        userSocket = socket;
        pw = new PrintWriter(socket.getOutputStream(), true);
        br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

        
    }

    /**
     * Входная точка клиентского потока. В цикле читает сокет пользователя, прочитанные сообщения отправляет в очередь сообщений. При разрыве сообщения выходит из цикла и отправляет сообщение серверному потоку об отключении клиента.
     */
    @Override
    public void run() {
        var g = new Gson();
        messages.offer(new NewUserJoinsMessage(this));
        System.out.println("Trying read message from client:");
        try {
            while (!userSocket.isClosed()) {
                String line;
                while ((line = br.readLine()) != null && !userSocket.isClosed()) {
                    System.out.println(line);
                    var m = g.fromJson(line, MessageContainer.class);
                    var msg = m.intoMessage();
                    switch (msg) {
                        case WhoImIMessage whoImIMessage:
                            messages.offer(new UserRegisteredMessage(this, whoImIMessage.getUsername()));
                            break;
                        case UserMessagesMessage userMessagesMessage:
                            messages.offer(new UserSentMessage(this, userMessagesMessage.content));
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + msg);
                    }
                    System.out.println(m);
                }
                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            //throw new RuntimeException(e);
            System.out.printf("Funny exception!!: %s\n", e);
        } finally {
            messages.offer(new UserLeftMessage(this));
        }
    }
}
