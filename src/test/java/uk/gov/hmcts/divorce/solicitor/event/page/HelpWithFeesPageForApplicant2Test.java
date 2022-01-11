package uk.gov.hmcts.divorce.solicitor.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.AMENDED_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.documentWithType;

@ExtendWith(MockitoExtension.class)
class HelpWithFeesPageForApplicant2Test {

    @InjectMocks
    private HelpWithFeesPageForApplicant2 page;

    @Test
    public void shouldSetApplicant1SolicitorAnswersLinkWhenDraftApplicationDocumentIsInDocumentsGenerated() {
        final CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.setApplicationType(JOINT_APPLICATION);

        final var documentListValue = documentWithType(APPLICATION);
        final var generatedDocuments = singletonList(documentListValue);
        caseData.setDocumentsGenerated(generatedDocuments);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getData().getApplication().getApplicant1SolicitorAnswersLink())
            .isEqualTo(documentListValue.getValue().getDocumentLink());
    }

    @Test
    public void shouldNotSetApplicant1SolicitorAnswersLinkWhenDraftApplicationDocumentIsInDocumentsGenerated() {
        final CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.setApplicationType(JOINT_APPLICATION);

        final var documentListValue = documentWithType(AMENDED_APPLICATION);
        final var generatedDocuments = singletonList(documentListValue);
        caseData.setDocumentsGenerated(generatedDocuments);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getData().getApplication().getApplicant1SolicitorAnswersLink()).isNull();
    }
}
