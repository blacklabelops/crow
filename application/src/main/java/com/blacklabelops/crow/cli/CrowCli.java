package com.blacklabelops.crow.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.client.RestTemplate;

import com.blacklabelops.crow.rest.JobInformation;
import com.blacklabelops.crow.rest.Version;

public class CrowCli {
	
	private static final Logger log = LoggerFactory.getLogger(CrowCli.class);

    public static void main(String args[]) {
    		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    		context.register(CliConfig.class);
    		context.refresh();
    		CommandLineConfiguration config = context.getBean("cliConfiguration",CommandLineConfiguration.class);
    		log.debug("Configuration base URL: {}", config.evaluateBaseURL());
    		CommandParser parser = new CommandParser(args);
    		CliCommand command = parser.getCommand();
    		switch (command) {
	    		case LIST: {
	    			RestTemplate restTemplate = new RestTemplate();
				JobInformation[] jobs = restTemplate.getForObject(config.evaluateBaseURL() + "/crow/jobs", JobInformation[].class);
	    	        PrintConsole consolePrinter = new PrintConsole();
	    	        consolePrinter.printJobs(jobs);
	    			break;
	    		}
	    		case VERSION: {
	    			RestTemplate restTemplate = new RestTemplate();
	    			Version version = restTemplate.getForObject(config.evaluateBaseURL() + "/crow/version", Version.class);
	    			PrintConsole consolePrinter = new PrintConsole();
	    			consolePrinter.printVersion(version.getVersion());
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
