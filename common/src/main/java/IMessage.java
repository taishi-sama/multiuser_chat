import java.io.Serializable;

/**
 * Интерфейс классов, использующихся в межсетевом общении.
 */
public interface IMessage extends Serializable {
    /**
     * Оборачивает класс, реализующий этот интерфейс, в специальный класс-контейнер MessageContainer для более лёгкой сериализации.
     * @return класс-контейнер для сериализации по сети
     */
    public MessageContainer intoContainer();
}
