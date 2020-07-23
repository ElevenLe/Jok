package db;

public class MySQLDBUtil {
	private static final String INSTANCE = "laioffer.cebrg3yqqn9q.us-east-1.rds.amazonaws.com";
	private static final String PORT_NUM = "3306";
	public static final String DB_NAME = "Job";
	private static final String USERNAME = "eleven";
	private static final String PASSWORD = "password";
	public static final String URL = "jdbc:mysql://"
			+ INSTANCE + ":" + PORT_NUM + "/" + DB_NAME
			+ "?user=" + USERNAME + "&password=" + PASSWORD
			+ "&autoReconnect=true&serverTimezone=UTC";
}
