public class UserSentMessage implements IServerUserMessage{
    public UserHandle userHandle;
    public String content;
    public UserSentMessage(UserHandle userHandle, String content) {
        this.userHandle = userHandle;
        this.content = content;
    }
}
