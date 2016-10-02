package application.config;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import org.apache.commons.lang3.SystemUtils;

import application.main.Main;

/**
 * An AppConfig holds information about user activity that should be persisted
 * between sessions. These information can be loaded and saved in JSON files, so
 * the content has to be serializable.
 * 
 * @author joachim
 *
 */
public class AppConfig {

	public static AppConfig ACTIVECONFIG;

	static {
		try {
			ACTIVECONFIG = ConfigAccessor.readFromDefault();
		} catch (IOException e) {
			Main.log.log(Level.SEVERE, "config file could not be read - empty config is used", e);
			ACTIVECONFIG = new AppConfig();
		}
	}

	private Path pathToFFmpeg;

	private Path lastInputFolder;

	private Path lastOutputFile;

	public AppConfig() {
		pathToFFmpeg = Paths.get("./ffmpeg/");
		lastInputFolder = Paths.get(".");
		lastOutputFile = Paths.get("./output.mp4");
	}

	public AppConfig(Path pathToFFmpeg, Path lastInputFolder, Path lastOutputFile) {
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
	
	public String getFFmpegExecutable(){
		if(SystemUtils.IS_OS_WINDOWS){
			return ACTIVECONFIG.pathToFFmpeg + "/ffmpeg.exe";
		} else {
			return ACTIVECONFIG.pathToFFmpeg + "/ffmpeg";
		}
	}
	
	public String getFFprobeExecutable(){
		if(SystemUtils.IS_OS_WINDOWS){
			return ACTIVECONFIG.pathToFFmpeg + "/ffprobe.exe";
		} else {
			return ACTIVECONFIG.pathToFFmpeg + "/ffprobe";
		}
	}

}
