package application.main.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import application.config.AppConfig;

public class ConcatService {
	
	private Process process;
	
	public void stopStartedProcess(Button startButton, Button stopButton){
		if(process != null && process.isAlive()){
			process.destroy();
			startButton.setDisable(false);
			stopButton.setDisable(true);
		}
	}

	public void startConcatenation(TextArea terminalArea, Button startButton, Button stopButton, Path fileList, Path targetFile){
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
			
			Thread readThread = new Thread() {
				@Override
				public void run() {
					try {
						while (process.isAlive()) {
							int toRead = process.getErrorStream().available();
							if (toRead > 0) {
								byte[] readData = new byte[2000];
								process.getErrorStream().read(readData);
								StringBuilder builder = new StringBuilder();
								for (byte b : readData) {
									builder.append((char) b);
								}
								builder.append("\n");
								terminalArea.appendText(builder.toString());
							}
						}
						int toRead = process.getErrorStream().available();
						if (toRead > 0) {
							byte[] readData = new byte[2000];
							process.getErrorStream().read(readData);
							StringBuilder builder = new StringBuilder();
							for (byte b : readData) {
								builder.append((char) b);
							}
							builder.append("\n");
							terminalArea.appendText(builder.toString());
						}
					} catch (IOException e) {
						// TODO: handle exception
					}
					terminalArea.appendText("\nfinished concatenation");
					startButton.setDisable(false);
					stopButton.setDisable(true);
				}
			};
			readThread.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Path createListFile(List<Path> files) throws IOException{
		File listFile = File.createTempFile("fileList", ".txt");
		listFile.deleteOnExit();
		
		StringBuilder contentBuilder = new StringBuilder();
		for(Path p : files){
			contentBuilder.append("file '");
			contentBuilder.append(p.toString());
			contentBuilder.append("'\n");
		}

		try(FileWriter writer = new FileWriter(listFile)){
			String content = contentBuilder.toString();
			writer.write(content);
		}
		
		return listFile.toPath();
	}
}
