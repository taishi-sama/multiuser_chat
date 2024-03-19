/**
 * Сообщает серверному потоку о новом клиенте, содержит UserHandle.
 */
public class NewUserJoinsMessage implements IServerUserMessage {
    public UserHandle userhandle;

    public NewUserJoinsMessage(UserHandle userhandle) {
        this.userhandle = userhandle;
    }
}
