package com.siemens.cto.aem.domain.model.fault;

import com.siemens.cto.aem.common.exception.MessageResponseStatus;

public enum AemFaultType implements MessageResponseStatus {

    GROUP_NOT_FOUND("AEM1", "GroupNotFound"),
    INVALID_GROUP_NAME("AEM2", "InvalidGroupName"),
    INVALID_IDENTIFIER("AEM3", "InvalidIdentifier"),
    DUPLICATE_GROUP_NAME("AEM4", "DuplicateGroupName"),
    INVALID_JVM_NAME("AEM5", "InvalidJvmName"),
    INVALID_HOST_NAME("AEM6", "InvalidHostName"),
    GROUP_NOT_SPECIFIED("AEM7", "GroupNotSpecified"),
    JVM_NOT_FOUND("AEM8", "JvmNotFound");

    private final String faultCode;
    private final String faultMessage;

    private AemFaultType(final String theFaultCode,
                         final String theFaultMessage) {
        faultCode = theFaultCode;
        faultMessage = theFaultMessage;
    }

    @Override
    public String getMessageCode() {
        return faultCode;
    }

    @Override
    public String getMessage() {
        return faultMessage;
    }
}
