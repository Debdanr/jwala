package com.cerner.jwala.service.impl;

import com.cerner.jwala.service.DbServerServiceException;
import org.h2.tools.Server;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created on 10/10/2016.
 */
public class H2TcpServerServiceImplTest {
    private H2TcpServerServiceImpl service;

    @Before
    public void setUp() {
        service = new H2TcpServerServiceImpl(null);
    }

    @Test
    public void testCreateServer() {
        String[] params = new String[0];
        Server result = service.createServer(params);
        assertEquals("default port", 9092, result.getPort());
        assertEquals("default status", "Not started", result.getStatus());
        // default URL is created using the IP address of the running machine so just test for not null
        assertNotNull("default URL", result.getURL());
        assertNotNull("default service", result.getService());
    }

    @Test (expected = DbServerServiceException.class)
    public void testCreateServerThrowsException() {
        String[] badParams = new String[]{"-tcpPort", "ERROR"};
        service.createServer(badParams);
    }
}
