package uk.gov.hmcts.divorce.solicitor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralApplicationReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorGeneralApplication.SOLICITOR_GENERAL_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class SolicitorGeneralApplicationTest {

    @InjectMocks
    private SolicitorGeneralApplication solicitorGeneralApplication;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorGeneralApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_GENERAL_APPLICATION);
    }

    @Test
    void shouldAddGeneralApplicationDocumentToListOfCaseDocumentsAndUpdateState() {
        final DivorceDocument document = mock(DivorceDocument.class);
        final CaseData caseData = caseData();
        caseData.getGeneralReferral().setGeneralApplicationDocument(document);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(1L);
        details.setState(Holding);
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorGeneralApplication.aboutToSubmit(details, details);

        assertThat(response.getState()).isEqualTo(GeneralApplicationReceived);
        assertThat(response.getData().getDocumentsUploaded().size()).isEqualTo(1);
        assertThat(response.getData().getDocumentsUploaded().get(0).getValue()).isEqualTo(document);
    }

    @Test
    void shouldReturnErrorsIfCaseIsCurrentlyLinkedToActiveBulkCase() {
        final CaseData caseData = caseData();
        caseData.setBulkListCaseReference("1234");

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(1L);
        details.setState(AwaitingPronouncement);
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorGeneralApplication.aboutToSubmit(details, details);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors())
            .contains("General Application cannot be submitted as this case is currently linked to an active bulk action case");
    }
}
