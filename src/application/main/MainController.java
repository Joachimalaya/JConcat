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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class MainController implements Initializable {

	@FXML
	private ListView<Path> fileList;

	@FXML
	private TextArea terminalArea;

	@FXML
	private TextField ffmpegPathLabel;

	@FXML
	private Button startButton, stopButton;

	public static final Logger log = Main.log;

	private ConcatService concat;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ffmpegPathLabel.setText(AppConfig.ACTIVECONFIG.getPathToFFmpeg().toString());
	}

	@FXML
	private void handleAddAction(ActionEvent event) {
		log.log(Level.INFO, "add button clicked - trying to use FileChooser");

		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(getSafeDirectory(AppConfig.ACTIVECONFIG.getLastInputFolder().toFile()));
		chooser.setTitle("Select files to concatenate");

		// TODO: add more extension filters
		chooser.getExtensionFilters().addAll(new ExtensionFilter("all files", "*.*"),
				new ExtensionFilter("mp4 files", "*.mp4"));

		List<File> selectedFiles = chooser.showOpenMultipleDialog(((Control) event.getSource()).getScene().getWindow());

		ArrayList<Path> addedFiles = new ArrayList<Path>();

		if (selectedFiles != null && !selectedFiles.isEmpty()) {
			AppConfig.ACTIVECONFIG.setLastInputFolder(selectedFiles.get(0).toPath().getParent());

			log.log(Level.INFO, "FileChooser returned - " + selectedFiles.size() + " files selected");
			for (File f : selectedFiles) {
				// TODO: match for supported formats
				// add file to complete list
				addedFiles.add(f.toPath());
			}
			fileList.getItems().addAll(addedFiles);
		} else {
			log.log(Level.INFO, "selection canceled");
		}
	}

	@FXML
	private void handleRemoveAction(ActionEvent event) {
		log.log(Level.INFO, "attempting to remove entries from file selection list");

		List<Integer> selection = new ArrayList<Integer>(fileList.getSelectionModel().getSelectedIndices());

		// sort this list, to make sure it is sorted; trivial for sorted list
		selection.sort(Integer::compare);

		for (int i = selection.size() - 1; i >= 0; i--) {
			fileList.getItems().remove((int) selection.get(i));
		}

	}

	@FXML
	private void handleUpAction(ActionEvent event) {
		if (isListCompact(fileList.getSelectionModel().getSelectedIndices())) {
			List<Integer> selection = new ArrayList<Integer>(fileList.getSelectionModel().getSelectedIndices());

			// sort this list, to make sure it is sorted; trivial for sorted
			// list
			selection.sort(Integer::compare);

			int insertAt = selection.get(0) - 1;

			if (insertAt >= 0) {
				List<Path> stash = new ArrayList<Path>();
				for (int i : selection) {
					stash.add(fileList.getItems().get(i));
				}

				fileList.getItems().removeAll(stash);
				fileList.getItems().addAll(insertAt, stash);

				// reselect items
				fileList.getSelectionModel().clearSelection();
				fileList.getSelectionModel().selectRange(selection.get(0) - 1, selection.get(selection.size() - 1));
				fileList.scrollTo(insertAt);
			}
		}
	}

	@FXML
	private void handleDownAction(ActionEvent event) {
		if (isListCompact(fileList.getSelectionModel().getSelectedIndices())) {
			List<Integer> selection = new ArrayList<Integer>(fileList.getSelectionModel().getSelectedIndices());

			// sort this list, to make sure it is sorted; trivial for sorted
			// list
			selection.sort(Integer::compare);

			int insertAt = selection.get(0) + 1;

			if (insertAt <= fileList.getItems().size() - selection.size()) {
				List<Path> stash = new ArrayList<Path>();
				for (int i : selection) {
					stash.add(fileList.getItems().get(i));
				}

				fileList.getItems().removeAll(stash);
				fileList.getItems().addAll(insertAt, stash);

				// reselect items
				fileList.getSelectionModel().clearSelection();
				fileList.getSelectionModel().selectRange(selection.get(0) + 1, selection.get(selection.size() - 1) + 2);
				fileList.scrollTo(insertAt);
			}
		}
	}

	@FXML
	private void handleConcatAction(ActionEvent event) {
		if (fileList.getItems().size() > 1) {
			log.log(Level.INFO, "asking for target file");
			FileChooser targetChooser = new FileChooser();
			targetChooser.setTitle("Specify output file");

			targetChooser.setInitialDirectory(getSafeDirectory(AppConfig.ACTIVECONFIG.getLastOutputFile().getParent().toFile()));
			targetChooser.setInitialFileName(AppConfig.ACTIVECONFIG.getLastOutputFile().getFileName().toString());

			targetChooser.getExtensionFilters().addAll(new ExtensionFilter("all files", "*.*"),
					new ExtensionFilter("mp4 files", "*.mp4"));

			File targetFile = targetChooser.showSaveDialog(((Control) event.getSource()).getScene().getWindow());
			if (targetFile != null) {
				// ensure targetFile has a file extension
				if(!targetFile.getName().endsWith("\\.mp4")){
					targetFile = new File(targetFile.getAbsolutePath() + ".mp4");
				}
				
				if (!targetFile.exists() || targetFile.delete()) {
					// TODO: notify user if deleting failed

					AppConfig.ACTIVECONFIG.setLastOutputFile(targetFile.toPath());

					log.log(Level.INFO, "starting concatenation");
					concat = new ConcatService();
					Path listFile;
					try {
						listFile = concat.createListFile(fileList.getItems());
						concat.startConcatenation(terminalArea, startButton, stopButton, listFile, targetFile.toPath());
					} catch (IOException e) {
						// TODO Auto-generated catch block
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

		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select FFmpeg binary");

		chooser.setInitialDirectory(getSafeDirectory(AppConfig.ACTIVECONFIG.getPathToFFmpeg().getParent().toFile()));
		chooser.setInitialFileName(AppConfig.ACTIVECONFIG.getPathToFFmpeg().getFileName().toString());

		File selectedBinary = chooser.showOpenDialog(((Control) event.getSource()).getScene().getWindow());

		if (selectedBinary != null) {
			AppConfig.ACTIVECONFIG.setPathToFFmpeg(selectedBinary.toPath());
			ffmpegPathLabel.setText(AppConfig.ACTIVECONFIG.getPathToFFmpeg().toString());
		}
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

	@FXML
	private void handleStopAction(ActionEvent event) {
		concat.stopStartedProcess(startButton, stopButton);
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
