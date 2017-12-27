package com.blacklabelops.crow.logger;

import org.slf4j.Logger;

import java.util.function.Consumer;


public class LogErrorConsumer implements Consumer<String> {

    private Logger logger;

    public LogErrorConsumer(Logger pLogger) {
        super();
        logger = pLogger;
    }

    @Override
    public void accept(String s) {
        logger.error(s);
    }
}
