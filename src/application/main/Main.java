package application.main;
	
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import application.config.AppConfig;
import application.config.ConfigAccessor;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {
	public static final Logger log = Logger.getLogger("slideshowFX");

	public static final String LAYOUTPATH = "MainLayout.fxml";

	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource(
					LAYOUTPATH));
			Scene scene = new Scene(root);
			scene.getStylesheets().add(
					getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setMinWidth(720);
			primaryStage.setMinHeight(640);
			primaryStage.setMaximized(true);
			primaryStage.show();
			primaryStage.setTitle("JConcat");
			
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				
				@Override
				public void handle(WindowEvent event) {
					try {
						ConfigAccessor.writeToDefault(AppConfig.ACTIVECONFIG);
					} catch (IOException e) {
						// TODO notify about failed save
						e.printStackTrace();
					}
				}
			});
			
		} catch (IOException e) {
			log.log(Level.SEVERE,
					"could not read fxml file used for layout from " + LAYOUTPATH + " - unable to show UI; program will terminate",
					e);
		}
	}

	public static void main(String[] args) {
		String mainArgs = "";
		for (String a : args) {
			mainArgs += " " + a;
		}
		log.log(Level.INFO, "program call arguments:" + mainArgs);
		launch(args);
	}
}
