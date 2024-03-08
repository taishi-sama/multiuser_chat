public class MessageContainer {
    public UserlistMessage userlistMessage;
    public UserMessagesMessage messagesMessage;
    public WhoImIMessage whoImIMessage;
    public IMessage into_message() {
        if (userlistMessage != null) {
            return userlistMessage;
        }
        if (messagesMessage != null) {
            return messagesMessage;
        }
        if (whoImIMessage != null) {
            return whoImIMessage;
        }
        return null;
    }

}
