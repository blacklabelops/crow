package com.blacklabelops.crow.config;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.internal.util.Version;

import javax.validation.constraints.NotNull;

/**
 * Created by steffenbleul on 28.12.16.
 */
public class Environment implements IConfigModel {

    @NotEmpty(message = "Each environment variable must have at least a key!")
    private String key;

    private String value;

    public Environment() {
        super();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
