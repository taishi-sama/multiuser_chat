public class UserRegistered implements IServerUserMessage{
    public UserHandle userHandle;
    public String newName;
    public UserRegistered(UserHandle userHandle, String newName) {
        this.userHandle = userHandle;
        this.newName = newName;
    }
}
