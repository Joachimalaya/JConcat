package application.config;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import application.main.Main;

public class AppConfig {

	public static AppConfig ACTIVECONFIG;

	static {
		try {
			ACTIVECONFIG = ConfigAccessor.readFromDefault();
		} catch (IOException e) {
			Main.log.log(Level.SEVERE,
					"config file could not be read - empty config is used", e);
			ACTIVECONFIG = new AppConfig();
		}
	}

	private Path pathToFFmpeg;

	private Path lastInputFolder;

	private Path lastOutputFile;

	public AppConfig() {
		pathToFFmpeg = Paths.get("./ffmpeg.exe");
		lastInputFolder = Paths.get(".");
		lastOutputFile = Paths.get("./output.mp4");
	}

	public AppConfig(Path pathToFFmpeg, Path lastInputFolder,
			Path lastOutputFile) {
		this.pathToFFmpeg = pathToFFmpeg;
		this.lastInputFolder = lastInputFolder;
		this.lastOutputFile = lastOutputFile;
	}

	public Path getPathToFFmpeg() {
		return pathToFFmpeg;
	}

	public void setPathToFFmpeg(Path pathToFFmpeg) {
		this.pathToFFmpeg = pathToFFmpeg;
	}

	public Path getLastInputFolder() {
		return lastInputFolder;
	}

	public void setLastInputFolder(Path lastInputFolder) {
		this.lastInputFolder = lastInputFolder;
	}

	public Path getLastOutputFile() {
		return lastOutputFile;
	}

	public void setLastOutputFile(Path lastOutputFile) {
		this.lastOutputFile = lastOutputFile;
	}
	
}
