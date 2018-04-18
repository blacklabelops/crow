package com.blacklabelops.crow.application.cli;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.blacklabelops.crow.application.CrowDemon;
import com.blacklabelops.crow.application.cli.CrowCli;
import com.blacklabelops.crow.application.cli.PrintConsole;
import com.blacklabelops.crow.application.rest.JobInformation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = CrowDemon.class)
@TestPropertySource(locations = "classpath:tryOut.properties")
public class CrowCliTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@LocalServerPort
	int randomServerPort;

	@Before
	public void setup() {
		System.setProperty("crow.server.baseUrl", "http://localhost:" + randomServerPort);
	}

	@Test
	public void testMain_ExecuteHelp() {
		CrowCli.main(new String[] { "help" });
	}

	@Test
	public void testMain_ExecuteVersion() {
		CrowCli.main(new String[] { "version" });
	}

	@Test
	public void testMain_ExecuteList() {
		CrowCli.main(new String[] { "list" });
	}

	@Test
	public void testMain_PrintList() {
		JobInformation[] jobs = this.restTemplate.getForObject("/crow/jobs", JobInformation[].class);
		PrintConsole console = new PrintConsole();
		console.printJobs(jobs);
	}

	@Test
	@Ignore
	public void testMain_PrintJsonList() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String jobs = this.restTemplate.getForObject("/crow/jobs", String.class);
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(jobs);
		String prettyJsonString = gson.toJson(je);
		System.out.println(prettyJsonString);
	}
}
