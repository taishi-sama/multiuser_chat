public class MessageContainer {
    public UserlistMessage userlistMessage;
    public UserMessagesMessage messagesMessage;
    public WhoImIMessage whoImIMessage;
    public IMessage intoMessage() {
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
