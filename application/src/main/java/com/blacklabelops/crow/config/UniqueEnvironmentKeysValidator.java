package com.blacklabelops.crow.config;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by steffenbleul on 29.12.16.
 */
public class UniqueEnvironmentKeysValidator implements ConstraintValidator<UniqueEnvironmentKeys, List<Environment>> {

    public UniqueEnvironmentKeysValidator() {
        super();
    }

    @Override
    public void initialize(UniqueEnvironmentKeys uniqueEnvironmentKeys) {

    }

    @Override
    public boolean isValid(List<Environment> environments, ConstraintValidatorContext constraintValidatorContext) {
        boolean isValid = true;
        if (environments != null && !environments.isEmpty()) {
            isValid = validateUniqueEnvironmentVariableKeys(environments);
        }
        return isValid;
    }

    private boolean validateUniqueEnvironmentVariableKeys(List<Environment> environments) {
        Set<String> envKeys = new HashSet<>();
        environments.stream().forEach(s -> envKeys.add(s.getKey()));
        return environments.size() == envKeys.size();
    }
}
