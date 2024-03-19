import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class DB {
    public final String UserlogTable = "Userlog";
    public final String RegisteredUsersTable = "RegisteredUsers";
    Connection connection;

    /**
     * Создаёт базу данных H2 в памяти.
     * @return созданная база данных
     * @throws SQLException
     */
    public static DB makeInMemory() throws SQLException {
        var db = new DB("mem:test");
        return db;
    }

    /**
     * Создаёт подключение к базе данных H2 с заданной строкой подключения.
     * @param filepath строка подключения
     * @throws SQLException
     */
    public DB(String filepath) throws SQLException {
        connection = DriverManager.getConnection ("jdbc:h2:"+filepath);
    }

    /**
     * Создаёт таблицу сообщений со следующим содержимым:
     * id
     * content
     * userid
     * time
     * @throws SQLException
     */
    public void EnsureUserlogTableCreation() throws SQLException {
        try(var statement = connection.createStatement()) {
            var sql = "CREATE TABLE IF NOT EXISTS " +
                    UserlogTable +
                    "(" +
                    "id INTEGER not NULL AUTO_INCREMENT, " +
                    "content VARCHAR(255), " +
                    "userid INTEGER not NULL, " +
                    "time TIMESTAMP, " +
                    "PRIMARY KEY (id)" +
                    ")";
            statement.executeUpdate(sql);
        }
    }

    /**
     * Создаёт таблицу пользователей со следующим содержимым:
     * id
     * username
     * lastvisit
     * online
     * @throws SQLException
     */
    public void EnsureRegisteredUsersTableCreation() throws SQLException {
        try(var statement = connection.createStatement()) {
            var sql = "CREATE TABLE IF NOT EXISTS " +
                    RegisteredUsersTable +
                    "(" +
                    "id INTEGER not NULL AUTO_INCREMENT, " +
                    "username VARCHAR(255) not NULL UNIQUE, " +
                    "lastvisit TIMESTAMP, " +
                    "online boolean, " +
                    "PRIMARY KEY (id)" +
                    ")";
            statement.executeUpdate(sql);
        }
    }

    /**
     * Добавляет сообщение в таблицу сообщений.
     * @param content текст сообщения
     * @param id ID пользователя
     * @param time время получения
     * @throws SQLException
     */
    public void insertNewMessage(String content, int id, LocalDateTime time) throws SQLException {
        try (var statement = connection.prepareStatement("INSERT INTO " + UserlogTable + "(userid, time, content) VALUES (?, ?, ?)")) {
            statement.setInt(1, id);
            statement.setTimestamp(2, Timestamp.valueOf(time));
            statement.setString(3, content);
            statement.executeUpdate();
        }
    }

    /**
     * Получает Id пользователя в таблице по имени, возвращает null, если пользователя с таким именем нету.
     * @param username имя пользователя
     * @return id пользователя или null
     * @throws SQLException
     */
    public Integer getIdOfUser(String username) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT id FROM " + RegisteredUsersTable + " WHERE username = ?")) {
            statement.setString(1, username);
            var res = statement.executeQuery();
            if (res.first())
            {
                return res.getInt("id");
            }
            else
                return null;
        }
    }

    /**
     * Обновляет статус online пользователя
     * @param userId Id пользователя
     * @param online новый статус online
     * @throws SQLException
     */
    public void updateUserStatus(int userId, boolean online) throws SQLException {
        try (var statement = connection.prepareStatement("UPDATE " + RegisteredUsersTable + " SET online = ? WHERE id = ?")){
            statement.setBoolean(1, online);
            statement.setInt(2, userId);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("No user with such ID found.");
            }
        }
    }

    /**
     * Обновляет дату последнего захода и статус онлайн пользователя за один запрос.
     * @param userId Id пользователя
     * @param online новый статус online
     * @param localDateTime новое время последнего захода
     * @throws SQLException
     */
    public void updateLastJoinAndOnlineStatus(int userId, boolean online, LocalDateTime localDateTime) throws SQLException {
        try (var statement = connection.prepareStatement("UPDATE " + RegisteredUsersTable + " SET (lastvisit, online) = (?, ?) WHERE id = ?")){
            statement.setTimestamp(1, Timestamp.valueOf(localDateTime));
            statement.setBoolean(2, online);
            statement.setInt(3, userId);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("No user with such ID found.");
            }
        }
    }

    /**
     * Добавляет нового пользователя, возвращая его id.
     * @param username имя нового пользователя
     * @param lastvisit дата создания
     * @param online статус онлайн
     * @return ID нового пользователя
     * @throws SQLException
     */
    public int insertNewUser(String username, LocalDateTime lastvisit, boolean online) throws SQLException {
        try (var statement = connection.prepareStatement("INSERT INTO " + RegisteredUsersTable + " (username, lastvisit, online) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)){
            statement.setString(1, username);
            statement.setTimestamp(2, Timestamp.valueOf(lastvisit));
            statement.setBoolean(3, online);
            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt("id");
                }
                else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Получает время последнего захода пользователя по его id.
     * @param id Id пользователя
     * @return время последнего захода
     * @throws SQLException
     */
    public LocalDateTime getUserLastJoinTime(int id) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT lastvisit FROM " + RegisteredUsersTable + " WHERE id = ?")) {
            statement.setInt(1, id);
            var res = statement.executeQuery();
            if (res.first())
            {
                return res.getTimestamp("lastvisit").toLocalDateTime();
            }
            else
                return null;
        }
    }

    /**
     * Получает статус пользователя по его id.
     * @param id Id пользователя
     * @return статус онлайн
     * @throws SQLException
     */
    public Boolean getUserOnlineStatus(int id) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT online FROM " + RegisteredUsersTable + " WHERE id = ?")) {
            statement.setInt(1, id);
            var res = statement.executeQuery();
            if (res.first())
            {
                return res.getBoolean("online");
            }
            else
                return null;
        }
    }

    /**
     * Получает список имён пользователей онлайн.
     * @return список пользователей, онлайн сейчас.
     * @throws SQLException
     */
    public ArrayList<String> getOnlineUsers() throws SQLException {
        try (var statement = connection.prepareStatement("SELECT username FROM " + RegisteredUsersTable + " WHERE online = true")){
            var al = new ArrayList<String>();
            var res = statement.executeQuery();
            while (res.next()) {
                al.add(res.getString("username"));
            }
            return al;
        }
    }
}
