package com.cerner.jwala.common.rule.app;

import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.exception.MessageResponseStatus;
import com.cerner.jwala.common.rule.ValidNameRule;

public class ApplicationContextRule extends ValidNameRule {

    public ApplicationContextRule(final String theName) {
        super(theName);
    }

    @Override
    protected MessageResponseStatus getMessageResponseStatus() {
        return AemFaultType.INVALID_APPLICATION_CTX;
    }

    @Override
    protected String getMessage() {
        return "Invalid WebApp Context: \"" + name + "\"";
    }
}
