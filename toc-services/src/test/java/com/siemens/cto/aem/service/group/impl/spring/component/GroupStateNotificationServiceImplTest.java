package com.siemens.cto.aem.service.group.impl.spring.component;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.service.JvmCrudService;
import com.siemens.cto.aem.persistence.jpa.service.WebServerCrudService;
import com.siemens.cto.aem.service.MessagingService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link GroupStateNotificationServiceImpl}.
 *
 * Created by JC043760 on 4/18/2016.
 */
public class GroupStateNotificationServiceImplTest {

    private GroupStateNotificationServiceImpl groupStateNotificationServiceImpl;

    @Mock
    private JvmCrudService mockJvmCrudService;

    @Mock
    private WebServerCrudService mockWebServerCrudService;

    private static String [] groupStateArray = new String[2];
    private static int groupStateArrayCount = 0;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        groupStateNotificationServiceImpl = new GroupStateNotificationServiceImpl(mockJvmCrudService, mockWebServerCrudService,
                new TesterMessagingService());
    }

    @Test
    public void testRetrieveStateAndSendToATopic() {
        final JpaJvm mockJpaJvm = mock(JpaJvm.class);
        final List<JpaGroup> jpaGroupList = new ArrayList<>();
        jpaGroupList.add(mock(JpaGroup.class));
        jpaGroupList.add(mock(JpaGroup.class));
        when(jpaGroupList.get(0).getId()).thenReturn(1L);
        when(jpaGroupList.get(1).getId()).thenReturn(2L);
        final Identifier<Jvm> id = new Identifier<>(1L);
        when(mockJvmCrudService.getJvm(eq(id))).thenReturn(mockJpaJvm);
        when(mockJpaJvm.getGroups()).thenReturn(jpaGroupList);
        when(mockJvmCrudService.getJvmStartedCount(anyString())).thenReturn(3L);
        when(mockJvmCrudService.getJvmCount(anyString())).thenReturn(4L);
        when(mockWebServerCrudService.getWebServerStartedCount(anyString())).thenReturn(2L);
        when(mockWebServerCrudService.getWebServerCount(anyString())).thenReturn(2L);
        groupStateNotificationServiceImpl.retrieveStateAndSendToATopic(id, Jvm.class);
        System.out.println(groupStateArray[0]);
        System.out.println(groupStateArray[1]);
        assertEquals("Identifier[id=1], GRP_UNKNOWN", groupStateArray[0]);
        assertEquals("Identifier[id=2], GRP_UNKNOWN", groupStateArray[1]);
    }

    private static class TesterMessagingService implements MessagingService {

        @Override
        public void send(Object payLoad) {
            final CurrentState<Group, GroupState> groupState = (CurrentState<Group, GroupState>) payLoad;
            groupStateArray[groupStateArrayCount++] = groupState.getId() + ", " + groupState.getState();
        }
    }

}
