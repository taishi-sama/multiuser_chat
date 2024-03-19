import java.util.List;

/**
 * Хранит имена активных участников, рассылаемый сервером.
 */
public class UserlistMessage implements IMessage {
    public List<String> current_users;
    @Override
    public MessageContainer intoContainer() {
        var messageContainer = new MessageContainer();
        messageContainer.userlistMessage = this;
        return messageContainer;
    }
}
