import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerMain {
    public static void main(String[] args) throws IOException, SQLException {
        var db = new DB("./appdb");
        db.EnsureUserlogTableCreation();
        db.EnsureRegisteredUsersTableCreation();
        var port = 10801;
        var ip = InetAddress.getByAddress(new byte[]{0, 0, 0, 0});
        try (var serverSocket = new ServerSocket(port, 0, ip)) {
            System.out.println("Listening to " + ip + ":" + port);
            var server_queue = new ConcurrentLinkedQueue<IServerUserMessage>();
            var server_handle = new ServerHandle(server_queue,db);
            var server_thread = new Thread(server_handle);
            server_thread.start();
            System.out.printf("Thread %s started\n", server_thread.getName());
            while (true) {
                var socket = serverSocket.accept();
                socket.setKeepAlive(true);
                var h = new UserHandle(socket, server_queue);
                var userThread = new Thread(h);
                userThread.start();
                System.out.printf("Thread %s started\n", userThread.getName());
            }
        }

    }
}
