package application;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;

class DBConnetion {

	String url = "jdbc:mysql://localhost:3306/demo";
	String user = "root";
	String password = "cichacz1";

	Connection myCon = null;
	Statement myStmt = null;

	private static DBConnetion dbConnection = new DBConnetion();

	private DBConnetion() {
		try {
			myCon = DriverManager.getConnection(url, user, password);
			myStmt = myCon.createStatement();
			System.out.println("connected to database");
		} catch (SQLException e) {
			System.out.println("fail to connect to db");
			e.printStackTrace();
		}
	}

	public static DBConnetion getInstance(){
		return dbConnection;
	}

	public int getLastId() {
		int result = 0;
		ResultSet myRs = null;
		try {
			myStmt = myCon.createStatement();
			myRs = myStmt.executeQuery("SELECT * FROM demo.pomodoro");

			while (myRs.next()) {
				result = myRs.getInt("id");
			}
			myRs.close();
			System.out.println("closed resultSet of getLastId");
		} catch (SQLException e) {
			System.out.println("can't read last id");
			e.printStackTrace();
			return 0;
		} finally {
			try {
				myStmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public void closeConnection() {
		if (myCon != null) {
			try {
				myCon.close();
				System.out.println("closed connection");
			} catch (SQLException e) {
				System.out.println("can't close connection");
				e.printStackTrace();
			}

		}
	}

	public void setStartTime(int id) {

		String sql = "insert into demo.pomodoro (id, start, activity) " + "values (" + id + ", NOW(), 'study')";

		try {
			myStmt = myCon.createStatement();
			myStmt.executeUpdate(sql);
			System.out.println("start time updated, id: " + id);
		} catch (SQLException e) {
			System.out.println("fail to start time update");
			e.printStackTrace();
		} finally {
			try {
				myStmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	public void setStopTime(int id) {
		String sql = "UPDATE pomodoro SET stop= NOW() WHERE `id`=" + id;

		try {
			myStmt = myCon.createStatement();
			myStmt.executeUpdate(sql);
			System.out.println("stop time study updated, id: " + id);
			this.setDurationOfStudy(id);
			this.insertStartRest(id + 1);

		} catch (SQLException e) {
			System.out.println("fail to stop study time update");
			e.printStackTrace();
		} finally {
			try {
				myStmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	private void insertStartRest(int id) {
		String sql = "insert into demo.pomodoro (id, start, activity) " + "values (" + id + ", NOW(), 'relax')";

		try {
			myStmt = myCon.createStatement();
			myStmt.executeUpdate(sql);
			System.out.println("start of relax inserted, id: " + id);
		} catch (SQLException e) {
			System.out.println("fail to start of relax insert");
			e.printStackTrace();
		} finally {
			try {
				myStmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	// (select start from demo.pomodoro start where id = " + id + ")
	// (select stop from demo.pomodoro stop where id = " + id + ")

	private void setDurationOfStudy(int id) {
		Timestamp start = null;
		Timestamp stop = null;

		try {
			myStmt = myCon.createStatement();
			ResultSet myRs = myStmt.executeQuery("select * from demo.pomodoro where id=" + id);
			while (myRs.next()) {
				start = myRs.getTimestamp("start");
				stop = myRs.getTimestamp("stop");
			}
			System.out.println("start time read: " + start.getTime() + " start time read: " + stop.getTime());

		} catch (SQLException e) {
			System.out.println("setDurationOfStudy fail");
			e.printStackTrace();
		} finally {
			try {
				myStmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		long diffrence = stop.getTime() - start.getTime();
		System.out.println("diffrence: " + diffrence);
		int seconds = (int) diffrence / 1000;
		int sec = seconds % 60;
		int minutes = (seconds / 60) % 60;
		int hours = (seconds / 3600);

		String sql = "UPDATE pomodoro SET duration = '" +
					String.format("%0,2d:%0,2d:%0,2d", hours, minutes, sec)
					+ "' WHERE `id`=" + id;

		try {
			myStmt = myCon.createStatement();
			myStmt.executeUpdate(sql);
			System.out.println("duration updated, id: " + id);
		} catch (SQLException e) {
			System.out.println("fail to duration update");
			e.printStackTrace();
		} finally {
			try {
				myStmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

}
