package com.bobsystem.exercise.commons.threadpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadKit {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadKit.class);

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
    }

    //region constructors
    private ThreadKit() { }
    //endregion
}
