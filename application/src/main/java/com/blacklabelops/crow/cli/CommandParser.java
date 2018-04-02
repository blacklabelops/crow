package com.blacklabelops.crow.cli;

import java.util.Arrays;
import java.util.Optional;

public class CommandParser {
	
	private final String[] arguments;
	
	private CliCommand command = null;
	
	public CommandParser(String[] args) {
		this.arguments = args;
	}

	public CliCommand getCommand() {
		Optional<String> lastArgument = Arrays.stream(arguments).reduce((first, second) -> second);
		lastArgument.ifPresent(l -> {
			switch (l) {
				case "help": {
					command = CliCommand.HELP;
					break;
				}
				case "version": {
					command = CliCommand.VERSION;
					break;
				}
				case "list": {
					command = CliCommand.LIST;
					break;
				}
			}
		});
		if (command == null) {
			command = CliCommand.HELP;
		}
		return command;
	}
	
	
}
