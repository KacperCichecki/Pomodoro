
package application;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.media.AudioClip;

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

	private long givenTime = 0;
	private long startOfWaiting = 0;
	private long timeOfWaiting = 0;
	private long startTime = 0;
	static ScheduledThreadPoolExecutor threadExecutor = null;
	static StateOfTimer state;

	// define sound of siren
	String sound = Controller.class.getResource("alarm.wav").toString();
	private final AudioClip ALARM = new AudioClip(sound);

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		state = StateOfTimer.PAUSED;
		startButton.setDisable(true);
	}

	public void setTime(ActionEvent actionEvent) {
		if(threadExecutor != null){
			threadExecutor.shutdown();
		}
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
		timeArea.setText("00:" + setArea.getText() + ":00");
		System.out.println("when click setTime: " + state);
	}

	public void startCounting(ActionEvent actionEvent) {
		// when click PAUSE
		if (state == StateOfTimer.STARTED_AND_RUNNING || state == StateOfTimer.STARTED) {
			threadExecutor.shutdown();
			startOfWaiting = System.nanoTime();
			startButton.setText(">>");
			state = StateOfTimer.PAUSED;
			System.out.println("when click PAUSE");
			System.out.println("startCounting: " + state);
		}
		// when click START
		else if (state == StateOfTimer.BEFORE_START) {
			startTime = System.nanoTime() + givenTime;
			startButton.setText("||");
			System.out.println("startTime " + startTime);
			state = StateOfTimer.STARTED_AND_RUNNING;
			System.out.println("when click START");
			System.out.println("startCounting: " + state);
		}

		// when click START after PAUSE
		else if (state == StateOfTimer.PAUSED) {
			timeOfWaiting = System.nanoTime() - startOfWaiting;
			startTime += timeOfWaiting;
			startButton.setText("||");
			state = StateOfTimer.STARTED;
			System.out.println("when click START after PAUSE");
			System.out.println("startCounting: " + state);
		}

		if (state == StateOfTimer.STARTED_AND_RUNNING || state == StateOfTimer.PAUSED) {
			System.out.println("create new ScheduledThreadPoolExecutor");
			System.out.println("startCounting: " + state);
			threadExecutor = new ScheduledThreadPoolExecutor(1);
			threadExecutor.scheduleAtFixedRate(new Task(), 0, 1, TimeUnit.SECONDS);
			threadExecutor.setRemoveOnCancelPolicy(true);
		}
	}

	class Task implements Runnable {
		@Override
		public void run() {

			// when click START
			if (state == StateOfTimer.STARTED_AND_RUNNING) {
				System.out.println("run when click START " + state);
				long currnetTime = System.nanoTime();
				long remainingTime = startTime - currnetTime;
				System.out.println("remainingTime " + remainingTime / 1000000000);

				int minuts = Math.round((remainingTime / 1000000000) / 60);
				int sec = Math.round((remainingTime / 1000000000) % 60);
				timeArea.setText(String.format("%0,2d : %0,2d", minuts, sec));

				double elapsedPercent = (double) remainingTime / givenTime;
				if (elapsedPercent > 0) {
					progresBar.setProgress(elapsedPercent);
				}

				if (remainingTime < 1) {
					long stopTime = System.nanoTime();
					ALARM.play();
					startButton.setDisable(false);
					threadExecutor.shutdown();
					System.out.println("stop time " + stopTime);
				}
			}

			// when click START after PAUSE
			if (state == StateOfTimer.STARTED) {
				System.out.println("run when click START after PAUSE " + state);
				long currnetTime = System.nanoTime();
				long remainingTime = startTime - currnetTime;
				System.out.println("remainingTime " + remainingTime / 1000000000);

				int minuts = Math.round((remainingTime / 1000000000) / 60);
				int sec = Math.round((remainingTime / 1000000000) % 60);
				timeArea.setText(String.format("%0,2d : %0,2d", minuts, sec));

				double elapsedPercent = (double) remainingTime / givenTime;
				if (elapsedPercent > 0) {
					progresBar.setProgress(elapsedPercent);
				}

				if (remainingTime < 1) {
					long stopTime = System.nanoTime();
					ALARM.play();
					startButton.setDisable(false);
					threadExecutor.shutdown();
					System.out.println("stop time " + stopTime);
				}
			}
		}
	}

}