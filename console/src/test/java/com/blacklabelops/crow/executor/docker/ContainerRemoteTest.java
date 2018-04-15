package com.blacklabelops.crow.executor.docker;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.blacklabelops.crow.definition.JobDefinition;
import com.blacklabelops.crow.util.FileAsserter;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;

public class ContainerRemoteTest {
	
	public RemoteContainer cli;

    public JobDefinition definition;
	
	public static DockerTestContainerFactory containerFactory;
	
	public static DockerClient dockerClient;
	
	@Rule
    public FileAsserter outputFile = new FileAsserter();

    @Rule
    public FileAsserter errorFile = new FileAsserter();
	
    @Before
    public void setup() {
        assert !System.getProperty("os.name").startsWith("Windows");
        cli = new RemoteContainer();
        cli.setOutputFile(outputFile.getFile());
        cli.setOutputErrorFile(errorFile.getFile());
    }
    
	@BeforeClass
	public static void setupClass() throws InterruptedException, IOException {
		dockerClient = DockerClientBuilder.getInstance().build();
		containerFactory = new DockerTestContainerFactory(dockerClient);
	}
	
	@AfterClass
	public static void tearDownClass() {
		containerFactory.deleteContainers();
	}
	
	@Test
	public void testRun_ExecuteCommandInContainer_ResultInFile() throws IOException, InterruptedException {
		String containerId = containerFactory.runContainer();
		JobDefinition jobDefinition = new JobDefinition();
		jobDefinition.setJobName("A");
		jobDefinition.setCommand("echo","HelloWorld");
		jobDefinition.setContainerName(containerId);
		
		cli.execute(jobDefinition);
		
		outputFile.assertContainsLine("HelloWorld");
	}
	
	@Test
	public void testRun_ExecuteErrorCommandInContainer_ResultInErrorFile() throws IOException, InterruptedException {
		String containerId = containerFactory.runContainer();
		JobDefinition jobDefinition = new JobDefinition();
		jobDefinition.setJobName("A");
		jobDefinition.setCommand("sh","-c",">&2 echo error");
		jobDefinition.setContainerName(containerId);
		
		cli.execute(jobDefinition);
		
		errorFile.assertContainsLine("error");
	}
	
	@Test
	public void testRun_ExecuteAllCommands_AllOutputsInFile() throws IOException, InterruptedException {
		String containerId = containerFactory.runContainer();
		JobDefinition jobDefinition = new JobDefinition();
		jobDefinition.setJobName("A");
		jobDefinition.setCommand("echo","command");
		jobDefinition.setPreCommand("echo","preCommand");
		jobDefinition.setPostCommand("echo","postCommand");
		jobDefinition.setContainerName(containerId);
		
		cli.execute(jobDefinition);
		
		outputFile.assertContainsLine("command");
		outputFile.assertContainsLine("preCommand");
		outputFile.assertContainsLine("postCommand");
	}
	
	@Test(timeout=70000)
	@Ignore
	public void testRun_CommandTakesLongerThanTimeOut_JobTimedOut() throws IOException, InterruptedException {
		String containerId = containerFactory.runContainer();
		JobDefinition jobDefinition = new JobDefinition();
		jobDefinition.setJobName("A");
		jobDefinition.setCommand("sleep","65000");
		jobDefinition.setTimeoutMinutes(1);
		jobDefinition.setContainerName(containerId);
		
		cli.execute(jobDefinition);
		
		assertTrue(cli.isTimedOut());
	}
}
