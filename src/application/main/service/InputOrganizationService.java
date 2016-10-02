package application.main.service;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import application.config.AppConfig;
import application.main.Main;
import application.main.MainController;
import javafx.event.ActionEvent;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class InputOrganizationService {

	public static final Logger log = Main.log;

	/**
	 * Shows a file choosing dialog to the user and add selected files to the
	 * given fileList.
	 * 
	 * @param fileList
	 * @param event
	 */
	public void addEntries(ListView<Path> fileList, ActionEvent event) {
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(
				MainController.getSafeDirectory(AppConfig.ACTIVECONFIG.getLastInputFolder().toFile()));
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

	/**
	 * Remove selected entries from the fileList. To remove multiple entries at
	 * once, they have to be compact.
	 * 
	 * @param fileList
	 */
	public void deleteEntries(ListView<Path> fileList) {
		List<Integer> selection = new ArrayList<Integer>(fileList.getSelectionModel().getSelectedIndices());

		// sort this list, to make sure it is sorted; trivial for sorted list
		selection.sort(Integer::compare);

		for (int i = selection.size() - 1; i >= 0; i--) {
			fileList.getItems().remove((int) selection.get(i));
		}
	}

	/**
	 * Move selected entries up the fileList.
	 * 
	 * @param fileList
	 */
	public void moveEntriesUp(ListView<Path> fileList) {
		if (MainController.isListCompact(fileList.getSelectionModel().getSelectedIndices())) {
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

	/**
	 * Move selected entries down the fileList.
	 * 
	 * @param fileList
	 */
	public void moveEntriesDown(ListView<Path> fileList) {
		if (MainController.isListCompact(fileList.getSelectionModel().getSelectedIndices())) {
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

	/**
	 * Open a file choosing dialog where the user can select the location of of
	 * the FFmpeg binary.
	 * 
	 * @param ffmpegPath
	 * @param event
	 */
	public void setFFmpegPath(TextField ffmpegPath, ActionEvent event) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Select FFmpeg binary directory");

		chooser.setInitialDirectory(
				MainController.getSafeDirectory(AppConfig.ACTIVECONFIG.getPathToFFmpeg().getParent().toFile()));

		File selectedBinary = chooser.showDialog(((Control) event.getSource()).getScene().getWindow());

		if (selectedBinary != null) {
			AppConfig.ACTIVECONFIG.setPathToFFmpeg(selectedBinary.toPath());
			ffmpegPath.setText(AppConfig.ACTIVECONFIG.getPathToFFmpeg().toString());
		}
	}

}
