import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class DBTest {
    @Test
    public void TestDBCreation() throws SQLException {
        var db = DB.makeInMemory();
        db.EnsureRegisteredUsersTableCreation();
        db.EnsureUserlogTableCreation();
        var res = db.connection.prepareStatement("SHOW TABLES").executeQuery();
        var al = new ArrayList<String>();
        while (res.next()) {
            al.add(res.getString("TABLE_NAME"));
        }
        //for (var s: al) {
        //    System.out.println(s);
        //}
        Assert.assertTrue(al.contains("REGISTEREDUSERS"));
        Assert.assertTrue(al.contains("USERLOG"));
        db.connection.close();
    }
    @Test
    public void TestGettingUser() throws SQLException {
        var db = DB.makeInMemory();
        db.EnsureUserlogTableCreation();
        db.EnsureRegisteredUsersTableCreation();
        var none = db.getIdOfUser("testUser");
        Assert.assertNull(none);
        var id = db.insertNewUser("testUser", LocalDateTime.of(2024, 3, 7, 10, 0), true);
        var some = db.getIdOfUser("testUser");
        Assert.assertEquals(id, some.intValue());
        db.connection.close();
    }
    @Test
    public void TestSettingUserStatus() throws SQLException {
        var db = DB.makeInMemory();
        db.EnsureUserlogTableCreation();
        db.EnsureRegisteredUsersTableCreation();
        var time = LocalDateTime.of(2024, 3, 7, 10, 0);
        var id = db.insertNewUser("testUser", time, false);
        {
            var status = db.connection.createStatement().executeQuery("SELECT online, lastvisit FROM " + db.RegisteredUsersTable + " WHERE id = " + id + ";");
            status.first();
            var s = db.getUserOnlineStatus(id);
            var t = status.getTimestamp("lastvisit");
            Assert.assertFalse(s);
            Assert.assertEquals(t, Timestamp.valueOf(time));
        }
        var newTime = LocalDateTime.of(2024, 3, 7, 12, 0);
        db.updateLastJoinAndOnlineStatus(id, true, newTime);
        {
            var status = db.connection.createStatement().executeQuery("SELECT online, lastvisit FROM " + db.RegisteredUsersTable + " WHERE id = " + id + ";");
            status.first();
            var s = db.getUserOnlineStatus(id);
            var t = status.getTimestamp("lastvisit");
            Assert.assertTrue(s);
            Assert.assertEquals(t, Timestamp.valueOf(newTime));
        }
        db.updateUserStatus(id, false);
        {
            var status = db.connection.createStatement().executeQuery("SELECT online, lastvisit FROM " + db.RegisteredUsersTable + " WHERE id = " + id + ";");
            status.first();
            var s = db.getUserOnlineStatus(id);
            var t = status.getTimestamp("lastvisit");
            Assert.assertFalse(s);
            Assert.assertEquals(t, Timestamp.valueOf(newTime));
        }

        Assert.assertEquals(newTime, db.getUserLastJoinTime(id));

        db.connection.close();
    }
    @Test
    public void TestGetOnlineUsers() throws SQLException {

        var db = DB.makeInMemory();
        db.EnsureRegisteredUsersTableCreation();

        var time = LocalDateTime.of(2024, 3, 7, 10, 0);
        var online = new String[]{"online1", "online2", "online3"};
        var offline = new String[]{"offline1", "offline2", "offline3"};
        for (var s: online) {
            db.insertNewUser(s, time, true);
        }
        for (var s: offline) {
            db.insertNewUser(s, time, false);
        }
        var list = db.getOnlineUsers();
        for (var s: online) {
            Assert.assertTrue(list.contains(s));
        }
        for (var s: offline) {
            Assert.assertFalse(list.contains(s));
        }
        db.connection.close();
    }

}
