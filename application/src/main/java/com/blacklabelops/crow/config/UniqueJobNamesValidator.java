package com.blacklabelops.crow.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueJobNamesValidator implements ConstraintValidator<UniqueJobNames, List<JobConfiguration>> {

	public UniqueJobNamesValidator() {
		super();
	}

	@Override
	public boolean isValid(List<JobConfiguration> jobs, ConstraintValidatorContext constraintValidatorContext) {
		boolean isValid = true;
		if (jobs != null && !jobs.isEmpty()) {
			isValid = validateUniqueJobNames(jobs);
		}
		return isValid;
	}

	private boolean validateUniqueJobNames(List<JobConfiguration> jobs) {
		Set<String> uniqueJobIds = new HashSet<>();
		jobs.stream().forEach(s -> uniqueJobIds.add(s.resolveJobId()));
		return jobs.size() == uniqueJobIds.size();
	}

	@Override
	public void initialize(UniqueJobNames uniqueJobNamesValidator) {

	}
}
