package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGeneralApplicationReceived.CASEWORKER_GENERAL_APPLICATION_RECEIVED;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType.DEEMED_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralApplicationReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerGeneralApplicationReceivedTest {

    @InjectMocks
    private CaseworkerGeneralApplicationReceived generalApplicationReceived;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        generalApplicationReceived.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_GENERAL_APPLICATION_RECEIVED);
    }

    @Test
    void shouldResetGeneralApplicationWhenAboutToStartCallbackTriggered() {
        final CaseData caseData = caseData();
        caseData.setGeneralApplication(GeneralApplication.builder()
            .generalApplicationType(DEEMED_SERVICE)
            .generalApplicationTypeOtherComments("some comments")
            .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setState(Holding);
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            generalApplicationReceived.aboutToStart(details);

        assertThat(response.getData().getGeneralApplication().getGeneralApplicationType()).isNull();
        assertThat(response.getData().getGeneralApplication().getGeneralApplicationTypeOtherComments()).isNull();
        assertThat(response.getData().getGeneralApplication().getGeneralApplicationUrgentCase()).isNull();
        assertThat(response.getData().getGeneralApplication().getGeneralApplicationUrgentCaseReason()).isNull();
        assertThat(response.getData().getGeneralApplication()).isEqualTo(GeneralApplication.builder().build());
    }

    @Test
    void shouldAddGeneralApplicationDocumentToListOfCaseDocumentsAndUpdateState() {
        final DivorceDocument document = DivorceDocument.builder()
            .documentLink(Document.builder().build())
            .build();
        final CaseData caseData = caseData();
        caseData.getGeneralApplication().setGeneralApplicationDocument(document);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setState(Holding);
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            generalApplicationReceived.aboutToSubmit(details, details);

        assertThat(response.getState()).isEqualTo(GeneralApplicationReceived);
        assertThat(response.getData().getDocuments().getDocumentsUploaded().size()).isEqualTo(1);
        assertThat(response.getData().getDocuments().getDocumentsUploaded().get(0).getValue()).isEqualTo(document);
    }

    @Test
    void shouldAddGeneralApplicationToGACollection() {
        final CaseData caseData = caseData();
        caseData.setGeneralApplication(GeneralApplication.builder()
            .generalApplicationType(DEEMED_SERVICE)
            .generalApplicationTypeOtherComments("some comments")
            .build()
        );

        final DivorceDocument document = DivorceDocument.builder()
            .documentLink(Document.builder().build())
            .build();

        caseData.getGeneralApplication().setGeneralApplicationDocument(document);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setState(Holding);
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            generalApplicationReceived.aboutToSubmit(details, details);

        assertThat(response.getData().getGeneralApplications().size()).isEqualTo(1);
    }
}
