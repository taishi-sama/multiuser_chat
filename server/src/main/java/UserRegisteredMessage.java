/**
 * Сообщает серверному потоку о том, что клиент прислал запрос на логин. Содержит UserHandle и имя пользователя, под которым пользователь хочет зайти.
 */
public class UserRegisteredMessage implements IServerUserMessage{
    public UserHandle userHandle;
    public String newName;
    public UserRegisteredMessage(UserHandle userHandle, String newName) {
        this.userHandle = userHandle;
        this.newName = newName;
    }
}
