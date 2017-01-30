package com.cerner.jwala.control.jvm.command;

/**
 * Created by Arvindo Kinny on 12/22/2016.
 */


import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.ssh.DecryptPassword;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.properties.PropertyKeys;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.exception.ApplicationServiceException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

import static com.cerner.jwala.control.AemControl.Properties.*;


/**
 * The CommandFactory class.<br/>
 */
@Component
public class JvmCommandFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(JvmCommandFactory.class);

    private HashMap<String, JvmCommand> commands;

    @Autowired
    protected SshConfiguration sshConfig;
    @Autowired
    protected RemoteCommandExecutorService remoteCommandExecutorService;


    /**
     *
     * @param jvm
     * @param operation
     * @return
     * @throws ApplicationServiceException
     */
    public RemoteCommandReturnInfo executeCommand(Jvm jvm, JvmControlOperation operation) throws ApplicationServiceException{
        if (commands.containsKey(operation.getExternalValue())) {
            return commands.get(operation.getExternalValue()).apply(jvm);
        }
        throw new ApplicationServiceException("JvmCommand not implemented: "+ operation.getExternalValue());
    }

    public void listCommands() {
        LOGGER.debug("Available jvm commands");
        for (String command:commands.keySet()) {
            LOGGER.debug(command);
        }
    }

    /* Factory pattern */
    @PostConstruct
    public void initJvmCommands() {
        commands = new HashMap<>();
        commands.put(JvmControlOperation.START.getExternalValue(), (Jvm jvm)
                -> remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(jvm),getExecCommand(START_SCRIPT_NAME.getValue(), jvm))));
        commands.put(JvmControlOperation.STOP.getExternalValue(), (Jvm jvm)
                -> remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(jvm),getExecCommandForStopService(jvm))));
        commands.put(JvmControlOperation.THREAD_DUMP.getExternalValue(), (Jvm jvm)
                -> remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(jvm),getExecCommandForThreadDump(THREAD_DUMP_SCRIPT_NAME.getValue(), jvm))));
        commands.put(JvmControlOperation.HEAP_DUMP.getExternalValue(), (Jvm jvm)
                -> remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(jvm),getExecCommandForHeapDump(HEAP_DUMP_SCRIPT_NAME.getValue(), jvm))));
        commands.put(JvmControlOperation.DEPLOY_CONFIG_ARCHIVE.getExternalValue(), (Jvm jvm)
                -> remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(jvm),getExecCommandForDeploy(jvm))));
        commands.put(JvmControlOperation.INSTALL_SERVICE.getExternalValue(), (Jvm jvm)
                -> remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(jvm),getExecCommandForInstallService(jvm))));
        commands.put(JvmControlOperation.DELETE_SERVICE.getExternalValue(), (Jvm jvm)
                -> remoteCommandExecutorService.executeCommand(new RemoteExecCommand(getConnection(jvm),getExecCommandForDeleteService(jvm))));


    }

    /**
     *
     * @param jvm
     * @return
     */
    private RemoteSystemConnection getConnection(Jvm jvm) {
        return new RemoteSystemConnection(sshConfig.getUserName(), sshConfig.getEncryptedPassword(), jvm.getHostName(), sshConfig.getPort());
    }

    /**
     * Get
     * @param jvm
     * @param scriptName
     * @return
     */
    private String getFullPathScript(Jvm jvm, String scriptName){
        return ApplicationProperties.get(PropertyKeys.REMOTE_PATH_INSTANCES_DIR)+ "/"+jvm.getJvmName()+"/"+
                ApplicationProperties.get(PropertyKeys.REMOTE_TOMCAT_DIR_NAME)+"/bin/"+scriptName;
    }

    /**
     * Generate parameters for JVM Heap dump
     * @param scriptName
     * @param jvm
     * @return
     */
    private ExecCommand getExecCommandForHeapDump(String scriptName, Jvm jvm) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd.HHmmss");
        String dumpFile = "heapDump." + StringUtils.replace(jvm.getJvmName(), " ", "") + "." +fmt.print(DateTime.now());
        String dumpLiveStr = ApplicationProperties.getAsBoolean(PropertyKeys.JMAP_DUMP_LIVE_ENABLED.name()) ? "live," : "\"\"";
        String jvmInstanceDir = ApplicationProperties.get(PropertyKeys.REMOTE_PATH_INSTANCES_DIR) + "/" +
                                StringUtils.replace(jvm.getJvmName(), " ", "")+ "/"+
                                ApplicationProperties.get(PropertyKeys.REMOTE_TOMCAT_DIR_NAME);
        return new ExecCommand(getFullPathScript(jvm, scriptName),
                ApplicationProperties.get(PropertyKeys.REMOTE_JAVA_HOME),
                ApplicationProperties.get(PropertyKeys.REMOTE_JAWALA_DATA_DIR),
                dumpFile, dumpLiveStr, jvmInstanceDir, jvm.getJvmName());
        //Windows " | grep PID | awk '{ print $3 }'`
    }

    /**
     * Generate parameters for Thread dump
     * @param scriptName
     * @param jvm
     * @return
     */
    private ExecCommand getExecCommandForThreadDump(String scriptName, Jvm jvm) {
        String jvmInstanceDir = ApplicationProperties.get(PropertyKeys.REMOTE_PATH_INSTANCES_DIR) + "/" +
                                StringUtils.replace(jvm.getJvmName(), " ", "")+ "/" +
                                ApplicationProperties.get(PropertyKeys.REMOTE_TOMCAT_DIR_NAME);

        return new ExecCommand(getFullPathScript(jvm, scriptName),
                                ApplicationProperties.get(PropertyKeys.REMOTE_JAVA_HOME),
                                jvmInstanceDir, jvm.getJvmName());
    }

    /**
     *
     * @param scriptName
     * @param jvm
     * @return
     */
    private ExecCommand getExecCommand(String scriptName, Jvm jvm){
        return new ExecCommand(getFullPathScript(jvm, scriptName), jvm.getJvmName());
    }

    /**
     * Method to generate remote command for extracting jar for jvm
     * @param jvm
     * @return
     */
    private ExecCommand getExecCommandForDeploy(Jvm jvm){
        final String remoteScriptDir = ApplicationProperties.getRequired(PropertyKeys.REMOTE_SCRIPT_DIR);
        final String remoteJavaHome = ApplicationProperties.get(PropertyKeys.REMOTE_JAVA_HOME);
        final String remotePathsInstancesDir = ApplicationProperties.get(PropertyKeys.REMOTE_PATH_INSTANCES_DIR);
        return new ExecCommand(remoteScriptDir+ "/" + jvm.getJvmName() + "/" + DEPLOY_CONFIG_ARCHIVE_SCRIPT_NAME,
                remoteScriptDir + "/" + jvm.getJvmName() + ".jar",
                remotePathsInstancesDir + "/" + jvm.getJvmName(),
                remoteJavaHome + "/bin/jar");
    }

    /**
     * Method to generate remote command for installing service for jvm
     * @param jvm
     * @return
     */
    private ExecCommand getExecCommandForInstallService(Jvm jvm){
        final String userName;
        final String encryptedPassword;

        if (jvm.getUserName()!=null) {
            userName = jvm.getUserName();
            encryptedPassword = jvm.getEncryptedPassword();
        } else {
            userName = null;
            encryptedPassword = null;
        }

        String remoteScriptDir = ApplicationProperties.getRequired(PropertyKeys.REMOTE_SCRIPT_DIR);
        String remotePathsInstancesDir = ApplicationProperties.getRequired(PropertyKeys.REMOTE_PATH_INSTANCES_DIR);

        final String quotedUsername;
        if (userName != null && userName.length() > 0) {
            quotedUsername = "\"" + userName + "\"";
        } else {
            quotedUsername = "";
        }
        final String decryptedPassword = encryptedPassword != null && encryptedPassword.length() > 0 ? new DecryptPassword().decrypt(encryptedPassword) : "";
        List<String> formatStrings = Arrays.asList(remoteScriptDir+ "/" + jvm.getJvmName() + "/" +INSTALL_SERVICE_SCRIPT_NAME,
                jvm.getJvmName(),
                remotePathsInstancesDir,
                ApplicationProperties.getRequired(PropertyKeys.REMOTE_TOMCAT_DIR_NAME));
        List<String> unformatStrings = Arrays.asList(quotedUsername, decryptedPassword);
        return new ExecCommand(
                formatStrings,
                unformatStrings);
    }

    private ExecCommand getExecCommandForDeleteService(Jvm jvm){
        //copy delete script
        return new ExecCommand(ApplicationProperties.getRequired(PropertyKeys.REMOTE_SCRIPT_DIR)+
                                "/" + jvm.getJvmName() + "/" +
                                DELETE_SERVICE_SCRIPT_NAME.getValue(), jvm.getJvmName());
    }

    private ExecCommand getExecCommandForStopService(Jvm jvm){
        //copy delete script
        return new ExecCommand(getFullPathScript(jvm, STOP_SCRIPT_NAME.getValue()), jvm.getJvmName(), SLEEP_TIME.getValue());
    }
}
