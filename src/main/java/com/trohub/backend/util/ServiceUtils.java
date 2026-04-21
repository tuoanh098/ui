package com.trohub.backend.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Utility to standardize try/catch behavior in service methods.
 */
public final class ServiceUtils {

    private static final Logger logger = LoggerFactory.getLogger(ServiceUtils.class);

    private ServiceUtils() {}

    /**
     * Execute a callable and convert unexpected exceptions to RuntimeException after logging.
     * Known business exceptions should still be thrown by the callable.
     */
    public static <T> T exec(Callable<T> callable, String actionDescription) {
        try {
            return callable.call();
        } catch (RuntimeException re) {
            // business exceptions - rethrow to be handled by GlobalExceptionHandler
            throw re;
        } catch (Exception ex) {
            logger.error("Unexpected error while {}: {}", actionDescription, ex.getMessage(), ex);
            throw new RuntimeException("Unexpected error while " + actionDescription + ": " + ex.getMessage(), ex);
        }
    }
}

