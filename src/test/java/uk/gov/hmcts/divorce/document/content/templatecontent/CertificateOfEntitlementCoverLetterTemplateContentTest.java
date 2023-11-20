package uk.gov.hmcts.divorce.document.content.templatecontent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateCertificateOfEntitlementHelper;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class CertificateOfEntitlementCoverLetterTemplateContentTest {

    private static final String NAME = "name";
    private static final String CASE_REFERENCE = "caseReference";
    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @Mock
    private GenerateCertificateOfEntitlementHelper generateCertificateOfEntitlementHelper;

    @Mock
    private Clock clock;

    @InjectMocks
    private CertificateOfEntitlementCoverLetterTemplateContent certificateOfEntitlementCoverLetterTemplateContent;

    @Test
    void shouldBeAbleToHandleCertificateOfEntitlementCoverLetterTemplates() {
        assertThat(certificateOfEntitlementCoverLetterTemplateContent.getSupportedTemplates())
                .containsAll(List.of(CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID,
                CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID,
                CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID));
    }

    @Test
    void shouldProvideCorrectTemplateContentForCertificateOfEntitlementCoverLetter() {
        CaseData caseData = validApplicant1CaseData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.NO);
        setMockClock(clock);

        Map<String, Object> templateContents = new HashMap<>();

        templateContents.put(NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        templateContents.put(ADDRESS, caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck());
        templateContents.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateContents.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateContents.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);

        when(generateCertificateOfEntitlementHelper.getTemplateContent(caseData, TEST_CASE_ID, caseData.getApplicant1(),
                caseData.getApplicant2())).thenReturn(templateContents);

        final var templateContent = certificateOfEntitlementCoverLetterTemplateContent.getTemplateContent(
                caseData,
                TEST_CASE_ID,
                caseData.getApplicant1());

        assertThat(templateContent).containsAllEntriesOf(
                Map.of(
                        NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME,
                        DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT,
                        CASE_REFERENCE, formatId(TEST_CASE_ID)
                )
        );
    }
}