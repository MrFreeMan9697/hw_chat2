package server;

import java.sql.*;
import java.util.Date;

public class SQLHandler {

    private static Connection connection;
    private static PreparedStatement psGetNickname;
    private static PreparedStatement psRegistration;
    private static PreparedStatement psChangeNick;

    private static PreparedStatement psAddMessage;
    private static PreparedStatement psGetMessageForNick;
    private static PreparedStatement psGetHistoryFromNick;


    public static boolean connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:main.db");
            prepareAllStatements();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }


    private static void prepareAllStatements() throws SQLException {
        psGetNickname = connection.prepareStatement("SELECT nickname FROM users WHERE login = ? AND password = ?;");
        psRegistration = connection.prepareStatement("INSERT INTO users(login, password, nickname) VALUES (?, ?, ?);");
        psChangeNick = connection.prepareStatement("UPDATE users SET nickname = ? WHERE nickname = ?;");

        psAddMessage = connection.prepareStatement("INSERT INTO messags (sender, receiver, text, date) VALUES (\n" +
                "(SELECT id FROM users WHERE nickname=?),\n" +
                "(SELECT id FROM users WHERE nickname=?),\n" +
                "?, ?)");
        psGetHistoryFromNick = connection.prepareStatement("SELECT * FROM messags WHERE sender = ? OR receiver = ?;");
    }

    public static String getNicknameByLoginAndPassword(String login, String password) {
        String nick = null;
        try {
            psGetNickname.setString(1,login);
            psGetNickname.setString(2,password);
            ResultSet rs = psGetNickname.executeQuery();
            if(rs.next()) {
                nick = rs.getString(1);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nick;
    }

    public static boolean registration (String login, String password, String nickname) {
        try {
            psRegistration.setString(1, login);
            psRegistration.setString(2, password);
            psRegistration.setString(3, nickname);
            psRegistration.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean changeNick (String odlNick, String newNickname) {
        try {
            psChangeNick.setString(1,newNickname);
            psChangeNick.setString(2,odlNick);
            psChangeNick.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addMessage (String sender, String receiver, String text, Date date) {
        try {
            psAddMessage.setString(1,sender);
            psAddMessage.setString(2,receiver);
            psAddMessage.setString(3,text);
            psAddMessage.setDate(4, (java.sql.Date) date);
            psAddMessage.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getMessageForNick (String nick) {
        StringBuilder sb = new StringBuilder();

        try {
            psGetMessageForNick.setString(1,nick);
            psGetMessageForNick.setString(2,nick);
            ResultSet rs = psGetMessageForNick.executeQuery();

            while (rs.next()) {
                String sender = rs.getString(1);
                String receiver = rs.getString(2);
                String text = rs.getString(3);
                Date date = rs.getDate(4);

                if (receiver.equals("null")) {
                    sb.append(String.format("%s : %s\n", sender, text));
                } else {
                    sb.append(String.format("[ %s ] private [ %s ]: %s\n", sender, receiver, text));
                }
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static void disconnect() {
        try {
            psRegistration.close();
            psGetNickname.close();
            psChangeNick.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
