
package application;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javafx.scene.media.AudioClip;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class Controller implements javafx.fxml.Initializable {

	@FXML
	private TextArea timeArea;
	@FXML
	private TextField setArea;
	@FXML
	private Button startButton;
	@FXML
	private ProgressBar progresBar;

	private long givenTime;
	private long elapsedTime;
	static volatile boolean execute;
	static ScheduledThreadPoolExecutor threadExecutor = null;

	// define sound of siren
	String sound = Controller.class.getResource("alarm.wav").toString();
	private final AudioClip ALARM = new AudioClip(sound);

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		startButton.setDisable(true);
	}

	public void setTime(ActionEvent actionEvent) {
		startButton.setDisable(false);

		try {
			givenTime = (long)1000000000 * 60 * Long.valueOf(setArea.getText());
		} catch (NumberFormatException e) {
			System.out.println("illegal input");
			givenTime = 0;
		}

		timeArea.setText("00:" + setArea.getText() + ":00");

		double result = (double)58989893/60000000;
		System.out.println(result);
	}

	public void startCounting(ActionEvent actionEvent) {

		startButton.setDisable(true);
		long startTime = System.nanoTime() + givenTime;
		execute = true;
		System.out.println("startTime " + startTime);

		class Task implements Runnable {

			@Override
			public void run() {
				long currnetTime = System.nanoTime();
				long remainingTime = startTime - currnetTime;
				System.out.println("remainingTime " + remainingTime/1000000000);

				int minuts = Math.round((remainingTime/ 1000000000) / 60);
				int sec = Math.round((remainingTime / 1000000000) % 60);
				timeArea.setText(String.format("%0,2d : %0,2d", minuts, sec));

				double elapsedPercent = (double)remainingTime/givenTime;
				if(elapsedPercent>0){
					progresBar.setProgress(elapsedPercent);
				}

				if (remainingTime < 1 && execute) {
					long stopTime = System.nanoTime();
					ALARM.play();
					startButton.setDisable(false);
					threadExecutor.shutdown();
					System.out.println("stop time " + stopTime);
				}
			}
		}
		threadExecutor = new ScheduledThreadPoolExecutor(1);
		threadExecutor.scheduleWithFixedDelay(new Task(), 0, 1, TimeUnit.SECONDS);
		threadExecutor.setRemoveOnCancelPolicy(true);
	}

}