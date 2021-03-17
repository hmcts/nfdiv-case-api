package uk.gov.hmcts.reform.divorce.ccd.mock;

import de.cronn.reflection.util.TypedPropertyGetter;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.DisplayContext;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.EventTypeBuilder;
import uk.gov.hmcts.ccd.sdk.api.Field;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings("rawtypes")
@Getter
public class EventBuildingMockUtil {

    private ConfigBuilder<CaseData, State, UserRole> configBuilder;
    private EventTypeBuilder<CaseData, UserRole, State> eventTypeBuilder;
    private Event.EventBuilder<CaseData, UserRole, State> eventBuilder;
    private FieldCollection.FieldCollectionBuilder fieldCollectionBuilder;
    private Field.FieldBuilder fieldBuilder;

    @SuppressWarnings("unchecked")
    public EventBuildingMockUtil mockEventBuildingWith(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        this.configBuilder = configBuilder;
        eventTypeBuilder = mock(EventTypeBuilder.class, withSettings().lenient());
        eventBuilder = mock(Event.EventBuilder.class, withSettings().lenient());
        fieldCollectionBuilder = mock(FieldCollection.FieldCollectionBuilder.class, withSettings().lenient());
        fieldBuilder = mock(Field.FieldBuilder.class, withSettings().lenient());

        mockConfigBuilderAndEventTypeBuilderMethods();
        mockEventBuilderMethods();
        mockFieldCollectionBuilderMethods();
        mockFieldBuilderMethods();

        return this;
    }

    @SuppressWarnings("unchecked")
    public EventBuildingMockUtil mockEventBuilding() {
        return mockEventBuildingWith(mock(ConfigBuilder.class, withSettings().lenient()));
    }

    private void mockConfigBuilderAndEventTypeBuilderMethods() {

        when(configBuilder.event(any())).thenReturn(eventTypeBuilder);

        when(eventTypeBuilder.initialState(any())).thenReturn(eventBuilder);
        when(eventTypeBuilder.forState(any())).thenReturn(eventBuilder);
        when(eventTypeBuilder.forStateTransition(any(), any())).thenReturn(eventBuilder);
        when(eventTypeBuilder.forAllStates()).thenReturn(eventBuilder);
        when(eventTypeBuilder.forStates(any())).thenReturn(eventBuilder);
    }

    @SuppressWarnings("unchecked")
    private void mockEventBuilderMethods() {

        when(eventBuilder.fields()).thenReturn(fieldCollectionBuilder);
        when(eventBuilder.name(any())).thenReturn(eventBuilder);
        when(eventBuilder.eventId(any())).thenReturn(eventBuilder);
        when(eventBuilder.eventNumber(any(Integer.class))).thenReturn(eventBuilder);
        when(eventBuilder.aboutToStartURL(any())).thenReturn(eventBuilder);
        when(eventBuilder.aboutToStartWebhook()).thenReturn(eventBuilder);
        when(eventBuilder.aboutToStartWebhook(any())).thenReturn(eventBuilder);
        when(eventBuilder.aboutToStartWebhook(any(), any())).thenReturn(eventBuilder);
        when(eventBuilder.aboutToSubmitURL(any())).thenReturn(eventBuilder);
        when(eventBuilder.aboutToSubmitWebhook()).thenReturn(eventBuilder);
        when(eventBuilder.aboutToSubmitWebhook(any())).thenReturn(eventBuilder);
        when(eventBuilder.aboutToSubmitWebhook(any(), any())).thenReturn(eventBuilder);
        when(eventBuilder.allWebhooks()).thenReturn(eventBuilder);
        when(eventBuilder.allWebhooks(any())).thenReturn(eventBuilder);
        when(eventBuilder.dataClass(any())).thenReturn(eventBuilder);
        when(eventBuilder.description(any())).thenReturn(eventBuilder);
        when(eventBuilder.displayOrder(any(Integer.class))).thenReturn(eventBuilder);
        when(eventBuilder.endButtonLabel(any())).thenReturn(eventBuilder);
        when(eventBuilder.explicitGrants()).thenReturn(eventBuilder);
        when(eventBuilder.grant(any(Permission.class))).thenReturn(eventBuilder);
        when(eventBuilder.grant(any(Permission.class), any())).thenReturn(eventBuilder);
        when(eventBuilder.grant(any(Set.class))).thenReturn(eventBuilder);
        when(eventBuilder.grant(any(Set.class), any())).thenReturn(eventBuilder);
        when(eventBuilder.grantHistoryOnly()).thenReturn(eventBuilder);
        when(eventBuilder.grantHistoryOnly(any())).thenReturn(eventBuilder);
        when(eventBuilder.grants(any())).thenReturn(eventBuilder);
        when(eventBuilder.historyOnlyRoles(any())).thenReturn(eventBuilder);
        when(eventBuilder.id(any())).thenReturn(eventBuilder);
        when(eventBuilder.namespace(any())).thenReturn(eventBuilder);
        when(eventBuilder.postState(any())).thenReturn(eventBuilder);
        when(eventBuilder.preState(any())).thenReturn(eventBuilder);
        when(eventBuilder.retries(any())).thenReturn(eventBuilder);
        when(eventBuilder.showEventNotes()).thenReturn(eventBuilder);
        when(eventBuilder.showSummary()).thenReturn(eventBuilder);
        when(eventBuilder.showSummary(any(Boolean.class))).thenReturn(eventBuilder);
        when(eventBuilder.showSummaryChangeOption()).thenReturn(eventBuilder);
        when(eventBuilder.showSummaryChangeOption(any(Boolean.class))).thenReturn(eventBuilder);
        when(eventBuilder.submittedWebhook()).thenReturn(eventBuilder);
    }

