package application.main.service;

import java.io.IOException;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class ProcessLogReader extends Thread {

	private static final int PROCESSLOGSIZE = 2000;

	private Process process;

	private TextArea terminalArea;
	private Button startButton;
	private Button stopButton;

	public ProcessLogReader(Process process, TextArea terminalArea, Button startButton, Button stopButton) {
		this.process = process;
		this.terminalArea = terminalArea;
		this.startButton = startButton;
		this.stopButton = stopButton;
	}

	@Override
	public void run() {
		try {
			while (process.isAlive()) {
				readProcessOutput(terminalArea);
			}
			readProcessOutput(terminalArea);
		} catch (IOException e) {
			// TODO: handle exception
		}
		terminalArea.appendText("\nfinished concatenation");
		startButton.setDisable(false);
		stopButton.setDisable(true);
	}

	/**
	 * Relay output from process to given TextArea. This implementation is not
	 * safe to grab all output, as the buffer is limited to PROCESSLOGSIZE and
	 * as such miss characters, when output from the process is larger.
	 * 
	 * @param terminalArea
	 * @throws IOException
	 */
	private void readProcessOutput(TextArea terminalArea) throws IOException {
		int toRead = process.getErrorStream().available();
		if (toRead > 0) {
			byte[] readData = new byte[PROCESSLOGSIZE];
			process.getErrorStream().read(readData);
			StringBuilder builder = new StringBuilder();
			for (byte b : readData) {
				builder.append((char) b);
			}
			builder.append("\n");
			terminalArea.appendText(builder.toString());
		}
	}

}
