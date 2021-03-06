package com.blacklabelops.crow.console.reporter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.console.executor.ExecutionResult;

public class ConsoleReporter implements IJobReporter {

	private Logger logger = LoggerFactory.getLogger(ConsoleReporter.class);

	private String startMessageFormat = "Starting job %s at %s.";

	private String finishMessageFormat = "Finished job %s with return code %d at %s.";

	private String failingMessageFormat = "Failed executing job %s at %s.";

	private DateTimeFormatter formatter = DateTimeFormatter
			.ofPattern("uuuu-MM-dd HH:mm")
			.withLocale(Locale.getDefault())
			.withZone(ZoneId.systemDefault());

	public ConsoleReporter() {
		super();
	}

	@Override
	public void startingJob(ExecutionResult pExecutingJob) {
		String time = formatter.format(pExecutingJob.getStartingTime().get());
		String message = String.format(startMessageFormat, pExecutingJob.getJobDefinition().getJobLabel(),
				time);
		logger.info(message);
	}

	@Override
	public void finishedJob(ExecutionResult pExecutingJob) {
		String time = formatter.format(pExecutingJob.getFinishingTime().get());
		String message = String.format(finishMessageFormat, pExecutingJob.getJobDefinition().getJobLabel(),
				pExecutingJob
						.getReturnCode().orElse(null),
				time);
		logger.info(message);
	}

	@Override
	public void failingJob(ExecutionResult executingJob) {
		String time = formatter.format(LocalDateTime.now());
		String message = String.format(failingMessageFormat, executingJob.getJobDefinition().getJobLabel(), time);
		logger.info(message);
	}
}
