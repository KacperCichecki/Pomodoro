package application;

import java.awt.Dimension;
import java.awt.Toolkit;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class MyPopUp {

	private Stage stage = null;
	private DBConnetion dbConnetion = DBConnetion.getInstance();
	private Popup popup = null;

	// message to save to db
	private String message = null;

	// figure out dimensions of screen
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private int centerX = screenSize.width / 2;
	private int centerY = screenSize.height / 2;

	// constructor
	public MyPopUp(Stage stage) {
		this.stage = stage;
	}

	// show Pop up window to collect info about activity
	public void show() {

		if (popup == null) {
			popup = new Popup();
			popup.setX(centerX - 200);
			popup.setY(centerY - 200);

			Label label = new Label("Tell me what are you going to study");
			label.setWrapText(true);
			label.setStyle("-fx-font-size: 17;");

			TextArea textArea = new TextArea();

			if (message == null) {
				textArea.setPromptText("I'm gona study...");
			} else {
				textArea.setText(message);
			}

			Button hide = new Button("OK");
			hide.setOnAction((a) -> {
				message = textArea.getText();
				dbConnetion.saveDescription(message);
				popup.hide();
			});
			hide.setDefaultButton(true);

			VBox layout1 = new VBox(5);
			layout1.setAlignment(Pos.CENTER);
			layout1.setStyle("-fx-padding: 5; " + "-fx-background-color: #ff4d4d; "
					+ "-fx-border-color: #009933; -fx-border-width: 2;" + "-fx-border-radius: 10;"+
					"-fx-background-radius: 10;");

			layout1.getChildren().addAll(label, textArea, hide);
			popup.getContent().addAll(layout1);
		}
		popup.show(stage);
	}

}
