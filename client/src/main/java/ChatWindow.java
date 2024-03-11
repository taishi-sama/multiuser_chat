import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

public class ChatWindow extends BasicWindow {
    WindowBasedTextGUI gui;
    Label listOfUsers;
    TextBox addressBox;
    TextBox portBox;
    Button connectButton;
    Button sendMessageButton;
    TextBox nicknameBox;
    Button setNicknameButton;
    TextBox messageBox;
    Label chatBoxLabel;
    MessageSenderReceiver messageSenderReceiver;
    Thread messageThread;
    ArrayList<String> messageHistory = new ArrayList<>();
    public void UpdateTick() {
        if (messageSenderReceiver != null) {
            IMessage imessage;
            while ((imessage = messageSenderReceiver.msg.poll()) != null) {
                switch (imessage) {
                    case UserMessagesMessage msg:
                        AddMessageToChatbox(msg.content, msg.username, LocalTime.now());
                        break;
                    case UserlistMessage userlistMessage:
                        updateActiveUsers(userlistMessage);
                        break;
                    default:
                        AddLineToChatbox(imessage.toString());

                }
            }
        }
    }
    public void updateActiveUsers(UserlistMessage userlistMessage) {
        var users = new StringBuilder();
        for (var u: userlistMessage.current_users) {
            users.append(u);
            users.append("\n");
        }
        listOfUsers.setText(users.toString());
    }
    public ChatWindow(WindowBasedTextGUI gui) {
        super("Chat");

        this.gui = gui;
        this.setHints(Arrays.asList(Window.Hint.FULL_SCREEN));

        Panel globalContentPane = new Panel(new LinearLayout(Direction.VERTICAL));

        var outerTopPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        globalContentPane.addComponent(outerTopPanel);

        var ipPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        addressBox = new TextBox(new TerminalSize(40, 1));
        portBox = new TextBox(new TerminalSize(6, 1));

        connectButton = new Button("Connect");
        ipPanel.addComponent(addressBox);
        ipPanel.addComponent(new Label(":"));
        ipPanel.addComponent(portBox);
        ipPanel.addComponent(connectButton);
        outerTopPanel.addComponent(ipPanel.withBorder(Borders.singleLine("Connect to:")));

        var nicknamePanel = new Panel((new LinearLayout(Direction.HORIZONTAL)));
        nicknameBox = new TextBox(new TerminalSize(20, 1));
        setNicknameButton = new Button("Register/Login");
        nicknamePanel.addComponent(nicknameBox);
        nicknamePanel.addComponent(setNicknameButton);
        outerTopPanel.addComponent(nicknamePanel.withBorder(Borders.singleLine("Register/Login:")));
        Panel contentPane =  new Panel(new LinearLayout(Direction.HORIZONTAL));
        globalContentPane.addComponent(contentPane);
        chatBoxLabel = new Label("");

        listOfUsers = new Label("");

        contentPane.addComponent(chatBoxLabel.withBorder(Borders.singleLine("Chat")));
        contentPane.addComponent(listOfUsers.withBorder(Borders.singleLine("List of users")));
        messageBox = new TextBox(new TerminalSize(64, 1));
        messageBox.setHorizontalFocusSwitching(true);
        //textbox.setEnabled(false);
        sendMessageButton = new Button("Send");
        globalContentPane.addComponent(messageBox);
        globalContentPane.addComponent(sendMessageButton);
        setComponent(globalContentPane);
        var gui2 = this.getTextGUI();
        var w = this;
        connectButton.addListener(new Button.Listener() {
            @Override
            public void onTriggered(Button button) {
                w.HandleConnect();
            }
        });
        sendMessageButton.addListener(new Button.Listener() {
            @Override
            public void onTriggered(Button button) {
                w.HandleMessageSend();
            }
        });
        setNicknameButton.addListener(new Button.Listener() {
            @Override
            public void onTriggered(Button button) {
                w.HandleLogin();
            }
        });
    }
    public void AddLineToChatbox(String line) {
        messageHistory.add(line);
        var diff = chatBoxLabel.getPreferredSize().getRows() - chatBoxLabel.getSize().getRows();
        for (var i = 0; i < diff; i++) {
            messageHistory.removeFirst();
        }
        //System.out.println("Before: " + chatBoxLabel.getPreferredSize());
        chatBoxLabel.setText(messageHistory.stream().reduce((x, y) -> {return x + "\n" + y;}).orElse("") + "\n.");
        //System.out.println("After: " + chatBoxLabel.getSize());
    }
    public void AddMessageToChatbox(String line, String nickname, LocalTime time) {
        AddLineToChatbox(String.format("%s(%s): %s", nickname, time.getHour() + ":" + time.getMinute() + ":" + time.getSecond(), line));
    }

    public void HandleConnect() {
        if (this.messageSenderReceiver == null) {
            try {
                this.messageSenderReceiver = new MessageSenderReceiver(addressBox.getText(), Integer.parseInt(portBox.getText()), nicknameBox.getTextOrDefault("None of text"));
                AddLineToChatbox("Successfully connected!");
                messageThread = new Thread(this.messageSenderReceiver);
                messageThread.start();

            } catch (IOException e) {
                AddLineToChatbox("Unable to connect! Error: " + e);
            }
        }
        else {
            AddLineToChatbox("Already connected!");
        }
    }
    public void HandleLogin() {
        if (this.messageSenderReceiver != null) {
            try {
                var message = nicknameBox.getText();
                if (message.isBlank()) {
                    AddLineToChatbox("Empty nickname!");
                    return;
                }
                AddLineToChatbox("Login message sent...");
                messageSenderReceiver.SendLogin(message);
            } catch (IOException e) {
                AddLineToChatbox("Unable to send message! Error: " + e);
            }
        }
        else {
            AddLineToChatbox("Not connected...");
        }
    }
    public void HandleMessageSend() {
        if (this.messageSenderReceiver != null) {
            try {
                var message = messageBox.getText();
                AddMessageToChatbox(message, "You", LocalTime.now());
                messageSenderReceiver.SendMessage(message);
            } catch (IOException e) {
                AddLineToChatbox("Unable to send message! Error: " + e);
            }
        }
        else {
            AddLineToChatbox("Not connected...");
        }
    }
    @Override
    public void close() {
        super.close();
        if (messageSenderReceiver != null) {
            messageSenderReceiver.stop.set(true);

        }
    }
}
