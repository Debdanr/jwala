package com.cerner.jwala.common.domain.model.jvm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cerner.jwala.common.domain.model.state.OperationalState;

import java.util.HashMap;
import java.util.Map;

/**
 * JvmState defines the known states for JVMs.
 * JVMs utilize infrastructure-provided code to send JVM state to Jwala.
 * The translation is done using the enum names defined for JvmState here.
 * 
 * @author horspe00
 */
public enum JvmState implements OperationalState {

    JVM_NEW             (StateLabel.NEW, Started.NO),
    JVM_INITIALIZING    (StateLabel.INITIALIZING, Started.YES),
    JVM_INITIALIZED     (StateLabel.INITIALIZING, Started.YES),
    JVM_START           (StateLabel.START_SENT, Started.YES) /* TODO: Remove from enum. This is no longer part of the JVM state. */ ,
    JVM_STARTING        (StateLabel.STARTING, Started.YES),
    JVM_STARTED         (StateLabel.STARTED, Started.YES),
    JVM_STOP            (StateLabel.STOP_SENT, Started.YES) /* TODO: Remove from enum. This is no longer part of the JVM state. */,
    JVM_STOPPING        (StateLabel.STOPPING, Started.YES),
    JVM_STOPPED         (StateLabel.STOPPED, Started.NO),
    JVM_DESTROYING      (StateLabel.DESTROYING, Started.YES),
    JVM_DESTROYED       (StateLabel.DESTROYED, Started.NO),
    JVM_UNEXPECTED_STATE(StateLabel.UNEXPECTED_STATE, Started.NO),
    JVM_FAILED          (StateLabel.FAILED, Started.NO),
    FORCED_STOPPED      (StateLabel.FORCED_STOPPED, Started.NO),
    JVM_UNKNOWN         (StateLabel.UNKNOWN, Started.NO)
    ;

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmState.class);
    private static final Map<String, JvmState> LOOKUP_MAP = new HashMap<>();
    private boolean isStartedState;
    private final String stateLabel;

    static {
        for (final JvmState state : values()) {
            LOOKUP_MAP.put(state.name(), state);
        }
    }

    /**
     * Converts a state from String to {@link JvmState}.
     * @param state e.g. JVM_STOPPED
     * @return {@link JvmState}
     */
    public static JvmState convertFrom(final String state) {
        if (LOOKUP_MAP.containsKey(state)) {
            return LOOKUP_MAP.get(state);
        }
        LOGGER.error("Unexpected JVM state:{} from db! Returning JVM_UNEXPECTED_STATE.", state);
        return JVM_UNEXPECTED_STATE;
    }

    JvmState(final String stateLabel, final boolean startedFlag) {
        this.stateLabel = stateLabel;
        this.isStartedState = startedFlag;
    }

    @Override
    public String toStateLabel() {
        return stateLabel;
    }

    @Override
    public String toPersistentString() {
        return name();
    }

    public boolean isStartedState() {
        return isStartedState;
    }

    private static class StateLabel {
        public static final String INITIALIZING = "INITIALIZING";
        public static final String NEW = "NEW";
        public static final String START_SENT = "START SENT";
        public static final String STARTING = "STARTING";
        public static final String STARTED = "STARTED";
        public static final String STOP_SENT = "STOP SENT";
        public static final String STOPPING = "STOPPING";
        public static final String STOPPED = "STOPPED";
        public static final String DESTROYING = "DESTROYING";
        public static final String DESTROYED = "DESTROYED";
        public static final String UNEXPECTED_STATE = "UNEXPECTED_STATE";
        public static final String FAILED = "FAILED";
        public static final String FORCED_STOPPED = "FORCE STOPPED";
        public static final String UNKNOWN = "UNKNOWN";
    }

    private static class Started {
        public static final boolean YES = true;
        public static final boolean NO = false;
    }

}
