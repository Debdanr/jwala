package com.siemens.cto.aem.persistence.jpa.service.state.impl;

import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.ExternalizableState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.command.SetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.persistence.jpa.domain.JpaCurrentState;
import com.siemens.cto.aem.persistence.jpa.domain.JpaCurrentStateId;
import com.siemens.cto.aem.persistence.jpa.service.JpaQueryPaginator;
import com.siemens.cto.aem.persistence.jpa.service.state.StateCrudService;

public class StateCrudServiceImpl<S, T extends ExternalizableState> implements StateCrudService<S, T> {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;

    private final StateType stateType;
    private final JpaQueryPaginator paginator;

    public StateCrudServiceImpl(final StateType theStateType) {
        stateType = theStateType;
        paginator = new JpaQueryPaginator();
    }

    @Override
    public JpaCurrentState updateState(final Event<SetStateCommand<S, T>> anEvent) {

        final JpaCurrentState currentState = new JpaCurrentState();
        final JpaCurrentStateId id = new JpaCurrentStateId(anEvent.getCommand().getNewState().getId().getId(),
                                                           stateType);
        currentState.setId(id);
        currentState.setState(anEvent.getCommand().getNewState().getState().toStateString());
        currentState.setAsOf(anEvent.getCommand().getNewState().getAsOf().toCalendar(Locale.US));

        return entityManager.merge(currentState);
    }

    @Override
    public JpaCurrentState getState(final Identifier<S> anId) {

        final JpaCurrentState currentState = entityManager.find(JpaCurrentState.class,
                                                                new JpaCurrentStateId(anId.getId(),
                                                                                      stateType));

        return currentState;
    }

    @Override
    public List<JpaCurrentState> getStates(final PaginationParameter somePagination) {
        final Query query = entityManager.createQuery("SELECT j FROM JpaCurrentState j WHERE j.id.stateType = :stateType");

        query.setParameter("stateType", stateType);
        paginator.paginate(query,
                           somePagination);

        return query.getResultList();
    }
}
