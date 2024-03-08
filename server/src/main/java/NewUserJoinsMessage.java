public class NewUserJoinsMessage implements IServerUserMessage {
    public UserHandle userhandle;

    public NewUserJoinsMessage(UserHandle userhandle) {
        this.userhandle = userhandle;
    }
}
