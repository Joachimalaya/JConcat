package application.config;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.json.Json;
import javax.json.JsonObject;

public abstract class ConfigAccessor {

	private static final Path DEFAULTCONFIGFILE = Paths
			.get("./jconcatConfig.json");

	public static void writeToDefault(AppConfig config) throws IOException {
		JsonObject jsonObject = Json.createObjectBuilder()
				.add("pathToFFmpeg", config.getPathToFFmpeg().toString())
				.add("lastInputFolder", config.getLastInputFolder().toString())
				.add("lastOutputFile", config.getLastOutputFile().toString())
				.build();

		try (FileWriter configWriter = new FileWriter(
				DEFAULTCONFIGFILE.toFile())) {
			Json.createWriter(configWriter).write(jsonObject);
		}
	}

	public static AppConfig readFromDefault() throws IOException {
		try (FileReader configReader = new FileReader(
				DEFAULTCONFIGFILE.toFile())) {
			JsonObject jsonObject = Json.createReader(configReader)
					.readObject();
			String pathToFFmpeg = jsonObject.getString("pathToFFmpeg");
			String lastInputFolder = jsonObject.getString("lastInputFolder");
			String lastOutputFile = jsonObject.getString("lastOutputFile");

			return new AppConfig(Paths.get(pathToFFmpeg),
					Paths.get(lastInputFolder), Paths.get(lastOutputFile));
		}

	}
}
