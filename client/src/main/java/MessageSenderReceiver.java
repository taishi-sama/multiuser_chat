import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageSenderReceiver implements Runnable{
    public AtomicBoolean stop = new AtomicBoolean(false);
    public Socket socket;
    public BufferedReader br;
    public PrintWriter pw;
    public Gson gson;
    public ConcurrentLinkedQueue<IMessage> msg = new ConcurrentLinkedQueue<>();

    /**
     * Создаёт подключение к серверу и соответствующие классы Reader и Writer
     * @param address сетевой адрес подключения
     * @param port порт подключения
     * @throws IOException
     */
    public MessageSenderReceiver(String address, int port) throws IOException {
        this.socket = new Socket(address, port);
        socket.setKeepAlive(true);
        var gsonbuilder = new GsonBuilder();
        gson = gsonbuilder.create();
        pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    }

    /**
     * Отправляет содержимое отправляемого пользователем сообщения на сервер.
     * @param messageContent Текстовое сообщение пользователя.
     * @throws IOException
     */
    public void SendMessage(String messageContent) throws IOException {
        var msg = new UserMessagesMessage(messageContent, null);
        var s = gson.toJson(msg.intoContainer());
        //System.out.println(s);
        pw.println(s);
        pw.flush();
    }

    /**
     * Отправляет сообщение о входе/регистрации на сервер.
     * @param newNickname Имя пользователя, под которым происходит вход.
     * @throws IOException
     */
    public void SendLogin(String newNickname) throws IOException {
        var msg = new WhoImIMessage(newNickname);
        var s = gson.toJson(msg.intoContainer());
        //System.out.println(s);
        pw.println(s);
        pw.flush();
    }

    /**
     * Получает сообщения с сервера и помещает их в очередь на обработку.
     */
    @Override
    public void run() {
        var g = new Gson();
        try {
            while (!stop.get() && !socket.isClosed()) {
                String line;
                while ((line = br.readLine()) != null && !socket.isClosed()) {
                    System.out.println(line);
                    var m = g.fromJson(line, MessageContainer.class);
                    var msg = m.intoMessage();
                    this.msg.add(msg);
                    //System.out.printf("Message received: %s\n", m);
                }
                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        catch (IOException io) {

        }
        finally {

        }
    }
}
