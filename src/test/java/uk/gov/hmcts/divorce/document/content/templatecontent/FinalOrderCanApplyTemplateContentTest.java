package uk.gov.hmcts.divorce.document.content.templatecontent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.payment.PaymentService;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_CAN_APPLY_RESPONDENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_CAN_APPLY_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.FINAL_ORDER_FEE;
import static uk.gov.hmcts.divorce.notification.CommonContent.FINAL_ORDER_OVERDUE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_NOTICE;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_OTHER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContent;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class FinalOrderCanApplyTemplateContentTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private FinalOrderCanApplyTemplateContent finalOrderCanApplyTemplateContent;



    @Test
    void testGetSupportedTemplates() {
        assertIterableEquals(finalOrderCanApplyTemplateContent.getSupportedTemplates(), List.of(FINAL_ORDER_CAN_APPLY_TEMPLATE_ID,
            FINAL_ORDER_CAN_APPLY_RESPONDENT_TEMPLATE_ID));
    }

    @Test
    void testGetTemplateContent() {

        CaseData caseData = validApplicant1CaseData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.NO);
        caseData.getFinalOrder().setDateFinalOrderEligibleFrom(LocalDate.now());
        caseData.getApplicant1().setAddress(AddressGlobalUK.builder().addressLine1("test value").build());

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
                caseData.getApplicant1().getLanguagePreference())).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        when(commonContent.getPartner(caseData, caseData.getApplicant2(), caseData.getApplicant1()
                .getLanguagePreference())).thenReturn("husband");

        when(paymentService.getServiceCost(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_NOTICE)).thenReturn(180.0);

        Map<String, Object> expectedEntries = new LinkedHashMap<>();

        expectedEntries.put(DATE, LocalDate.now().format(DATE_TIME_FORMATTER));
        expectedEntries.put(FIRST_NAME, TEST_FIRST_NAME);
        expectedEntries.put(LAST_NAME, TEST_LAST_NAME);
        expectedEntries.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        expectedEntries.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        expectedEntries.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        expectedEntries.put(FINAL_ORDER_OVERDUE_DATE, caseData.getFinalOrder().getDateFinalOrderEligibleFrom().plusMonths(12)
                .format(getDateTimeFormatterForPreferredLanguage(caseData.getApplicant1().getLanguagePreference())));
        expectedEntries.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        expectedEntries.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        expectedEntries.put(PARTNER, "husband");
        expectedEntries.put(ADDRESS, "test value");
        expectedEntries.put(IS_JOINT, false);
        expectedEntries.put(IS_DIVORCE, true);
        expectedEntries.put(FINAL_ORDER_FEE, "£180.00");

        Map<String, Object> templateContents = finalOrderCanApplyTemplateContent.getTemplateContent(caseData,
                TEST_CASE_ID, caseData.getApplicant1());

        assertThat(templateContents).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }
}
