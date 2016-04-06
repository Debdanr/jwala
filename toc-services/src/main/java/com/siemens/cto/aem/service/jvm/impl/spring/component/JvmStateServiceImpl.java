package com.siemens.cto.aem.service.jvm.impl.spring.component;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.common.exec.RemoteExecCommand;
import com.siemens.cto.aem.common.exec.RemoteSystemConnection;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;
import com.siemens.cto.aem.service.MessagingService;
import com.siemens.cto.aem.service.RemoteCommandExecutorService;
import com.siemens.cto.aem.service.RemoteCommandReturnInfo;
import com.siemens.cto.aem.service.group.GroupStateNotificationService;
import com.siemens.cto.aem.service.jvm.JvmStateService;
import com.siemens.cto.aem.service.state.InMemoryStateManagerService;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * {@link JvmStateService} implementation.
 *
 * Created by JC043760 on 3/22/2016.
 */
@Service
public class JvmStateServiceImpl implements JvmStateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmStateServiceImpl.class);

    private final JvmPersistenceService jvmPersistenceService;
    private final InMemoryStateManagerService<Identifier<Jvm>, CurrentState<Jvm, JvmState>> inMemoryStateManagerService;
    private final JvmStateResolverWorker jvmStateResolverWorker;
    private final long jvmStateUpdateInterval;
    private final MessagingService messagingService;
    private final GroupStateNotificationService groupStateNotificationService;
    private final RemoteCommandExecutorService remoteCommandExecutorService;
    private final SshConfiguration sshConfig;

    private static final Map<Identifier<Jvm>, Future<CurrentState<Jvm, JvmState>>> PING_FUTURE_MAP = new HashMap<>();

    @Autowired
    public JvmStateServiceImpl(final JvmPersistenceService jvmPersistenceService,
                               @Qualifier("jvmInMemoryStateManagerService")
                               final InMemoryStateManagerService<Identifier<Jvm>, CurrentState<Jvm, JvmState>> inMemoryStateManagerService,
                               final JvmStateResolverWorker jvmStateResolverWorker,
                               final MessagingService messagingService,
                               final GroupStateNotificationService groupStateNotificationService,
                               @Value("${jvm.state.update.interval:60000}")
                               final long jvmStateUpdateInterval,
                               final RemoteCommandExecutorService remoteCommandExecutorService,
                               final SshConfiguration sshConfig) {
        this.jvmPersistenceService = jvmPersistenceService;
        this.inMemoryStateManagerService = inMemoryStateManagerService;
        this.jvmStateResolverWorker = jvmStateResolverWorker;
        this.jvmStateUpdateInterval = jvmStateUpdateInterval;
        this.messagingService = messagingService;
        this.groupStateNotificationService = groupStateNotificationService;
        this.remoteCommandExecutorService = remoteCommandExecutorService;
        this.sshConfig = sshConfig;
    }

    @Override
    @Scheduled(fixedDelayString = "${ping.jvm.period.millis}")
    public void verifyAndUpdateNotInMemOrStartedAndStaleStates() {
        final List<Jvm> jvms = jvmPersistenceService.getJvms();
        for (final Jvm jvm : jvms) {
            if ((!isStateInMemory(jvm) || (isStarted(jvm) && isStale(jvm))) &&
                (!PING_FUTURE_MAP.containsKey(jvm.getId()) || PING_FUTURE_MAP.get(jvm.getId()).isDone())) {
                    LOGGER.debug("Pinging JVM {} ...", jvm.getJvmName());
                    PING_FUTURE_MAP.put(jvm.getId(), jvmStateResolverWorker.pingAndUpdateJvmState(jvm, this));
                    LOGGER.debug("Pinged JVM {}", jvm.getJvmName());
            }
        }
    }

    /**
     * Check if the JVM's state is stale by checking the state's time stamp.
     * @param jvm {@link Jvm}
     * @return true if the state is stale.
     */
    protected boolean isStale(final Jvm jvm) {
        final long interval = DateTime.now().getMillis() - inMemoryStateManagerService.get(jvm.getId()).getAsOf().getMillis();
        if (interval > jvmStateUpdateInterval) {
            LOGGER.debug("JVM {}'s state is stale. Interval since last update = {} sec!", jvm.getJvmName(), interval/1000);
            return true;
        }
        return false;
    }

    /**
     * Checks if a JVM's state is set to started.
     * @param jvm {@link Jvm}
     * @return true if the state is started.
     */
    protected boolean isStarted(final Jvm jvm) {
        return inMemoryStateManagerService.get(jvm.getId()).getState().equals(JvmState.JVM_STARTED);
    }

    @Override
    public void updateNotInMemOrStartedButStaleState(final Jvm jvm, final JvmState state, final String errMsg) {
        // Check again before updating to make sure that nothing has change after pinging the JVM.
        if (!isStateInMemory(jvm) || (isStarted(jvm) && isStale(jvm))) {
                LOGGER.debug("Updating state of JVM {} ...", jvm.getJvmName());
                updateState(jvm.getId(), state, errMsg);
                groupStateNotificationService.retrieveStateAndSendToATopic(jvm.getId(), Jvm.class);
                LOGGER.debug("Updated state of JVM {}!", jvm.getJvmName());
        }
    }

    /**
     * Check if the state in the application context state map
     * @param jvm {@link Jvm}
     * @return true if the state of a certain JVM is in the application context state map.
     */
    protected boolean isStateInMemory(Jvm jvm) {
        return inMemoryStateManagerService.containsKey(jvm.getId());
    }

    @Override
    public RemoteCommandReturnInfo getServiceStatus(final Jvm jvm) {
        final RemoteExecCommand remoteExecCommand = new RemoteExecCommand(new RemoteSystemConnection(sshConfig.getUserName(),
                sshConfig.getPassword(), jvm.getHostName(), sshConfig.getPort()) , new ExecCommand("sc query '" +
                jvm.getJvmName() + "' | grep STATE"));
        return remoteCommandExecutorService.executeCommand(remoteExecCommand);
    }

    @Override
    public void updateState(final Identifier<Jvm> id, final JvmState state) {
        updateState(id, state, StringUtils.EMPTY);
    }

    @Override
    public void updateState(final Identifier<Jvm> id, final JvmState state, final String errMsg) {
        jvmPersistenceService.updateState(id, state, errMsg);
        inMemoryStateManagerService.put(id, new CurrentState<>(id, state, DateTime.now(), StateType.JVM));
        messagingService.send(new CurrentState<>(id, state, DateTime.now(), StateType.JVM, errMsg));
    }
}
