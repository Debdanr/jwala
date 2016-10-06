package com.cerner.jwala.common.domain.model.webserver;

import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.exception.BadRequestException;

import java.util.HashMap;
import java.util.Map;

public enum WebServerControlOperation {

    START("start", WebServerReachableState.WS_START_SENT),
    STOP("stop", WebServerReachableState.WS_STOP_SENT),
    VIEW_HTTP_CONFIG_FILE("viewHttpConfigFile", null),
    SECURE_COPY("secureCopy", null),
    BACK_UP("backUpConfigFile", null),
    DELETE_SERVICE("deleteService", null),
    INVOKE_SERVICE("invokeService", null),
    CREATE_DIRECTORY("mkdir", null),
    CHANGE_FILE_MODE("chmod", null),
    CHECK_FILE_EXISTS("test", null);

    private static final Map<String, WebServerControlOperation> LOOKUP_MAP = new HashMap<>();

    static {
        for (final WebServerControlOperation operation : values()) {
            LOOKUP_MAP.put(operation.operationValue.toLowerCase(), operation);
        }
    }

    private final String operationValue;
    private final WebServerReachableState operationState;

    private WebServerControlOperation(final String theValue,
                                      final WebServerReachableState theOperationState) {
        operationValue = theValue;
        operationState = theOperationState;
    }

    public static WebServerControlOperation convertFrom(final String aValue) {
        final String value = aValue.toLowerCase();
        if (LOOKUP_MAP.containsKey(value)) {
            return LOOKUP_MAP.get(value);
        }

        throw new BadRequestException(AemFaultType.INVALID_WEBSERVER_OPERATION,
                "Invalid operation: " + aValue);
    }

    public String getExternalValue() {
        return operationValue;
    }

    public WebServerReachableState getOperationState() {
        return operationState;
    }
}
