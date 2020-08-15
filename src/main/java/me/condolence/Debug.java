package me.condolence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Debug {
    private static final Logger logger = LogManager.getLogger("Mention");

    public static void log(String message) {
        logger.info("[Debug] [MENTION_ADDON] " + message);
    }
}
