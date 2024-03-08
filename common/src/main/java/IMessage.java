import java.io.Serializable;

public interface IMessage extends Serializable {
    public MessageContainer intoContainer();
}
