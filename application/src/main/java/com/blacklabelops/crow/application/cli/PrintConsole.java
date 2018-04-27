package com.blacklabelops.crow.application.cli;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.blacklabelops.crow.application.rest.JobInformation;
import com.blacklabelops.crow.application.util.VersionAccessor;
import com.cronutils.utils.StringUtils;

public class PrintConsole {

	public void printJobs(JobInformation[] jobs) {
		String columnFormat = "%1$-30s%2$-15s%3$-25s%4$-12s%5$-12s";
		String header = String.format(columnFormat, "NAME", "CRON", "NEXT", "EXECUTION", "ON ERROR");
		System.out.println(header);
		for (JobInformation job : jobs) {
			String nextExecution = new SimpleDateFormat("yyyy/MM/dd HH:mm z").format(job.getNextExecution());
			String row = String.format(columnFormat,
					prune(job.getName(), 30),
					prune(job.getCron(), 15),
					prune(nextExecution, 25),
					prune(job.getExecution(), 12),
					prune(job.getErrorMode(), 12));
			System.out.println(row);
		}
	}

	private Object prune(String name, int i) {
		String pruned = "";
		if (!StringUtils.isEmpty(name)) {
			pruned = name.substring(0, Math.min(name.length(), i));
		}
		return pruned;
	}

	public void printHelp() {
		System.out.println();
		for (String row : createHelp()) {
			System.out.println(row);
		}
	}

	private String[] createHelp() {
		List<String> help = new ArrayList<>();
		String commandFormat = "%-20s%-30s";
		help.add("usage: crow [command]");
		help.add("");
		help.add(String.format(commandFormat, "list", "List all jobs."));
		help.add(String.format(commandFormat, "version", "Print client and server versions."));
		help.add(String.format(commandFormat, "help", "Print help."));
		return help.toArray(new String[help.size()]);
	}

	public void printVersion(String serverVersion) {
		System.out.println("Client Version: " + new VersionAccessor().getVersion());
		System.out.println("Server Version: " + serverVersion);
	}

}
