
package application;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

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

	 String sound = Controller.class.getResource("alarm.wav").toString();
	 private final AudioClip ALARM = new AudioClip(sound);


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		startButton.setDisable(true);
	}

	private long now(){
		return System.currentTimeMillis();
	}

	public void setTime(ActionEvent actionEvent) {
		startButton.setDisable(false);

		try {
			givenTime = 1000*60 * Long.valueOf(setArea.getText());
		} catch (NumberFormatException e) {
			System.out.println("illegal input");
			givenTime = 0;
		}

		timeArea.setText("00:" + givenTime/60000 + ":00");
	}

	public void startCounting(ActionEvent actionEvent) {

		long startTime = now();
		long setTime = startTime + givenTime;

		Thread t1 = new Thread(() -> {
			System.out.println("thread started");
			execute = true;

			do{
				try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
				elapsedTime = (setTime - now())/1000;

				long minutes = elapsedTime/60;
				long seconds = elapsedTime%60;


				timeArea.setText(String.format("00:%d:%d", minutes, seconds));
			}while(elapsedTime > 0 && execute);

			if(execute) ALARM.play();
			startButton.setDisable(true);

		});

		t1.start();

	}

}