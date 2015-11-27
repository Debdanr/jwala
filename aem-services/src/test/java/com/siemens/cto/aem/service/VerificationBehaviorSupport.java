package com.siemens.cto.aem.service;

import com.siemens.cto.aem.domain.command.Command;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.command.group.AddJvmToGroupCommand;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;

public class VerificationBehaviorSupport {

    protected Set<AddJvmToGroupCommand> createMockedAddCommands(final int aNumberToCreate) {

        final Set<AddJvmToGroupCommand> commands = new HashSet<>(aNumberToCreate);

        for (int i = 0; i < aNumberToCreate; i++) {
            commands.add(mock(AddJvmToGroupCommand.class));
        }

        return commands;
    }

    protected <T> Event<T> matchCommandInEvent(final T aCommand) {
        return argThat(new EventMatcher<>(aCommand));
    }

    protected <T extends Command> T matchCommand(final T aCommand) {
        return argThat(new CommandMatcher<T>(aCommand));
    }
}
