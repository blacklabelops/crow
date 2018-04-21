package com.blacklabelops.crow.application.model;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;


public class CronValidator implements ConstraintValidator<Cron, String> {

    public static final Logger LOG = LoggerFactory.getLogger(CronValidator.class);

    public CronValidator() {
        super();
    }

    @Override
    public void initialize(Cron cron) {

    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        boolean isValid = false;
        if (s != null) {
            isValid = isValidCron(s);
        }
        return isValid;
    }

    private boolean isValidCron(String s) {
        boolean isValid = false;
        CronDefinition cronD =
                CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronD);
        com.cronutils.model.Cron cron = null;
        try {
            cron = parser.parse(s);
        } catch (IllegalArgumentException e) {
            LOG.debug("Cron parsing failed!",e);
        }
        if (cron != null) {
            isValid = true;
        }
        return isValid;
    }
}
