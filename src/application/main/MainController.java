package application.main;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import application.config.AppConfig;
import application.main.service.ConcatService;
import application.main.service.InputOrganizationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class MainController implements Initializable {

	@FXML
	private ListView<Path> fileList;

	@FXML
	private TextArea terminalArea;

	@FXML
	private TextField ffmpegPathField;

	@FXML
	private Button startButton;
	
	@FXML
	private Button stopButton;

	public static final Logger log = Main.log;

	private ConcatService concatService;
	private InputOrganizationService inputOrganizationService;
	

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ffmpegPathField.setText(AppConfig.ACTIVECONFIG.getPathToFFmpeg().toString());
		inputOrganizationService = new InputOrganizationService();
	}

	@FXML
	private void handleAddAction(ActionEvent event) {
		log.log(Level.INFO, "add button clicked - trying to use FileChooser");

		inputOrganizationService.addEntries(fileList, event);
	}

	@FXML
	private void handleRemoveAction(ActionEvent event) {
		log.log(Level.INFO, "attempting to remove entries from file selection list");

		inputOrganizationService.deleteEntries(fileList);

	}

	@FXML
	private void handleUpAction(ActionEvent event) {
		inputOrganizationService.moveEntriesUp(fileList);
	}

	@FXML
	private void handleDownAction(ActionEvent event) {
		inputOrganizationService.moveEntriesDown(fileList);
	}

	@FXML
	private void handleConcatAction(ActionEvent event) {
		if (fileList.getItems().size() > 1) {
			log.log(Level.INFO, "asking for target file");
			
			File targetFile = concatService.askForOutputFile(event);
			
			if (targetFile != null) {
				// ensure targetFile has a file extension
				// TODO: this can lead to overwriting a file, without telling the user
				if(!targetFile.getName().endsWith(".mp4")){
					targetFile = new File(targetFile.getAbsolutePath() + ".mp4");
				}
				
				if (!targetFile.exists() || targetFile.delete()) {
					// TODO: notify user if deleting failed

					AppConfig.ACTIVECONFIG.setLastOutputFile(targetFile.toPath());

					log.log(Level.INFO, "starting concatenation");
					concatService = new ConcatService();
					try {
						Path listFile = concatService.createListFile(fileList.getItems());
						concatService.startConcatenation(terminalArea, startButton, stopButton, listFile, targetFile.toPath());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				startButton.setDisable(true);
				stopButton.setDisable(false);
			}
		} else {
			Alert notification = new Alert(AlertType.ERROR);
			notification.setHeaderText("You have not selected enough files to be concatenated.");
			notification.setContentText("You need two or more files.");
			notification.showAndWait();
		}
	}

	@FXML
	private void handleSetFfmpegPathAction(ActionEvent event) {
		log.log(Level.INFO, "selecting ffmpeg binary");

		inputOrganizationService.setFFmpegPath(ffmpegPathField, event);
	}
	
	@FXML
	private void handleStopAction(ActionEvent event) {
		concatService.stopStartedProcess(startButton, stopButton);
	}

	/**
	 * checks for given List if the contained Integers are compact, so for every
	 * item n with index i it is true that item m = n+1 with index j = i+1
	 * 
	 * @param numbers
	 * @return
	 */
	public static boolean isListCompact(List<Integer> numbers) {
		List<Integer> workingList = new ArrayList<Integer>(numbers);

		workingList.sort(Integer::compare);

		for (int i = 0; i < workingList.size() - 1; i++) {
			if (workingList.get(i) + 1 != workingList.get(i + 1)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Make sure the given file points to an existing directory and if not return a default exisiting directory.
	 * @param fileToSafe
	 * @return fileToSafe or File pointing to .
	 */
	public static File getSafeDirectory(File fileToSafe) {
		if (fileToSafe.exists() && fileToSafe.isDirectory()) {
			return fileToSafe;
		} else {
			return new File(".");
		}
	}

}
