package com.siemens.cto.aem.service.group.impl;

import com.siemens.cto.aem.domain.command.dispatch.GroupWebServerDispatchCommand;
import com.siemens.cto.aem.domain.command.dispatch.WebServerDispatchCommandResult;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.user.User;
import com.siemens.cto.aem.domain.command.webserver.ControlGroupWebServerCommand;
import com.siemens.cto.aem.persistence.service.group.GroupControlPersistenceService;
import com.siemens.cto.aem.service.dispatch.CommandDispatchGateway;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.group.GroupWebServerControlService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class GroupWebServerControlServiceImpl implements GroupWebServerControlService {

    private final GroupControlPersistenceService persistenceService;
    private final GroupService groupService;
    private final CommandDispatchGateway commandDispatchGateway;

    public GroupWebServerControlServiceImpl(final GroupControlPersistenceService thePersistenceService,
            final GroupService theGroupService, final CommandDispatchGateway theCommandDispatchGateway) {

        persistenceService = thePersistenceService;
        groupService = theGroupService;
        commandDispatchGateway = theCommandDispatchGateway;
    }
    
    @Transactional
    @Override
    public void controlGroup(ControlGroupWebServerCommand aCommand, User aUser) {

        aCommand.validateCommand();

        Group group = groupService.getGroup(aCommand.getGroupId());

        GroupWebServerDispatchCommand dispatchCommand = new GroupWebServerDispatchCommand(group, aCommand, aUser);
        
        commandDispatchGateway.asyncDispatchCommand(dispatchCommand);
    }

    @Transactional
    @Override
    public void dispatchCommandComplete(List<WebServerDispatchCommandResult> results) {
    }

}
