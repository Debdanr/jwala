package com.cerner.jwala.listener;

import com.cerner.jwala.service.DbServerServiceException;
import com.cerner.jwala.service.impl.H2TcpServerServiceImpl;
import com.cerner.jwala.service.impl.H2WebServerServiceImpl;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A life cycle listener that starts/stops h2 db server
 *
 * Created by JC043760 on 8/28/2016
 */
public class H2LifecycleListener implements LifecycleListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2LifecycleListener.class);

    private H2TcpServerServiceImpl h2TcpServerService;
    private H2WebServerServiceImpl h2WebServerService;

    private String tcpServerParam;
    private String webServerParam;

    @Override
    public void lifecycleEvent(final LifecycleEvent event) {
        final LifecycleState lifecycleState = event.getLifecycle().getState();

        // h2 tcp server
        if (h2TcpServerService == null) {
            h2TcpServerService = new H2TcpServerServiceImpl(tcpServerParam);
        }

        if (LifecycleState.STARTING_PREP.equals(lifecycleState) && !h2TcpServerService.isServerRunning()) {
            LOGGER.info("Initializing H2 tcp server on Tomcat lifecyle: {}", lifecycleState);
            h2TcpServerService.startServer();
        } else if (LifecycleState.DESTROYING.equals(lifecycleState) && h2TcpServerService.isServerRunning()) {
            LOGGER.info("Destroying H2 tcp server on Tomcat lifecyle: {}", lifecycleState);
            h2TcpServerService.stopServer();
        }

        // h2 web server
        try {
            if (h2WebServerService == null) {
                h2WebServerService = new H2WebServerServiceImpl(webServerParam);
            }

            if (LifecycleState.STARTING_PREP.equals(lifecycleState) && !h2WebServerService.isServerRunning()) {
                LOGGER.info("Initializing H2 web server on Tomcat lifecyle: {}", lifecycleState);
                h2WebServerService.startServer();
            } else if (LifecycleState.DESTROYING.equals(lifecycleState) && h2WebServerService.isServerRunning()) {
                LOGGER.info("Destroying H2 web server on Tomcat lifecyle: {}", lifecycleState);
                h2WebServerService.stopServer();
            }
        } catch (final DbServerServiceException e) {
            LOGGER.error("Failed to start H2 Web Server! Continuing without it.", e);
        }
    }

    public void setTcpServerParam(final String tcpServerParam) {
        this.tcpServerParam = tcpServerParam;
    }

    public void setWebServerParam(final String webServerParam) {
        this.webServerParam = webServerParam;
    }
}
