public class UserMessagesMessage implements IMessage {
    public UserMessagesMessage(String content, String username) {
        this.content = content;
        this.username = username;
    }
    public String content;
    public String username;

    @Override
    public MessageContainer intoContainer() {
        var messageContainer = new MessageContainer();
        messageContainer.messagesMessage = this;
        return messageContainer;
    }
}
