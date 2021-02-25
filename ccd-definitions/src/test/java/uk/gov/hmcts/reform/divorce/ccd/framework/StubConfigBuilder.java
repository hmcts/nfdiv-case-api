package uk.gov.hmcts.reform.divorce.ccd.framework;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.types.Event;
import uk.gov.hmcts.ccd.sdk.types.EventTypeBuilder;
import uk.gov.hmcts.ccd.sdk.types.Field;
import uk.gov.hmcts.ccd.sdk.types.HasRole;
import uk.gov.hmcts.ccd.sdk.types.RoleBuilder;
import uk.gov.hmcts.ccd.sdk.types.Tab.TabBuilder;
import uk.gov.hmcts.ccd.sdk.types.WebhookConvention;
import uk.gov.hmcts.ccd.sdk.types.WorkBasket;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@SuppressWarnings("rawtypes")
public class StubConfigBuilder<T, S, R extends HasRole> implements ConfigBuilder<T, S, R> {

    private final List<Grant> grant = new ArrayList<>();
    private final List<GrantHistory> grantHistory = new ArrayList<>();
    private final Map<S, String> prefixes = new HashMap<>();
    private final List<Field.FieldBuilder> explicitFields = new ArrayList<>();
    private final List<TabBuilder> tabs = new ArrayList<>();
    private final List<WorkBasket.WorkBasketBuilder> workBasketResultFields = new ArrayList<>();
    private final List<WorkBasket.WorkBasketBuilder> workBasketInputFields = new ArrayList<>();
    private final Map<String, String> roleHierarchy = new Hashtable<>();
    private final Set<String> apiOnlyRoles = new HashSet<>();
    private final Set<String> noFieldAuthRoles = new HashSet<>();
    private final Map<EventState, List<Event.EventBuilder<T, R, S>>> events = new HashMap<>();

    private String caseType;
    private String env;
    private WebhookConvention webhookConvention;

    @Override
    public EventTypeBuilder<T, R, S> event(final String id) {
        final Event.EventBuilder<T, R, S> eventBuilder = Event.EventBuilder.builder(CaseData.class, webhookConvention, new StubPropertyUtils());
        eventBuilder.id(id);
        eventBuilder.eventId(id);
        return new StubEventTypeBuilder(eventBuilder);
    }

    @Override
    public void caseType(final String caseType) {
        this.caseType = caseType;
    }

    @Override
    public void setEnvironment(final String env) {
        this.env = env;
    }

    @Override
    public void grant(final S state, final String permissions, final R role) {
        this.grant.add(new Grant(state, permissions, role));
    }

    @Override
    public void grantHistory(final S state, final R... role) {
        this.grantHistory.add(new GrantHistory(state, role));
    }

    @Override
    public void prefix(final S state, final String prefix) {
        prefixes.put(state, prefix);
    }

    @Override
    public Field.FieldBuilder<?, ?, ?> field(final String id) {
        final Field.FieldBuilder builder = Field.FieldBuilder.builder(CaseData.class, null, id);
        explicitFields.add(builder);
        return builder;
    }

    @Override
    public void caseField(final String id, final String showCondition, final String type, final String typeParam, final String label) {
        field(id).label(label).type(type).fieldTypeParameter(typeParam);
    }

    @Override
    public void caseField(final String id, final String label, final String type, final String collectionType) {
        caseField(id, null, type, collectionType, label);
    }

    @Override
    public void caseField(final String id, final String label, final String type) {
        caseField(id, label, type, null);
    }

    @Override
    public void setWebhookConvention(final WebhookConvention convention) {
        this.webhookConvention = convention;
    }

    @Override
    public TabBuilder<CaseData, HasRole> tab(final String tabId, final String tabLabel) {
        final TabBuilder<CaseData, HasRole> result = TabBuilder.builder(CaseData.class,
            new StubPropertyUtils()).tabID(tabId).label(tabLabel);
        tabs.add(result);
        return result;
    }

    @Override
    public WorkBasket.WorkBasketBuilder workBasketResultFields() {
        final WorkBasket.WorkBasketBuilder result = WorkBasket.WorkBasketBuilder.builder(CaseData.class, new StubPropertyUtils());
        workBasketResultFields.add(result);
        return result;
    }

    @Override
    public WorkBasket.WorkBasketBuilder workBasketInputFields() {
        final WorkBasket.WorkBasketBuilder result = WorkBasket.WorkBasketBuilder.builder(CaseData.class, new StubPropertyUtils());
        workBasketInputFields.add(result);
        return result;
    }

    @Override
    public RoleBuilder<R> role(final R... roles) {
        return new RoleBuilder<R>() {
            @Override
            public void has(R parent) {
                for (R role : roles) {
                    roleHierarchy.put(role.getRole(), parent.getRole());
                }
            }

            @Override
            public void setApiOnly() {
                for (R role : roles) {
                    apiOnlyRoles.add(role.getRole());
                }
            }

            @Override
            public void noCaseEventToField() {
                for (R role : roles) {
                    noFieldAuthRoles.add(role.getRole());
                }
            }
        };
    }

    @Getter
    public class Grant {

        private final S state;
        private final String permission;
        private final R userRole;

        public Grant(final S state, final String permission, final R userRole) {
            this.state = state;
            this.permission = permission;
            this.userRole = userRole;
        }
    }

    @Getter
    public class GrantHistory {

        private final S state;
        private final R[] userRoles;

        public GrantHistory(final S state, final R[] userRoles) {
            this.state = state;
            this.userRoles = userRoles;
        }
    }

    @Getter
    @EqualsAndHashCode
    public static class EventState {

        private final String fromState;
        private final String toState;

        public EventState(final String fromState, final String toState) {
            this.fromState = fromState;
            this.toState = toState;
        }
    }

    public class StubEventTypeBuilder implements EventTypeBuilder<T, R, S> {

        private Event.EventBuilder<T, R, S> eventBuilder;

        public StubEventTypeBuilder(final Event.EventBuilder<T, R, S> eventBuilder) {
            this.eventBuilder = eventBuilder;
        }

        @Override
        public Event.EventBuilder<T, R, S> forState(final S state) {
            final EventState eventState = new EventState(state.toString(), state.toString());

            events.computeIfAbsent(eventState, e -> new ArrayList<>());
            events.get(eventState).add(eventBuilder);

            return eventBuilder;
        }

        @Override
        public Event.EventBuilder<T, R, S> initialState(final S state) {
            final EventState eventState = new EventState("", state.toString());

            events.computeIfAbsent(eventState, e -> new ArrayList<>());
            events.get(eventState).add(eventBuilder);

            return eventBuilder;
        }

        @Override
        public Event.EventBuilder<T, R, S> forStateTransition(final S from, final S to) {
            final EventState eventState = new EventState(from.toString(), to.toString());

            events.computeIfAbsent(eventState, e -> new ArrayList<>());
            events.get(eventState).add(eventBuilder);

            return eventBuilder;
        }

        @Override
        public Event.EventBuilder<T, R, S> forAllStates() {
            final EventState eventState = new EventState("*", "*");

            events.computeIfAbsent(eventState, e -> new ArrayList<>());
            events.get(eventState).add(eventBuilder);

            return eventBuilder;
        }

        @Override
        public Event.EventBuilder<T, R, S> forStates(final S... states) {
            for (S state : states) {
                forState(state);
            }

            return eventBuilder;
        }
    }
}
