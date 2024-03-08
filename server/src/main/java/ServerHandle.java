import com.google.gson.Gson;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerHandle implements Runnable {
    Gson g = new Gson();
    DB db;
    List<UserHandle> user_handles = new ArrayList<>();
    public ConcurrentLinkedQueue<IServerUserMessage> message_queue;

    public ServerHandle(ConcurrentLinkedQueue<IServerUserMessage> message_queue, DB db) {
        this.message_queue = message_queue;
        this.db = db;
    }


    @Override
    public void run() {
        while (true) {
            IServerUserMessage isum;
            while ((isum = message_queue.poll()) != null) {
                processMessage(isum);
            }
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void processMessage(IServerUserMessage msg) {
        switch (msg) {
            case NewUserJoinsMessage newUserJoinsMessage:
                System.out.println("User joined: " + newUserJoinsMessage.userhandle.userSocket.getInetAddress() + ":" + newUserJoinsMessage.userhandle.userSocket.getPort());
                user_handles.add(newUserJoinsMessage.userhandle);
                System.out.println("Users in list: " + user_handles.size());

                break;
            case UserRegistered userRegistered:
                if (!userRegistered.userHandle.isRegistered) {
                    try {

                        userRegistered.userHandle.id = db.getIdOfUser(userRegistered.newName);
                        if (userRegistered.userHandle.id == null) {
                            userRegistered.userHandle.id = db.insertNewUser(userRegistered.newName, LocalDateTime.now(), true);
                            sendMessageToUser(new UserMessagesMessage("Welcome as new user!", "Server"), userRegistered.userHandle);
                        }
                        else {
                            var time = db.getUserLastJoinTime(userRegistered.userHandle.id);
                            var message = String.format("Welcome back! Last visit was %s", time.getYear() + "." + time.getMonthValue() + "." + time.getDayOfMonth() + " " + time.getHour() + ":" + time.getMinute() + ":" + time.getSecond());
                                    //time.getHour() + ":" + time.getMinute() + ":" + time.getSecond()
                            sendMessageToUser(
                                    new UserMessagesMessage(message, "Server"),
                                    userRegistered.userHandle);

                            db.updateLastJoinAndOnlineStatus(userRegistered.userHandle.id, true, LocalDateTime.now());
                        }
                        System.out.printf("User \"%s\" registered as \"%s\".\n", userRegistered.userHandle.username, userRegistered.newName);
                        userRegistered.userHandle.username = userRegistered.newName;
                    } catch (SQLException e) {
                        System.out.println("SQL Error: " + e);
                    }

                    userRegistered.userHandle.isRegistered = true;

                    sendMessageToEveryone(generateListOfUsers(), null);
                }
                break;

            case UserSentMessage userSentMessage:
                if (userSentMessage.userHandle.isRegistered) {
                    var usermsg = new UserMessagesMessage(userSentMessage.content, userSentMessage.userHandle.username);
                    try {
                        db.insertNewMessage(userSentMessage.content, userSentMessage.userHandle.id, LocalDateTime.now());
                    } catch (SQLException e) {
                        System.out.println("SQL Error: " + e);
                    }
                    sendMessageToEveryone(usermsg, userSentMessage.userHandle);
                }
                else {
                    sendMessageToUser(new UserMessagesMessage("Register first before sending messages!", "Server"), userSentMessage.userHandle);
                }
                break;
            case UserLeftMessage userLeftMessage:
                System.out.println("User removed: " + userLeftMessage.userHandle.userSocket.getInetAddress() + ":" + userLeftMessage.userHandle.userSocket.getPort());
                user_handles.remove(userLeftMessage.userHandle);
                System.out.println("Users in list: " + user_handles.size());
                if (userLeftMessage.userHandle.isRegistered) {
                    try {
                        db.updateUserStatus(userLeftMessage.userHandle.id, false);
                    } catch (SQLException e) {
                        System.out.println("SQL Error: " + e);
                    }
                }
                sendMessageToEveryone(generateListOfUsers(), null);

                break;
            default:
                System.out.println("Unexpected value: " + msg);
                //throw new IllegalStateException("Unexpected value: " + msg);
        }
    }
    public UserlistMessage generateListOfUsers() {
        var list = new UserlistMessage();
        var arraylist = new ArrayList<String>();


        try {
            list.current_users = db.getOnlineUsers();
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e);
        }
        return list;
    }
    public void sendMessageToUser(IMessage msg, UserHandle user) {
        var container = msg.intoContainer();
        var json = g.toJson(container);
        user.pw.println(json);
        if (user.pw.checkError()) {
            System.out.println("Error was encountered");
        }
    }
    //null for ignore to sent message to everybody
    public void sendMessageToEveryone(IMessage msg, UserHandle ignore )
    {

        var container = msg.intoContainer();
        var json = g.toJson(container);

        for (var userHandle : user_handles) {
            if (userHandle == ignore) continue;
            try {

                userHandle.pw.println(json);
                if (userHandle.pw.checkError()) {
                    System.out.println("Error was encountered");
                }
            }
            catch (Throwable e) {
                System.out.println(e);
            }


        }
    }
}
