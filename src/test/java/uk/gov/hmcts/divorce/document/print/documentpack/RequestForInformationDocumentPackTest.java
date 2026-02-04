package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_SOLICITOR_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_SOLICITOR_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.REQUEST_FOR_INFORMATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

class RequestForInformationDocumentPackTest {

    private static final String LETTER_TYPE_REQUEST_FOR_INFORMATION = "request-for-information-letter";

    private static final DocumentPackInfo APPLICANT_REQUEST_FOR_INFORMATION_PACK = new DocumentPackInfo(
            ImmutableMap.of(
                    REQUEST_FOR_INFORMATION, Optional.of(REQUEST_FOR_INFORMATION_LETTER_TEMPLATE_ID)
            ),
            ImmutableMap.of(
                    REQUEST_FOR_INFORMATION_LETTER_TEMPLATE_ID, REQUEST_FOR_INFORMATION_LETTER_DOCUMENT_NAME
            )
    );

    private static final DocumentPackInfo SOLICITOR_REQUEST_FOR_INFORMATION_PACK = new DocumentPackInfo(
            ImmutableMap.of(
                    REQUEST_FOR_INFORMATION, Optional.of(REQUEST_FOR_INFORMATION_SOLICITOR_LETTER_TEMPLATE_ID)
            ),
            ImmutableMap.of(
                    REQUEST_FOR_INFORMATION_SOLICITOR_LETTER_TEMPLATE_ID, REQUEST_FOR_INFORMATION_SOLICITOR_LETTER_DOCUMENT_NAME
            )
    );

    private final RequestForInformationDocumentPack requestForInformationDocumentPack = new RequestForInformationDocumentPack();

    @Test
    void shouldReturnApplicantDocumentPackWhenPassedApplicant1() {
        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setSolicitorRepresented(NO);
        var documentPack = requestForInformationDocumentPack.getDocumentPack(data, data.getApplicant1());

        assertThat(documentPack).isEqualTo(APPLICANT_REQUEST_FOR_INFORMATION_PACK);
    }

    @Test
    void shouldReturnSolicitorDocumentPackWhenPassedRepresentedApplicant1() {
        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setSolicitorRepresented(YES);
        var documentPack = requestForInformationDocumentPack.getDocumentPack(data, data.getApplicant1());

        assertThat(documentPack).isEqualTo(SOLICITOR_REQUEST_FOR_INFORMATION_PACK);
    }

    @Test
    void shouldReturnApplicantDocumentPackWhenPassedApplicant2() {
        CaseData data = validApplicant2CaseData();
        data.getApplicant1().setSolicitorRepresented(NO);
        var documentPack = requestForInformationDocumentPack.getDocumentPack(data, data.getApplicant2());

        assertThat(documentPack).isEqualTo(APPLICANT_REQUEST_FOR_INFORMATION_PACK);
    }

    @Test
    void shouldReturnSolicitorDocumentPackWhenPassedRepresentedApplicant2() {
        CaseData data = validApplicant2CaseData();
        data.getApplicant2().setSolicitorRepresented(YES);
        var documentPack = requestForInformationDocumentPack.getDocumentPack(data, data.getApplicant2());

        assertThat(documentPack).isEqualTo(SOLICITOR_REQUEST_FOR_INFORMATION_PACK);
    }

    @Test
    void shouldReturnCorrectLetterId() {
        assertThat(requestForInformationDocumentPack.getLetterId()).isEqualTo(LETTER_TYPE_REQUEST_FOR_INFORMATION);
    }
}
