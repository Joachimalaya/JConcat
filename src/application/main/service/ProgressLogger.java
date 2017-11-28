package application.main.service;

import java.time.Duration;

import javafx.scene.control.TextArea;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;

public class ProgressLogger implements ProgressListener {

	private TextArea terminalArea;

	public ProgressLogger(TextArea terminalArea) {
		this.terminalArea = terminalArea;
	}

	@Override
	public void progress(Progress progress) {
		Duration duration = Duration.ofNanos(progress.out_time_ns);
		terminalArea.appendText(String.format("\noutput time: %02d:%02d:%02d.%02d", duration.toHours(),
				duration.toMinutes(), duration.getSeconds() % 60, duration.toMillis() % 100));

		if (progress.isEnd()) {
			terminalArea.appendText("concatenation done");
		}
	}

}
