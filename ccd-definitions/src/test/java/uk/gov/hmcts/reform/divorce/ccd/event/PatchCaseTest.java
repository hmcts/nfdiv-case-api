package uk.gov.hmcts.reform.divorce.ccd.event;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.types.Event;
import uk.gov.hmcts.ccd.sdk.types.EventTypeBuilder;
import uk.gov.hmcts.ccd.sdk.types.FieldCollection;
import uk.gov.hmcts.reform.divorce.ccd.mock.EventBuildingMockUtil;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.divorce.ccd.model.CaseEvent.PATCH_CASE;
import static uk.gov.hmcts.reform.divorce.ccd.model.State.Draft;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CITIZEN;

@SuppressWarnings("unchecked")
public class PatchCaseTest {

    private final PatchCase patchCase = new PatchCase();

    private final EventBuildingMockUtil eventBuildingMockUtil = new EventBuildingMockUtil().mockEventBuilding();
    private final ConfigBuilder<CaseData, State, UserRole> configBuilder = eventBuildingMockUtil.getConfigBuilder();
    private final EventTypeBuilder<CaseData, UserRole, State> eventTypeBuilder = eventBuildingMockUtil.getEventTypeBuilder();
    private final Event.EventBuilder<CaseData, UserRole, State> eventBuilder = eventBuildingMockUtil.getEventBuilder();
    private final FieldCollection.FieldCollectionBuilder<CaseData, Event.EventBuilder<CaseData, UserRole, State>>
        fieldCollectionBuilder = eventBuildingMockUtil.getFieldCollectionBuilder();

    @Test
    public void shouldBuildEventWithConfigBuilder() {

        patchCase.buildWith(configBuilder);

        verify(configBuilder).event(PATCH_CASE.name);
        verify(eventTypeBuilder).forState(Draft);
        verify(eventBuilder).name("Patch case");
        verify(eventBuilder).description("Patch a divorce or dissolution");
        verify(eventBuilder).displayOrder(2);
        verify(eventBuilder).retries(120, 120);
        verify(eventBuilder).grant("CRU", CITIZEN);
        verify(eventBuilder).grant("R",
            CASEWORKER_DIVORCE_COURTADMIN_BETA,
            CASEWORKER_DIVORCE_COURTADMIN,
            CASEWORKER_DIVORCE_SOLICITOR,
            CASEWORKER_DIVORCE_SUPERUSER,
            CASEWORKER_DIVORCE_COURTADMIN_LA);
        verify(eventBuilder, times(1)).fields();
        verify(fieldCollectionBuilder, times(13)).optional(any());

        verifyNoMoreInteractions(configBuilder, eventTypeBuilder, eventBuilder, fieldCollectionBuilder);
    }
}