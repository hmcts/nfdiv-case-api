package uk.gov.hmcts.divorce.solicitor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DraftApplicationAction;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.DraftApplicationAction.AMEND;
import static uk.gov.hmcts.divorce.divorcecase.model.DraftApplicationAction.WITHDRAW;
import static uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType.SOLICITOR_D8B_ANSWERS;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorAmendD8bAnswers.SOLICITOR_AMEND_D8B_ANSWERS;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondentWithDigitalSolicitor;

@ExtendWith(MockitoExtension.class)
class SolicitorAmendD8bAnswersTest {

    @InjectMocks
    private SolicitorAmendD8bAnswers solicitorAmendD8bAnswers;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorAmendD8bAnswers.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_AMEND_D8B_ANSWERS);
    }

    @Test
    void shouldResetApplicationD8bAnswerOptionsIfActionIsAmendAboutToSubmitCallback() {

        CaseData caseData = buildCaseData(WITHDRAW);

        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData).build();

        var response =  solicitorAmendD8bAnswers.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getGeneralApplication().getSolGeneralApplicationD8bDocuments()).isNull();
        assertThat(response.getData().getApplicant2().getInterimApplicationOptions().getInterimAppsPaymentMethod()).isNull();
        assertThat(response.getData().getApplicant2().getInterimApplicationOptions().getInterimApplicationType()).isNull();
    }

    @Test
    void shouldNotResetApplicationD8bAnswerOptionsIfActionIsAmendAboutToSubmitCallback() {

        CaseData caseData = buildCaseData(AMEND);

        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData).build();

        var response =  solicitorAmendD8bAnswers.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getGeneralApplication().getSolGeneralApplicationD8bDocuments()).isNotNull();
        assertThat(response.getData().getApplicant2().getInterimApplicationOptions().getInterimAppsPaymentMethod()).isNotNull();
        assertThat(response.getData().getApplicant2().getInterimApplicationOptions().getInterimApplicationType()).isNotNull();

    }

    private CaseData buildCaseData(DraftApplicationAction draftApplicationAction) {
        Applicant respondent = respondentWithDigitalSolicitor();
        respondent.setInterimApplicationOptions(InterimApplicationOptions.builder()
            .interimAppsPaymentMethod(FEE_PAY_BY_ACCOUNT)
            .draftApplicationAction(draftApplicationAction)
            .interimApplicationType(SOLICITOR_D8B_ANSWERS).build());

        final ListValue<DivorceDocument> documentListValue = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        return CaseData.builder().generalApplication(GeneralApplication.builder()
            .solGeneralApplicationD8bDocuments(List.of(documentListValue)).build()).applicant2(respondent).build();
    }
}
