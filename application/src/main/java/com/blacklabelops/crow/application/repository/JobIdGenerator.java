package com.blacklabelops.crow.application.repository;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blacklabelops.crow.console.definition.JobId;

class JobIdGenerator {

	private static Logger LOG = LoggerFactory.getLogger(JobIdGenerator.class);

	public JobIdGenerator() {
		super();
	}

	public JobId generate() {
		String id = UUID.randomUUID().toString().replaceAll("[-]", "");
		LOG.debug("Generated Id: {}", id);
		return JobId.of(id);
	}
}