    @SuppressWarnings("unchecked")
    private void mockFieldCollectionBuilderMethods() {

        when(fieldCollectionBuilder.done()).thenReturn(eventBuilder);
        when(fieldCollectionBuilder.optional(any(TypedPropertyGetter.class))).thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.optional(any(TypedPropertyGetter.class), any(String.class)))
            .thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.mandatory(any(TypedPropertyGetter.class))).thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.mandatory(any(TypedPropertyGetter.class), any(String.class)))
            .thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.readonly(any(TypedPropertyGetter.class))).thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.readonly(any(TypedPropertyGetter.class), any(String.class)))
            .thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.list(any(TypedPropertyGetter.class))).thenReturn(fieldBuilder);
        when(fieldCollectionBuilder.list(any(String.class))).thenReturn(fieldBuilder);
        when(fieldCollectionBuilder.immutableList(any(TypedPropertyGetter.class))).thenReturn(fieldBuilder);
        when(fieldCollectionBuilder.field(any(), any(), any(), any(), any(), any())).thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.field(any(String.class), any(DisplayContext.class), any(String.class)))
            .thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.field(any(String.class), any(DisplayContext.class))).thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.field(any(String.class))).thenReturn(fieldBuilder);
        when(fieldCollectionBuilder.field(any(TypedPropertyGetter.class), any(DisplayContext.class)))
            .thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.field(any(TypedPropertyGetter.class), any(DisplayContext.class), any(String.class)))
            .thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.field(any(TypedPropertyGetter.class), any(DisplayContext.class), any(Boolean.class)))
            .thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.field(any(TypedPropertyGetter.class))).thenReturn(fieldBuilder);
        when(fieldCollectionBuilder.showCondition(any())).thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.midEventWebhook(any())).thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.midEventWebhook()).thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.complex(any(), any(), any(), any(Boolean.class))).thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.complex(any(), any(), any())).thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.complex(any(TypedPropertyGetter.class))).thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.complex(any(TypedPropertyGetter.class), any(Class.class), any(Boolean.class)))
            .thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.complex(any(TypedPropertyGetter.class), any(Class.class)))
            .thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.label(any(), any())).thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.page(any(String.class))).thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.page(any(Integer.class))).thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.previousPage()).thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.pageLabel(any(String.class))).thenReturn(fieldCollectionBuilder);
    }

    @SuppressWarnings("unchecked")
    private void mockFieldBuilderMethods() {
        when(fieldBuilder.optional()).thenReturn(fieldBuilder);
        when(fieldBuilder.mandatory()).thenReturn(fieldBuilder);
        when(fieldBuilder.blacklist(any(), any())).thenReturn(fieldBuilder);
        when(fieldBuilder.blacklist(any(), any())).thenReturn(fieldBuilder);
        when(fieldBuilder.blacklist(any())).thenReturn(fieldBuilder);
        when(fieldBuilder.immutable()).thenReturn(fieldBuilder);
        when(fieldBuilder.showSummary()).thenReturn(fieldBuilder);
        when(fieldBuilder.showSummary(any(Boolean.class))).thenReturn(fieldBuilder);
        when(fieldBuilder.complex()).thenReturn(fieldCollectionBuilder);
        when(fieldBuilder.complex(any())).thenReturn(fieldCollectionBuilder);
        when(fieldBuilder.complex(any(), any())).thenReturn(fieldCollectionBuilder);
        when(fieldBuilder.complexWithParent(any())).thenReturn(fieldCollectionBuilder);
        when(fieldBuilder.readOnly()).thenReturn(fieldBuilder);
        when(fieldBuilder.context(any())).thenReturn(fieldBuilder);
        when(fieldBuilder.label(anyString())).thenReturn(fieldBuilder);
        when(fieldBuilder.showCondition(anyString())).thenReturn(fieldBuilder);
        when(fieldBuilder.done()).thenReturn(fieldCollectionBuilder);
    }
}
