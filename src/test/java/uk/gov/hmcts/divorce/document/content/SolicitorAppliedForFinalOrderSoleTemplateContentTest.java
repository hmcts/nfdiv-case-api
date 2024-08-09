package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.testutil.TestDataHelper;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_APP1_SOLICITOR_APPLIED_FOR_FINAL_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContentWithCtscContactDetails;

@ExtendWith(MockitoExtension.class)
class SolicitorAppliedForFinalOrderSoleTemplateContentTest {

    @Mock
    private Clock clock;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private SolicitorAppliedForFinalOrderSoleTemplateContent solicitorAppliedForFinalOrderSoleTemplateContent;

    private static final CtscContactDetails CTSC_CONTACT = CtscContactDetails
            .builder()
            .centreName("HMCTS Digital Divorce and Dissolution")
            .serviceCentre("Courts and Tribunals Service Centre")
            .emailAddress("contactdivorce@justice.gov.uk")
            .poBox("PO Box 13226")
            .town("Harlow")
            .postcode("CM20 9UG")
            .phoneNumber("0300 303 0642")
            .build();

    @Test
    void shouldBeAbleToHandleLitigantGrantOfRepresentationConfirmationTemplate() {
        assertThat(solicitorAppliedForFinalOrderSoleTemplateContent.getSupportedTemplates())
                .containsAll(List.of(NFD_APP1_SOLICITOR_APPLIED_FOR_FINAL_ORDER_TEMPLATE_ID));
    }

    @Test
    void shouldMapTemplateContentWhenRecipientIsApplicant1() {
        setMockClock(clock, LocalDate.of(2022, 3, 16));
        var caseData = TestDataHelper.caseData();
        caseData.getApplicant1().setSolicitor(Solicitor.builder().address("123 test address").build());

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
                .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        final Map<String, Object> templateContent = solicitorAppliedForFinalOrderSoleTemplateContent
                .getTemplateContent(caseData, TEST_CASE_ID, caseData.getApplicant1());

        assertThat(templateContent).containsAnyOf(
                entry(ISSUE_DATE, "16 March 2022"),
                entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
                entry(APPLICANT_1_FULL_NAME, String.join(" ", TEST_FIRST_NAME, TEST_MIDDLE_NAME, TEST_LAST_NAME)),
                entry(APPLICANT_2_FULL_NAME, String.join(" ", TEST_FIRST_NAME, TEST_MIDDLE_NAME, TEST_LAST_NAME)),
                entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT),
                entry(SOLICITOR_ADDRESS, caseData.getApplicant1().getSolicitor()
                        .getAddress())
        );
    }
}
