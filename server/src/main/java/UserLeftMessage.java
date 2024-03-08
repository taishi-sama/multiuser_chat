public class UserLeftMessage implements IServerUserMessage{
    public UserHandle userHandle;

    public UserLeftMessage(UserHandle userHandle) {
        this.userHandle = userHandle;
    }
}
