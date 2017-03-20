package com.cerner.jwala.common;

import com.cerner.jwala.common.properties.PropertyKeys;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cerner.jwala.common.exception.ApplicationException;
import com.cerner.jwala.common.properties.ApplicationProperties;

import java.io.File;

public class ApplicationPropertiesTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationPropertiesTest.class);

    public void setUp() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources/properties");
        LOGGER.debug("Loading properties from dir " + System.getProperty(ApplicationProperties.PROPERTIES_ROOT_PATH));
    }

    public void testBadPropertiesPath() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/blah");
        try {
            ApplicationProperties.get("doesn't matter");
        } catch (ApplicationException e) {
            assertTrue(true);
            return;
        }

        assertFalse(false);
    }

    public void testLoadProperties() {
        assertTrue(ApplicationProperties.size() > 0);
    }

    public void testReadProperties() {
        assertEquals(ApplicationProperties.get(PropertyKeys.STRING_PROPERTY), ApplicationProperties.get("string.property"));
        assertEquals(Integer.valueOf(5), ApplicationProperties.getAsInteger(ApplicationProperties.get(PropertyKeys.INTEGER_PROPERTY)));
        assertEquals(Boolean.TRUE, ApplicationProperties.getAsBoolean(ApplicationProperties.get(PropertyKeys.BOOLEAN_PROPERTY)));
    }

    public void testReload() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources/properties/reload");
        ApplicationProperties.reload();
        assertEquals("reloaded", ApplicationProperties.get("reload.property"));
        assertNull(ApplicationProperties.get("home team"));
    }
}
