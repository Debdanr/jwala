package com.cerner.jwala.tomcat.listener.messaging;

/**
 * Wrapper for {@link MessagingService} exceptions
 *
 * Created by JC043760 on 8/15/2016
 */
public class MessagingServiceException extends RuntimeException {

    public MessagingServiceException(final String msg, final Throwable t) {
        super(msg, t);
    }
}
