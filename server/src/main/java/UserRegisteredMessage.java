public class UserRegisteredMessage implements IServerUserMessage{
    public UserHandle userHandle;
    public String newName;
    public UserRegisteredMessage(UserHandle userHandle, String newName) {
        this.userHandle = userHandle;
        this.newName = newName;
    }
}
