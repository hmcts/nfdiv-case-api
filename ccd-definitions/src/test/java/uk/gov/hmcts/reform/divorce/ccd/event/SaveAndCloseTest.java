package uk.gov.hmcts.reform.divorce.ccd.event;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.EventTypeBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.reform.divorce.ccd.mock.EventBuildingMockUtil;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.divorce.ccd.event.SaveAndClose.SAVE_AND_CLOSE;
import static uk.gov.hmcts.reform.divorce.ccd.model.State.Draft;

public class SaveAndCloseTest {

    private final SaveAndClose saveAndClose = new SaveAndClose();

    private final EventBuildingMockUtil eventBuildingMockUtil = new EventBuildingMockUtil().mockEventBuilding();
    private final ConfigBuilder<CaseData, State, UserRole> configBuilder = eventBuildingMockUtil.getConfigBuilder();
    private final EventTypeBuilder<CaseData, UserRole, State> eventTypeBuilder = eventBuildingMockUtil.getEventTypeBuilder();
    private final Event.EventBuilder<CaseData, UserRole, State> eventBuilder = eventBuildingMockUtil.getEventBuilder();
    private final FieldCollection.FieldCollectionBuilder<CaseData, Event.EventBuilder<CaseData, UserRole, State>>
        fieldCollectionBuilder = eventBuildingMockUtil.getFieldCollectionBuilder();

    @Test
    void shouldBuildPatchCaseEventWithConfigBuilder() {

        saveAndClose.applyTo(configBuilder);

        verify(configBuilder).event(SAVE_AND_CLOSE);
        verify(eventTypeBuilder).initialState(Draft);
        verify(eventBuilder).name("Save and close application");
        verify(eventBuilder).description("Save application and send email notification to petitioner");
        verify(eventBuilder).displayOrder(1);
        verify(eventBuilder).retries(120, 120);
        verify(eventBuilder).aboutToSubmitWebhook(SAVE_AND_CLOSE);
        verify(eventBuilder, times(0)).fields();

        verifyNoMoreInteractions(configBuilder, eventTypeBuilder, eventBuilder, fieldCollectionBuilder);
    }
}
