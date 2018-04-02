package com.blacklabelops.crow.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.client.RestTemplate;

import com.blacklabelops.crow.rest.JobInformation;

public class CrowCli {
	
	private static final Logger log = LoggerFactory.getLogger(CrowCli.class);

    public static void main(String args[]) {
    		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    		context.register(CliConfig.class);
    		context.refresh();
    		CommandLineConfiguration config = context.getBean("cliConfiguration",CommandLineConfiguration.class);
    		log.debug("Configuration base URL: {}", config.getBaseUrl());
    		CommandParser parser = new CommandParser(args);
    		CliCommand command = parser.getCommand();
    		switch (command) {
	    		case LIST: {
	    			RestTemplate restTemplate = new RestTemplate();
				JobInformation[] jobs = restTemplate.getForObject(config.getBaseUrl() + "/crow/jobs", JobInformation[].class);
	    	        PrintConsole consolePrinter = new PrintConsole();
	    	        consolePrinter.printJobs(jobs);
	    			break;
	    		}
	    		case VERSION: {
	    			PrintConsole consolePrinter = new PrintConsole();
	    			consolePrinter.printVersion();
	    			break;
	    		}
	    		case HELP: {
	    			PrintConsole consolePrinter = new PrintConsole();
	    			consolePrinter.printHelp();
	    			break;
	    		}
    		}
    		context.close();
    }
}
