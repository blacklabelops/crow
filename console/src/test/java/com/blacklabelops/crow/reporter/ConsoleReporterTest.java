package com.blacklabelops.crow.reporter;

import java.time.LocalDateTime;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.blacklabelops.crow.definition.JobDefinition;
import com.blacklabelops.crow.executor.ExecutionResult;
import com.blacklabelops.crow.executor.IExecutor;

public class ConsoleReporterTest {

    public ConsoleReporter reporter = new ConsoleReporter();

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    IExecutor executor;

    @Test
    public void testStartingJob() {
    		JobDefinition jobDefinition = new JobDefinition();
    		jobDefinition.setJobName("Name");
    		ExecutionResult result = new ExecutionResult(jobDefinition);
    		result.setStartingTime(LocalDateTime.now());
        
        reporter.startingJob(result);
    }

    @Test
    public void finishedJob() {
    		JobDefinition jobDefinition = new JobDefinition();
		jobDefinition.setJobName("Name");
		ExecutionResult result = new ExecutionResult(jobDefinition);
		result.setFinishingTime(LocalDateTime.now());
		result.setReturnCode(Integer.valueOf(5));
		
        reporter.finishedJob(result);
    }


}