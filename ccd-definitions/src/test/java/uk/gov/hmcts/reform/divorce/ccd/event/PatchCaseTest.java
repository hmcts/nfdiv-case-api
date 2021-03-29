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
import static uk.gov.hmcts.reform.divorce.ccd.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.reform.divorce.ccd.Permissions.READ;
import static uk.gov.hmcts.reform.divorce.ccd.event.PatchCase.PATCH_CASE;
import static uk.gov.hmcts.reform.divorce.ccd.model.State.Draft;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CITIZEN;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class PatchCaseTest {

    private final PatchCase patchCase = new PatchCase();

    private final EventBuildingMockUtil eventBuildingMockUtil = new EventBuildingMockUtil().mockEventBuilding();
    private final ConfigBuilder<CaseData, State, UserRole> configBuilder = eventBuildingMockUtil.getConfigBuilder();
    private final EventTypeBuilder<CaseData, UserRole, State> eventTypeBuilder = eventBuildingMockUtil.getEventTypeBuilder();
    private final Event.EventBuilder<CaseData, UserRole, State> eventBuilder = eventBuildingMockUtil.getEventBuilder();
    private final FieldCollection.FieldCollectionBuilder<CaseData, Event.EventBuilder<CaseData, UserRole, State>>
        fieldCollectionBuilder = eventBuildingMockUtil.getFieldCollectionBuilder();

    @Test
    void shouldBuildPatchCaseEventWithConfigBuilder() {

        patchCase.applyTo(configBuilder);

        verify(configBuilder).event(PATCH_CASE);
        verify(eventTypeBuilder).forState(Draft);
        verify(eventBuilder).name("Patch case");
        verify(eventBuilder).description("Patch a divorce or dissolution");
        verify(eventBuilder).displayOrder(2);
        verify(eventBuilder).retries(120, 120);
        verify(eventBuilder).grant(CREATE_READ_UPDATE, CITIZEN);
        verify(eventBuilder).grant(READ,
            CASEWORKER_DIVORCE_COURTADMIN_BETA,
            CASEWORKER_DIVORCE_COURTADMIN,
            CASEWORKER_DIVORCE_SOLICITOR,
            CASEWORKER_DIVORCE_SUPERUSER,
            CASEWORKER_DIVORCE_COURTADMIN_LA);
        verify(eventBuilder, times(1)).fields();
        verify(fieldCollectionBuilder, times(37)).optional(any());

        verifyNoMoreInteractions(configBuilder, eventTypeBuilder, eventBuilder, fieldCollectionBuilder);
    }
}
