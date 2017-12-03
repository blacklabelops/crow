package com.blacklabelops.crow.config;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by steffenbleul on 29.12.16.
 */
public class UniqueJobNamesValidator implements ConstraintValidator<UniqueJobNames, List<Job>> {

    public UniqueJobNamesValidator() {
        super();
    }

    @Override
    public boolean isValid(List<Job> jobs, ConstraintValidatorContext constraintValidatorContext) {
        boolean isValid = true;
        if (jobs != null && !jobs.isEmpty()) {
            isValid = validateUniqueJobNames(jobs);
        }
        return isValid;
    }

    private boolean validateUniqueJobNames(List<Job> environments) {
        Set<String> envKeys = new HashSet<>();
        environments.stream().forEach(s -> envKeys.add(s.getName()));
        return environments.size() == envKeys.size();
    }

    @Override
    public void initialize(UniqueJobNames uniqueJobNamesValidator) {

    }
}
