package application.main.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import application.config.AppConfig;
import application.main.MainController;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * The ConcatService handles the call to FFmpeg via command line and relays
 * console output to the UI.
 * 
 * @author joachim
 *
 */
public class ConcatService {

	private Process process;

	/**
	 * A started concatenation process may be cancelled by calling this method.
	 * 
	 * @param startButton
	 * @param stopButton
	 */
	public void stopStartedProcess(Button startButton, Button stopButton) {
		if (process != null && process.isAlive()) {
			process.destroy();
			startButton.setDisable(false);
			stopButton.setDisable(true);
		}
	}
	
	public File askForOutputFile(ActionEvent event){
	FileChooser targetChooser = new FileChooser();
	targetChooser.setTitle("Specify output file");

	targetChooser.setInitialDirectory(MainController.getSafeDirectory(AppConfig.ACTIVECONFIG.getLastOutputFile().getParent().toFile()));
	targetChooser.setInitialFileName(AppConfig.ACTIVECONFIG.getLastOutputFile().getFileName().toString());

	targetChooser.getExtensionFilters().addAll(new ExtensionFilter("all files", "*.*"),
			new ExtensionFilter("mp4 files", "*.mp4"));

	File targetFile = targetChooser.showSaveDialog(((Control) event.getSource()).getScene().getWindow());
	
	return targetFile;
	}

	/**
	 * Calling this method will cause a call to the configured FFmpeg location.
	 * Arguments to FFmpeg are the location of the temporary fileList file and
	 * the output target.
	 * 
	 * Outputs from FFmpeg will be displayed in the given terminalArea.
	 * 
	 * @param terminalArea
	 * @param startButton
	 * @param stopButton
	 * @param fileList
	 * @param targetFile
	 */
	public void startConcatenation(TextArea terminalArea, Button startButton, Button stopButton, Path fileList,
			Path targetFile) {
		StringBuilder commandBuilder = new StringBuilder();
		commandBuilder.append(AppConfig.ACTIVECONFIG.getPathToFFmpeg().toString());
		commandBuilder.append(" -f concat -i ");
		commandBuilder.append(fileList.toString());
		commandBuilder.append(" -c copy ");
		commandBuilder.append(targetFile.toString());

		try {
			terminalArea.clear();
			process = Runtime.getRuntime().exec(commandBuilder.toString());

			startButton.setDisable(true);
			stopButton.setDisable(false);

			Thread readThread = new ProcessLogReader(process, terminalArea, startButton, stopButton);
			readThread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the temporary file, that is read by FFmpeg to determine input
	 * files.
	 * 
	 * @param files
	 * @return
	 * @throws IOException
	 */
	public Path createListFile(List<Path> files) throws IOException {
		File listFile = File.createTempFile("fileList", ".txt");
		listFile.deleteOnExit();

		StringBuilder contentBuilder = new StringBuilder();
		for (Path p : files) {
			contentBuilder.append("file '");
			contentBuilder.append(p.toString());
			contentBuilder.append("'\n");
		}

		try (FileWriter writer = new FileWriter(listFile)) {
			String content = contentBuilder.toString();
			writer.write(content);
		}

		return listFile.toPath();
	}
}
