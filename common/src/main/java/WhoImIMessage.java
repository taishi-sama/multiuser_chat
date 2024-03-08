public class WhoImIMessage implements IMessage{
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public WhoImIMessage(String username) {
        this.username = username;
    }

    protected String username;

    @Override
    public MessageContainer intoContainer() {
        var messageContainer = new MessageContainer();
        messageContainer.whoImIMessage = this;
        return messageContainer;
    }
}
