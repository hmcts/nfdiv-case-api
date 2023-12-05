package uk.gov.hmcts.divorce.document.print.documentpack;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JS_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

class CertificateOfEntitlementDocumentPackTest {
    private CertificateOfEntitlementDocumentPack certificateOfEntitlementDocumentPack =
            new CertificateOfEntitlementDocumentPack();

    @Test
     void shouldReturnApplicant1DocumentPackWhenPassedApplicant1NonJS() {
        CaseData data = validApplicant1CaseData();
        assertDocumentPackEntries(data, data.getApplicant1(), CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1,
                CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID);
    }

    @Test
    void shouldReturnApplicant2DocumentPackWhenPassedApplicant2NonJS() {
        CaseData data = validApplicant2CaseData();
        data.setApplicationType(ApplicationType.JOINT_APPLICATION);
        assertDocumentPackEntries(data, data.getApplicant2(), CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2,
                CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID);
    }

    @Test
    void shouldReturnApplicantRespondentDocumentPackWhenPassedRespondent() {
        CaseData data = validApplicant2CaseData();
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);
        assertDocumentPackEntries(data, data.getApplicant2(), CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2,
                CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID);
    }

    @Test
    void shouldReturnApplicant1DocumentPackWhenPassedApplicant1JudicialSeparation() {
        CaseData data = validApplicant1CaseData();
        data.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        assertDocumentPackEntries(data, data.getApplicant1(), CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1,
                CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID);
    }

    @Test
    void shouldReturnApplicant2DocumentPackWhenPassedApplicant2JudicialSeparation() {
        CaseData data = validApplicant2CaseData();
        data.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        assertDocumentPackEntries(data, data.getApplicant2(), CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2,
                CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID);
    }

    @Test
    void shouldReturnApplicant1RepresentedDocumentPackWhenPassedApplicant1RepresentedJudicialSeparation() {
        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setSolicitorRepresented(YES);
        data.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        assertDocumentPackEntries(data, data.getApplicant1(), CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1,
                CERTIFICATE_OF_ENTITLEMENT_JS_SOLICITOR_COVER_LETTER_TEMPLATE_ID);
    }

    @Test
    void shouldReturnApplicant2RepresentedDocumentPackWhenPassedApplicant2RepresentedJudicialSeparation() {
        CaseData data = validApplicant2CaseData();
        data.getApplicant2().setSolicitorRepresented(YES);
        data.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        assertDocumentPackEntries(data, data.getApplicant2(),
                CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2,
                CERTIFICATE_OF_ENTITLEMENT_JS_SOLICITOR_COVER_LETTER_TEMPLATE_ID);
    }


    private void assertDocumentPackEntries(CaseData caseData, Applicant applicant, DocumentType documentType,
                                           String templateId) {
        var documentPack = certificateOfEntitlementDocumentPack.getDocumentPack(caseData, applicant);

        assertThat(documentPack.documentPack()).hasSize(2);
        assertThat(documentPack.documentPack()).containsEntry(documentType,
                Optional.of(templateId));
        assertThat(documentPack.documentPack()).containsEntry(CERTIFICATE_OF_ENTITLEMENT, Optional.empty());
        assertThat(documentPack.templateInfo()).containsEntry(templateId, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME);
    }
}