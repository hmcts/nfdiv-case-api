package uk.gov.hmcts.divorce.document.content.templatecontent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.ConditionalOrderCourtDetailsConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JS_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JUDICIAL_SEPARATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_FIRM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_FIRM_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getOfflineSolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class CertificateOfEntitlementTemplateContentTest {

    private static final String NAME = "name";
    public static final String CASE_REFERENCE = "caseReference";

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @Mock
    private ConditionalOrderCourtDetailsConfig conditionalOrderCourtDetailsConfig;

    @InjectMocks
    private CertificateOfEntitlementTemplateContent certificateOfEntitlementTemplateContent;


    @Test
    void shouldBeAbleToHandleCertificateOfEntitlementTemplates() {
        assertThat(certificateOfEntitlementTemplateContent.getSupportedTemplates())
                .containsAll(List.of(CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID,
                CERTIFICATE_OF_ENTITLEMENT_JUDICIAL_SEPARATION_TEMPLATE_ID,
                CERTIFICATE_OF_ENTITLEMENT_JS_SOLICITOR_COVER_LETTER_TEMPLATE_ID));
    }

    @Test
    void shouldProvideCorrectTemplateContentForCertificateOfEntitlementCoverLetter() {
        CaseData caseData = validApplicant1CaseData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.NO);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
                .thenReturn(new HashMap<>(Map.of(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT)));

        final var templateContent = certificateOfEntitlementTemplateContent.getTemplateContent(
                caseData,
                TEST_CASE_ID,
                caseData.getApplicant1());

        assertThat(templateContent).containsAllEntriesOf(
                Map.of(
                        APPLICANT_1_FULL_NAME, TEST_FIRST_NAME + " " + TEST_MIDDLE_NAME + " " + TEST_LAST_NAME,
                        DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT,
                        CCD_CASE_REFERENCE, formatId(TEST_CASE_ID)
                )
        );
    }

    @Test
    void shouldProvideCorrectTemplateContentForRepresentedCertificateOfEntitlementCoverLetter() {
        CaseData caseData = validApplicant1CaseData();
        caseData.getApplicant1().setSolicitor(getOfflineSolicitor());
        caseData.getApplicant1().getSolicitor().setReference(TEST_REFERENCE);
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.NO);
        caseData.getApplicant2().setSolicitor(getOfflineSolicitor());
        caseData.getApplicant2().getSolicitor().setReference(TEST_REFERENCE);
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.NO);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
            .thenReturn(new HashMap<>(Map.of(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT)));

        final var templateContent = certificateOfEntitlementTemplateContent.getTemplateContent(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1());

        assertThat(templateContent).containsAllEntriesOf(
            Map.of(
                APPLICANT_1_FULL_NAME, TEST_FIRST_NAME + " " + TEST_MIDDLE_NAME + " " + TEST_LAST_NAME,
                DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT,
                CCD_CASE_REFERENCE, formatId(TEST_CASE_ID),
                APPLICANT_1_SOLICITOR_NAME, TEST_SOLICITOR_NAME,
                APPLICANT_2_SOLICITOR_NAME, TEST_SOLICITOR_NAME,
                SOLICITOR_NAME, TEST_SOLICITOR_NAME,
                SOLICITOR_FIRM, TEST_SOLICITOR_FIRM_NAME,
                SOLICITOR_ADDRESS, "Address(addressLine1=sol line1, addressLine2=sol line2, addressLine3=null, postTown=sol city, "
                    + "county=null, postCode=sol postcode, country=null)",
                SOLICITOR_REFERENCE, TEST_REFERENCE
            )
        );
    }
}
