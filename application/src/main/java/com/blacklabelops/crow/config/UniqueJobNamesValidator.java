package com.blacklabelops.crow.config;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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

    private boolean validateUniqueJobNames(List<JobConfiguration> environments) {
        Set<String> envKeys = new HashSet<>();
        environments.stream().forEach(s -> envKeys.add(s.getName()));
        return environments.size() == envKeys.size();
    }

    @Override
    public void initialize(UniqueJobNames uniqueJobNamesValidator) {

    }
}
