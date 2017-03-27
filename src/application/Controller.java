
package application;

import java.net.URL;
import java.sql.Date;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class Controller implements javafx.fxml.Initializable {

	@FXML
	private TextArea timeArea;
	@FXML
	private TextField setArea;
	@FXML
	private Button startButton;
	@FXML
	private Button setButton;
	@FXML
	private ProgressBar progresBar;

	// main stage of the program
	private Stage stage = null;

	// popUp to collect info about activity
	MyPopUp popUp = null;

	private long givenTime = 0;
	private long startOfWaiting = 0;
	private long timeOfWaiting = 0;
	private long startTime = 0;
	// use it variable to decide whether this activity should be added to db
	static int timeOfPauseWithoutRegistration = 15;

	static ScheduledThreadPoolExecutor threadExecutor = null;
	static StateOfTimer state;
	static DBConnetion dbConnetion = null;
	// id of last record in my pomodoro table in db
	int currentLastId = 0;
	//flag to show whether time was already set
	private boolean justStarted = true;

	// define sound of siren
	String sound = Controller.class.getResource("alarm.wav").toString();
	private final AudioClip ALARM = new AudioClip(sound);

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		dbConnetion = DBConnetion.getInstance();
		state = StateOfTimer.PAUSED;
	}

	//method to set and reset time
	public void setTime(ActionEvent actionEvent) {
		if (threadExecutor != null) {
			threadExecutor.shutdown();
			System.out.println("threadExecutor shutdown");
		}

		if (stage == null) {
			stage = (Stage) progresBar.getScene().getWindow();
		}

		if (popUp == null) {
			popUp = new MyPopUp(stage);
		}

		if(!justStarted){
			dbConnetion.registerStopDurationLastActivity();
		}
		justStarted = false;

		startButton.setText(">>");
		setButton.setText("reset");
		startButton.setDisable(false);
		state = StateOfTimer.BEFORE_START;

		try {
			givenTime = (long) 1000000000 * 60 * Long.valueOf(setArea.getText());
		} catch (NumberFormatException e) {
			System.out.println("illegal input");
			givenTime = 0;
		}

		int hours = Math.round((givenTime / 1000000000) / 3600);
		int minuts = Math.round(((givenTime / 1000000000) - hours * 3600) / 60);
		int sec = Math.round((givenTime / 1000000000) % 60);
		timeArea.setText(String.format("%0,2d : %0,2d : %0,2d", hours, minuts, sec));

		System.out.println("when click setTime: " + state);
	}

	//method to start counting set time
	public void startCounting(ActionEvent actionEvent) {
		// when click PAUSE
		if (state == StateOfTimer.STARTED_AND_RUNNING || state == StateOfTimer.STARTED) {
			threadExecutor.shutdown();
			currentLastId = dbConnetion.getLastId();
			dbConnetion.setPauseTime(currentLastId);
			startOfWaiting = System.nanoTime();
			startButton.setText(">>");
			state = StateOfTimer.PAUSED;
			System.out.println("when click PAUSE; startCounting:  " + state);
			setButton.setDisable(false);
		}

		// when click START after setting time
		else if (state == StateOfTimer.BEFORE_START) {
			currentLastId = dbConnetion.getLastId();
			dbConnetion.insertStartActivity(currentLastId + 1, "study");
			startTime = System.nanoTime() + givenTime;
			startButton.setText("||");
			System.out.println("startTime " + startTime);
			state = StateOfTimer.STARTED_AND_RUNNING;
			System.out.println("when click START first time; startCounting: " + state);
			setButton.setDisable(true);
		}

		// when click START after PAUSE
		else if (state == StateOfTimer.PAUSED) {
			currentLastId = dbConnetion.getLastId();
			dbConnetion.setStartAfterPauseTime(currentLastId);
			timeOfWaiting = System.nanoTime() - startOfWaiting;
			startTime += timeOfWaiting;
			startButton.setText("||");
			state = StateOfTimer.STARTED;
			System.out.println("when click START after PAUSE; startCounting: " + state);
			setButton.setDisable(true);
		}

		// this code runs every time apart from starting first time
		if (state == StateOfTimer.STARTED_AND_RUNNING || state == StateOfTimer.PAUSED) {
			System.out.println("create new ScheduledThreadPoolExecutor");
			System.out.println("startCounting: " + state);
			threadExecutor = new ScheduledThreadPoolExecutor(1);
			threadExecutor.scheduleAtFixedRate(new Task(), 0, 500, TimeUnit.MILLISECONDS);
			threadExecutor.setRemoveOnCancelPolicy(true);
		}

		// show popup to save info about study when click start of study
		if (state == StateOfTimer.STARTED || state == StateOfTimer.STARTED_AND_RUNNING) {
			popUp.show();
		}
	}

	class Task implements Runnable {

		@Override
		public void run() {

			// when click START
			if (state == StateOfTimer.STARTED_AND_RUNNING) {

				// System.out.println("run when click START " + state);
				long currnetTime = System.nanoTime();
				long remainingTime = startTime - currnetTime;
				// System.out.println("remainingTime " + remainingTime /1000000000);
				long seconds = (TimeUnit.NANOSECONDS.toSeconds(remainingTime)) % 60;
				long minutes = (TimeUnit.NANOSECONDS.toMinutes(remainingTime)) % 60;
				long hours = (TimeUnit.NANOSECONDS.toHours(remainingTime)) % 60;

				timeArea.setText(String.format("%0,2d : %0,2d : %0,2d", hours, minutes, seconds));

				double elapsedPercent = (double) remainingTime / givenTime;
				if (elapsedPercent > 0) {
					progresBar.setProgress(elapsedPercent);
				}

				if (remainingTime < 1) {
					long stopTime = System.nanoTime();
					ALARM.play();
					threadExecutor.shutdown();
					startButton.setDisable(true);
					System.out.println("stop time " + stopTime);
					currentLastId = dbConnetion.getLastId();
					dbConnetion.setPauseTime(currentLastId);
					state = StateOfTimer.PAUSED;
				}
			}

			// when click START after PAUSE
			if (state == StateOfTimer.STARTED) {
				// System.out.println("run when click START after PAUSE " + state);
				long currnetTime = System.nanoTime();
				long remainingTime = startTime - currnetTime;
				// System.out.println("remainingTime " + remainingTime / 1000000000);

				long seconds = (TimeUnit.NANOSECONDS.toSeconds(remainingTime)) % 60;
				long minutes = (TimeUnit.NANOSECONDS.toMinutes(remainingTime)) % 60;
				long hours = (TimeUnit.NANOSECONDS.toHours(remainingTime)) % 60;

				timeArea.setText(String.format("%0,2d : %0,2d : %0,2d", hours, minutes, seconds));

				double elapsedPercent = (double) remainingTime / givenTime;
				if (elapsedPercent > 0) {
					progresBar.setProgress(elapsedPercent);
				}

				if (remainingTime < 1) {
					long stopTime = System.nanoTime();
					ALARM.play();
					threadExecutor.shutdown();
					startButton.setDisable(true);
					System.out.println("stop time " + stopTime);
					currentLastId = dbConnetion.getLastId();
					dbConnetion.setPauseTime(currentLastId);
					state = StateOfTimer.PAUSED;
				}
			}
		}
	}

}