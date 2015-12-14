package com.siemens.cto.aem.common.rule;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.MessageResponseStatus;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class PortNumberRule implements Rule {

    private final Integer port;
	private final AemFaultType error;
    private final boolean nullable;

    public PortNumberRule(final Integer thePort, final AemFaultType errorCode) {
        this(thePort,
             errorCode,
             false);
    }

    public PortNumberRule(final Integer thePort, final AemFaultType errorCode, final boolean nullable) {
        port = thePort;
        error = errorCode;
        this.nullable = nullable;
    }

    protected Integer getPort() { 
        return port;
    }
    
    @Override
    public boolean isValid() {
        if (nullable && port == null) {
            return true;
        }
        return (port != null) && (port > 0/*TCP/IP Reserved Port*/) && (port <= 65535 /*2^16-1*/);
    }

    @Override
    public void validate() throws BadRequestException {
        if (!isValid()) {
            throw new BadRequestException(getMessageResponseStatus(),
                                          getMessage());
        }
    }

    protected MessageResponseStatus getMessageResponseStatus() { return error; }

    protected String getMessage() {
   		return "Port specified is invalid" + (port != null?(" ("+port+")."):".");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        PortNumberRule rhs = (PortNumberRule) obj;
        return new EqualsBuilder()
                .append(this.port, rhs.port)
                .append(this.error, rhs.error)
                .append(this.nullable, rhs.nullable)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(port)
                .append(error)
                .append(nullable)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("port", port)
                .append("error", error)
                .append("nullable", nullable)
                .toString();
    }
}
