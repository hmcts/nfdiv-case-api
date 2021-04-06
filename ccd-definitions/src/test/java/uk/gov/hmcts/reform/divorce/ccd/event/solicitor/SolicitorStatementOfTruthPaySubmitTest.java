package uk.gov.hmcts.reform.divorce.ccd.event.solicitor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.EventTypeBuilder;
import uk.gov.hmcts.reform.divorce.ccd.mock.EventBuildingMockUtil;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.ccd.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.reform.divorce.ccd.access.Permissions.READ;
import static uk.gov.hmcts.reform.divorce.ccd.event.solicitor.SolicitorStatementOfTruthPaySubmit.SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT;
import static uk.gov.hmcts.reform.divorce.ccd.model.State.SOTAgreementPayAndSubmitRequired;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
class SolicitorStatementOfTruthPaySubmitTest {

    private final SolicitorStatementOfTruthPaySubmit solicitorStatementOfTruthPaySubmit =
        new SolicitorStatementOfTruthPaySubmit();

    private final EventBuildingMockUtil eventBuildingMockUtil = new EventBuildingMockUtil().mockEventBuilding();
    private final ConfigBuilder<CaseData, State, UserRole> configBuilder = eventBuildingMockUtil.getConfigBuilder();
    private final EventTypeBuilder<CaseData, UserRole, State> eventTypeBuilder = eventBuildingMockUtil.getEventTypeBuilder();
    private final Event.EventBuilder<CaseData, UserRole, State> eventBuilder = eventBuildingMockUtil.getEventBuilder();

    @Test
    void shouldBuildSolicitorCreateEventWithConfigBuilder() {

        solicitorStatementOfTruthPaySubmit.applyTo(configBuilder);

        verify(configBuilder).event(SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT);
        verify(eventTypeBuilder).forStates(SOTAgreementPayAndSubmitRequired);
        verify(eventBuilder).name("Case submission");
        verify(eventBuilder).description("Agree Statement of Truth, Pay & Submit");
        verify(eventBuilder).displayOrder(1);
        verify(eventBuilder).showSummary();
        verify(eventBuilder).endButtonLabel("Submit Petition");
        verify(eventBuilder).grant(CREATE_READ_UPDATE, CASEWORKER_DIVORCE_SOLICITOR);
        verify(eventBuilder).grant(READ,
            CASEWORKER_DIVORCE_COURTADMIN_BETA,
            CASEWORKER_DIVORCE_COURTADMIN,
            CASEWORKER_DIVORCE_SUPERUSER,
            CASEWORKER_DIVORCE_COURTADMIN_LA);
        verify(eventBuilder).aboutToStartWebhook("submit-petition");
    }
}
