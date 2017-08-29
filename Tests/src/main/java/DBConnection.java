import java.sql.*;


public class DBConnection {
    public static void main(String[] args) throws SQLException {
       //connectDb();
    }

    public void connectDb() throws SQLException{

        try {

            Class.forName("oracle.jdbc.driver.OracleDriver");

        } catch (ClassNotFoundException e) {

            System.out.println("Where is your Oracle JDBC Driver?");
            e.printStackTrace();
            return;

        }

        System.out.println("Oracle JDBC Driver Registered!");

        Connection connection = null;

        try {

            connection = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:DBOne", "artemy", "artemy");

        } catch (SQLException e) {

            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            return;

        }

        if (connection != null) {
            System.out.println("You made it, take control your database now!");
        } else {
            System.out.println("Failed to make connection!");
        }
        Statement statement = null;

        statement = connection.createStatement();
        /*
        ResultSet result1 = statement.executeQuery(
                "SELECT * FROM REQUEST_REJECTED where ID = 71");
        System.out.println("Выводим statement");
        while (result1.next()) {
            System.out.println("Номер в выборке #" + result1.getRow()
                    + "\t Номер в базе #" + result1.getInt("ID")
                    + "\t" + result1.getString("DATETIME") + " " + result1.getString("GUID") + " " + result1.getString("ERROR_DESCR"));
        }
        */
        statement.executeUpdate(
                "INSERT INTO REQUEST_STATUS(GUID, CLIENT_ID, DATETIME, REQUEST_STATUS, ALERT_STATUS, CARD_NO, REQUEST_DATE, PHONE_NUM, AMOUNT) " +
                        "values('fd8624af-d8e6-4f9c-80fc-dd57917db727', '777', '23.08.17 11:54:05,121707000', 'PROCESSED', 'DELIVERED', '123', '23.08.17', '320502', '700')");

    }

}

