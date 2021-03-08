package uk.gov.hmcts.reform.divorce.ccd.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.EventTypeBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.reform.divorce.ccd.mock.EventBuildingMockUtil;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.divorce.ccd.model.CaseEvent.DRAFT_CREATE;
import static uk.gov.hmcts.reform.divorce.ccd.model.State.Draft;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class DraftCreateTest {

    private final DraftCreate draftCreate = new DraftCreate();

    private final EventBuildingMockUtil eventBuildingMockUtil = new EventBuildingMockUtil().mockEventBuilding();
    private final ConfigBuilder<CaseData, State, UserRole> configBuilder = eventBuildingMockUtil.getConfigBuilder();
    private final EventTypeBuilder<CaseData, UserRole, State> eventTypeBuilder = eventBuildingMockUtil.getEventTypeBuilder();
    private final Event.EventBuilder<CaseData, UserRole, State> eventBuilder = eventBuildingMockUtil.getEventBuilder();
    private final FieldCollection.FieldCollectionBuilder<CaseData, Event.EventBuilder<CaseData, UserRole, State>>
        fieldCollectionBuilder = eventBuildingMockUtil.getFieldCollectionBuilder();

    @Test
    public void shouldBuildDraftCreateEventWithConfigBuilder() {

        draftCreate.applyTo(configBuilder);

        verify(configBuilder).event(DRAFT_CREATE.name);
        verify(eventTypeBuilder).initialState(Draft);
        verify(eventBuilder).name("Create draft case");
        verify(eventBuilder).description("Apply for a divorce or dissolution");
        verify(eventBuilder).displayOrder(1);
        verify(eventBuilder).retries(120, 120);
        verify(eventBuilder, times(1)).fields();
        verify(fieldCollectionBuilder, times(1)).mandatory(any());

        verifyNoMoreInteractions(configBuilder, eventTypeBuilder, eventBuilder, fieldCollectionBuilder);
    }
}
