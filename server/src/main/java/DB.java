import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class DB {
    public final String UserlogTable = "Userlog";
    public final String RegisteredUsersTable = "RegisteredUsers";
    Connection connection;
    public static DB makeInMemory() throws SQLException {
        var db = new DB("mem:test");
        return db;
    }
    public DB(String filepath) throws SQLException {
        connection = DriverManager.getConnection ("jdbc:h2:"+filepath);
    }
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
    public void insertNewMessage(String content, int id, LocalDateTime time) throws SQLException {
        try (var statement = connection.prepareStatement("INSERT INTO " + UserlogTable + "(userid, time, content) VALUES (?, ?, ?)")) {
            statement.setInt(1, id);
            statement.setTimestamp(2, Timestamp.valueOf(time));
            statement.setString(3, content);
            statement.executeUpdate();
        }
    }
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
