package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_RESPONSE_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_RESPONSE_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_SOLICITOR_RESPONSE_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_SOLICITOR_RESPONSE_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.REQUEST_FOR_INFORMATION_RESPONSE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.addResponseToLatestRequestForInformation;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getRequestForInformationCaseDetails;

class RequestForInformationResponseDocumentPackTest {

    private static final String LETTER_TYPE_REQUEST_FOR_INFORMATION_RESPONSE = "request-for-information-response-letter";

    private static final DocumentPackInfo APPLICANT_REQUEST_FOR_INFORMATION_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            REQUEST_FOR_INFORMATION_RESPONSE, Optional.of(REQUEST_FOR_INFORMATION_RESPONSE_LETTER_TEMPLATE_ID)
        ),
        ImmutableMap.of(
            REQUEST_FOR_INFORMATION_RESPONSE_LETTER_TEMPLATE_ID, REQUEST_FOR_INFORMATION_RESPONSE_LETTER_DOCUMENT_NAME
        )
    );

    private static final DocumentPackInfo SOLICITOR_REQUEST_FOR_INFORMATION_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            REQUEST_FOR_INFORMATION_RESPONSE, Optional.of(REQUEST_FOR_INFORMATION_SOLICITOR_RESPONSE_LETTER_TEMPLATE_ID)
        ),
        ImmutableMap.of(
            REQUEST_FOR_INFORMATION_SOLICITOR_RESPONSE_LETTER_TEMPLATE_ID, REQUEST_FOR_INFORMATION_SOLICITOR_RESPONSE_LETTER_DOCUMENT_NAME
        )
    );

    private final RequestForInformationResponseDocumentPack requestForInformationResponseDocumentPack =
        new RequestForInformationResponseDocumentPack();

    @Test
    void shouldReturnApplicantDocumentPackWhenPassedApplicant1ForSoleCase() {
        CaseData data = getRequestForInformationCaseDetails(
            RequestForInformationSoleParties.APPLICANT,
            false,
            false
        ).getData();
        addResponseToLatestRequestForInformation(data, data.getApplicant1());
        var documentPack = requestForInformationResponseDocumentPack.getDocumentPack(data, data.getApplicant1());

        assertThat(documentPack).isEqualTo(APPLICANT_REQUEST_FOR_INFORMATION_RESPONSE_PACK);
    }

    @Test
    void shouldReturnSolicitorDocumentPackWhenPassedRepresentedApplicant1ForSoleCase() {
        CaseData data = getRequestForInformationCaseDetails().getData();
        addResponseToLatestRequestForInformation(data, data.getApplicant1());
        var documentPack = requestForInformationResponseDocumentPack.getDocumentPack(data, data.getApplicant1());

        assertThat(documentPack).isEqualTo(SOLICITOR_REQUEST_FOR_INFORMATION_RESPONSE_PACK);
    }

    @Test
    void shouldReturnApplicantDocumentPackWhenPassedApplicant1ForJointCase() {
        CaseData data = getRequestForInformationCaseDetails(
            RequestForInformationJointParties.APPLICANT1,
            false,
            false
        ).getData();
        addResponseToLatestRequestForInformation(data, data.getApplicant1());
        var documentPack = requestForInformationResponseDocumentPack.getDocumentPack(data, data.getApplicant1());

        assertThat(documentPack).isEqualTo(APPLICANT_REQUEST_FOR_INFORMATION_RESPONSE_PACK);
    }

    @Test
    void shouldReturnSolicitorDocumentPackWhenPassedRepresentedApplicant1ForJointCase() {
        CaseData data = getRequestForInformationCaseDetails(
            RequestForInformationJointParties.APPLICANT1,
            true,
            false
        ).getData();
        addResponseToLatestRequestForInformation(data, data.getApplicant1());
        var documentPack = requestForInformationResponseDocumentPack.getDocumentPack(data, data.getApplicant1());

        assertThat(documentPack).isEqualTo(SOLICITOR_REQUEST_FOR_INFORMATION_RESPONSE_PACK);
    }

    @Test
    void shouldReturnApplicantDocumentPackWhenPassedApplicant2() {
        CaseData data = getRequestForInformationCaseDetails(
            RequestForInformationJointParties.APPLICANT2,
            false,
            false
        ).getData();
        addResponseToLatestRequestForInformation(data, data.getApplicant2());
        var documentPack = requestForInformationResponseDocumentPack.getDocumentPack(data, data.getApplicant2());

        assertThat(documentPack).isEqualTo(APPLICANT_REQUEST_FOR_INFORMATION_RESPONSE_PACK);
    }

    @Test
    void shouldReturnSolicitorDocumentPackWhenPassedRepresentedApplicant2() {
        CaseData data = getRequestForInformationCaseDetails(
            RequestForInformationJointParties.APPLICANT2,
            false,
            true
        ).getData();
        addResponseToLatestRequestForInformation(data, data.getApplicant2());
        var documentPack = requestForInformationResponseDocumentPack.getDocumentPack(data, data.getApplicant2());

        assertThat(documentPack).isEqualTo(SOLICITOR_REQUEST_FOR_INFORMATION_RESPONSE_PACK);
    }

    @Test
    void shouldReturnApplicantDocumentPackWhenPassedApplicant1ForBothPartiesRfi() {
        CaseData data = getRequestForInformationCaseDetails(
            RequestForInformationJointParties.BOTH,
            false,
            false
        ).getData();
        addResponseToLatestRequestForInformation(data, data.getApplicant1());
        var documentPack = requestForInformationResponseDocumentPack.getDocumentPack(data, data.getApplicant1());

        assertThat(documentPack).isEqualTo(APPLICANT_REQUEST_FOR_INFORMATION_RESPONSE_PACK);
    }

    @Test
    void shouldReturnSolicitorDocumentPackWhenPassedRepresentedApplicant1ForBothPartiesRfi() {
        CaseData data = getRequestForInformationCaseDetails(
            RequestForInformationJointParties.BOTH,
            true,
            false
        ).getData();
        addResponseToLatestRequestForInformation(data, data.getApplicant1());
        var documentPack = requestForInformationResponseDocumentPack.getDocumentPack(data, data.getApplicant1());

        assertThat(documentPack).isEqualTo(SOLICITOR_REQUEST_FOR_INFORMATION_RESPONSE_PACK);
    }

    @Test
    void shouldReturnApplicantDocumentPackWhenPassedApplicant2ForBothPartiesRfi() {
        CaseData data = getRequestForInformationCaseDetails(
            RequestForInformationJointParties.BOTH,
            false,
            false
        ).getData();
        addResponseToLatestRequestForInformation(data, data.getApplicant2());
        var documentPack = requestForInformationResponseDocumentPack.getDocumentPack(data, data.getApplicant2());

        assertThat(documentPack).isEqualTo(APPLICANT_REQUEST_FOR_INFORMATION_RESPONSE_PACK);
    }

    @Test
    void shouldReturnSolicitorDocumentPackWhenPassedRepresentedApplicant2ForBothPartiesRfi() {
        CaseData data = getRequestForInformationCaseDetails(
            RequestForInformationJointParties.BOTH,
            false,
            true
        ).getData();
        addResponseToLatestRequestForInformation(data, data.getApplicant2());
        var documentPack = requestForInformationResponseDocumentPack.getDocumentPack(data, data.getApplicant2());

        assertThat(documentPack).isEqualTo(SOLICITOR_REQUEST_FOR_INFORMATION_RESPONSE_PACK);
    }

    @Test
    void shouldReturnCorrectLetterId() {
        assertThat(requestForInformationResponseDocumentPack.getLetterId()).isEqualTo(LETTER_TYPE_REQUEST_FOR_INFORMATION_RESPONSE);
    }
}
