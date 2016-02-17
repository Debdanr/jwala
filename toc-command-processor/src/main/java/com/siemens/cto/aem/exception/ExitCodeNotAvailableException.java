package com.siemens.cto.aem.exception;

/**
 * Exception wrapper when an executed command does not return an exit code.
 *
 * Created by JC043760 on 2/17/2016.
 */
public class ExitCodeNotAvailableException extends RuntimeException {

    public ExitCodeNotAvailableException(final String commandStr) {
        super("Exit code not available for command: " + commandStr);
    }

}
