package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;


public class Main extends Application {
	Parent root;
	@Override
	public void start(Stage primaryStage) {
		try {
			root = FXMLLoader.load(Main.class.getResource("Sample.fxml"));

		} catch(Exception e) {
			System.out.println("Problem with FXMLLoader: cant load parent");
			//e.printStackTrace();
		}

		Scene scene = new Scene(root);
			//scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.sizeToScene();
			primaryStage.setResizable(false);
			primaryStage.setTitle("Pomadoro");
			primaryStage.show();
	}

	public void stop(){
		Controller.execute = false;
	}



	public static void main(String[] args) {
		launch(args);
	}
}
