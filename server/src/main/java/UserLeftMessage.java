/**
 * Сообщает серверному потоку о том, что пользователь отключился от сервера. Содержит UserHandle отключившегося клиента.
 */
public class UserLeftMessage implements IServerUserMessage{
    public UserHandle userHandle;

    public UserLeftMessage(UserHandle userHandle) {
        this.userHandle = userHandle;
    }
}
