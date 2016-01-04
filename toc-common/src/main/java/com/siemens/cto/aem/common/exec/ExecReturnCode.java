package com.siemens.cto.aem.common.exec;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class ExecReturnCode implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Integer ZERO = 0;
    public static final int STP_EXIT_CODE_FAST_FAIL = 125; /* Use FailFast Listener */
    public static final int STP_EXIT_CODE_ABNORMAL_SUCCESS = 126;
    public static final int STP_EXIT_CODE_NO_OP = 127;
    public static final int STP_EXIT_NO_SUCH_SERVICE = 123;
    public static final int STP_EXIT_PROCESS_KILLED = 255;

    private final Integer returnCode;

    public ExecReturnCode(final Integer theReturnCode) {
        returnCode = theReturnCode;
    }

    public Integer getReturnCode() {
        return returnCode;
    }

    public Boolean getWasSuccessful() {
        return wasSuccessful();
    }

    public Boolean getWasCompleted() {
        return wasCompleted();
    }

    public boolean wasSuccessful() {
        return (returnCode.equals(ZERO));
    }

    public boolean wasAbnormallySuccessful() {
        return returnCode.equals(STP_EXIT_CODE_ABNORMAL_SUCCESS);
    }

    public boolean wasCompleted() {
        return (returnCode != null);
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
        ExecReturnCode rhs = (ExecReturnCode) obj;
        return new EqualsBuilder()
                .append(this.returnCode, rhs.returnCode)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(returnCode)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("returnCode", returnCode)
                .toString();
    }
}
