package com.cerner.jwala.service;

import com.cerner.jwala.common.exec.RemoteExecCommand;

/**
 * Defines a contract for executing remote commands.
 *
 * Created by Jedd Cuison on 3/25/2016.
 */
public interface RemoteCommandExecutorService {

    RemoteCommandReturnInfo executeCommand(RemoteExecCommand remoteExecCommand);
}
