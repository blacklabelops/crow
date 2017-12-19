package com.blacklabelops.crow.config;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by steffenbleul on 29.12.16.
 */
public class UniqueEnvironmentKeysValidator implements ConstraintValidator<UniqueEnvironmentKeys, Map<String, String>> {

    public UniqueEnvironmentKeysValidator() {
        super();
    }

    @Override
    public void initialize(UniqueEnvironmentKeys uniqueEnvironmentKeys) {

    }

    @Override
    public boolean isValid(Map<String, String> environments, ConstraintValidatorContext constraintValidatorContext) {
        boolean isValid = true;
        if (environments != null && !environments.isEmpty()) {
            isValid = validateUniqueEnvironmentVariableKeys(environments);
        }
        return isValid;
    }

    private boolean validateUniqueEnvironmentVariableKeys(Map<String, String> environments) {
        Set<String> envKeys = new HashSet<>();
        environments.keySet().stream().forEach(s -> envKeys.add(s));
        return environments.size() == envKeys.size();
    }
}
