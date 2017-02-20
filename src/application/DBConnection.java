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

	// data required for database connection
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

	// Singleton pattern of database connection
	public static DBConnetion getInstance() {
		return dbConnection;
	}

	// get id of last event from data base
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

	// insert start time of given activity
	public void insertStartActivity(int id, String acivity) {
		String sql = "insert into demo.pomodoro (id, start, activity) " +
	"values (" + id + ", NOW(), '" + acivity + "')";
		try {
			myStmt = myCon.createStatement();
			myStmt.executeUpdate(sql);
			System.out.println(acivity + " time updated, id: " + id);
		} catch (SQLException e) {
			System.out.println("fail to insert start time of" + acivity);
			e.printStackTrace();
		} finally {
			try {
				myStmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// set stop time of study, duration of study and insert start time of relax
	public void setPauseTime(int id) {
		String sql = "UPDATE pomodoro SET stop= NOW() WHERE `id`=" + id;
		try {
			myStmt = myCon.createStatement();
			myStmt.executeUpdate(sql);
			System.out.println("stop time study updated, id: " + id);
			this.setDuration(id);
			this.insertStartActivity(id + 1, "relax");
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

	// set stop time and duration of relax and insert start time of study
	public void setStartAfterPauseTime(int id) {

		String sql = "UPDATE demo.pomodoro SET stop=NOW(), activity='relax' WHERE `id`=" + id;
		try {
			myStmt = myCon.createStatement();
			myStmt.executeUpdate(sql);
			System.out.println("stop time relax updated, id: " + id);
			this.setDuration(id);
			this.insertStartActivity(id + 1, "study");
		} catch (SQLException e) {
			System.out.println("fail to stop relax time update, id: " + id);
			e.printStackTrace();
		} finally {
			try {
				myStmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// set stop time and duration when program is closing
	public void registerTimeOfClosing() {
		int currentId = getLastId();
		String sql = "UPDATE pomodoro SET stop= NOW() WHERE `id`=" + currentId;
		try {
			myStmt = myCon.createStatement();
			myStmt.executeUpdate(sql);
			System.out.println("stop time of last activity updated, id: " + currentId);
			this.setDuration(currentId);
		} catch (SQLException e) {
			System.out.println("fail to stop last activity update");
			e.printStackTrace();
		} finally {
			try {
				myStmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// insert duration of activity
	private void setDuration(int id) {
		Timestamp start = null;
		Timestamp stop = null;
		// get form db start and stop of activity
		try {
			myStmt = myCon.createStatement();
			ResultSet myRs = myStmt.executeQuery("select * from demo.pomodoro where id=" + id);
			while (myRs.next()) {
				start = myRs.getTimestamp("start");
				stop = myRs.getTimestamp("stop");
			}
			System.out.println("start time read: " + start.getTime() + " start time read: " + stop.getTime());
		} catch (SQLException e) {
			System.out.println("setDuration fail");
			e.printStackTrace();
		} finally {
			try {
				myStmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		// calculate duration of activity and insert that time to db
		// very inefficient way to do this. Improve when know how to do this in
		// mySql automatically
		long diffrence = stop.getTime() - start.getTime();
		System.out.println("diffrence: " + diffrence);
		int seconds = (int) diffrence / 1000;
		int sec = seconds % 60;
		int minutes = (seconds / 60) % 60;
		int hours = (seconds / 3600);
		String sql = "UPDATE pomodoro SET duration = '" + String.format("%0,2d:%0,2d:%0,2d", hours, minutes, sec)
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

	// close connection to database
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
}
