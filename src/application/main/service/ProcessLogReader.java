package application.main.service;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class ProcessLogReader extends Thread {

	private static final int PROCESSLOGSIZE = 2000;

	private Thread wrapperThread;

	private TextArea terminalArea;
	private Button startButton;
	private Button stopButton;

	public ProcessLogReader(Thread wrapperThread, TextArea terminalArea, Button startButton, Button stopButton) {
		this.wrapperThread = wrapperThread;
		this.terminalArea = terminalArea;
		this.startButton = startButton;
		this.stopButton = stopButton;
	}

	@Override
	public void run() {
		while(wrapperThread.getState() == State.NEW || wrapperThread.isAlive()){
			// TODO: do stuff; read output
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// nothing to do
			}
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
	// TODO: does not work for new approach; rewrite
//	private void readProcessOutput(TextArea terminalArea) throws IOException {
//		int toRead = wrapperThread.getErrorStream().available();
//		if (toRead > 0) {
//			byte[] readData = new byte[PROCESSLOGSIZE];
//			wrapperThread.getErrorStream().read(readData);
//			StringBuilder builder = new StringBuilder();
//			for (byte b : readData) {
//				builder.append((char) b);
//			}
//			builder.append("\n");
//			terminalArea.appendText(builder.toString());
//		}
//	}

}
