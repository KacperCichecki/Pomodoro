package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Main extends Application {
	Parent root;

	@Override
	public void start(Stage primaryStage) {
		try {
			root = FXMLLoader.load(Main.class.getResource("Sample.fxml"));

		} catch (Exception e) {
			System.out.println("Problem with FXMLLoader: cant load parent");
		}

		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.sizeToScene();
		primaryStage.setResizable(false);
		primaryStage.setTitle("Pomadoro");
		primaryStage.getIcons().add(new Image(getClass().getResource("pomodoro.png").toString()));
		primaryStage.show();
		
	}

	public void stop() {
		if (Controller.threadExecutor != null) {
			Controller.threadExecutor.shutdown();
		}
		if (Controller.dbConnetion != null) {
			Controller.dbConnetion.registerStopDurationLastActivity();
			Controller.dbConnetion.closeConnection();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
