/**
 * Хранит в себе одну из разновидностей сообщений в качестве поля
 */
public class MessageContainer {
    public UserlistMessage userlistMessage;
    public UserMessagesMessage messagesMessage;
    public WhoImIMessage whoImIMessage;

    /**
     * Возвращает хранящееся внутри сообщение.
     * @return сообщение или null, если контейнер пустой.
     */
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
