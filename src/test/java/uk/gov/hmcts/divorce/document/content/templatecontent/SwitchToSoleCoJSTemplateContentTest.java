package uk.gov.hmcts.divorce.document.content.templatecontent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;

@ExtendWith(MockitoExtension.class)
class SwitchToSoleCoJSTemplateContentTest {

    @Mock
    private Clock clock;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private SwitchToSoleCoJSTemplateContent switchToSoleCoJSTemplateContent;

    @Test
    void shouldApplyContentFromCaseDataForSwitchToSoleJSConditionalOrder() {
        setMockClock(clock);

        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .languagePreferenceWelsh(YesOrNo.NO)
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(APPLICANT_2_FIRST_NAME)
            .lastName(APPLICANT_2_LAST_NAME)
            .gender(Gender.FEMALE)
            .languagePreferenceWelsh(YesOrNo.NO)
            .address(AddressGlobalUK.builder()
                .addressLine1("223b")
                .addressLine2("Baker Street")
                .postTown("Tampa")
                .county("Florida")
                .country("United States")
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
            .thenReturn(new HashMap<>(Map.of(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT)));
        when(commonContent.getPartner(caseData, caseData.getApplicant1(), caseData.getApplicant2().getLanguagePreference()))
            .thenReturn("wife");

        final Map<String, Object> result = switchToSoleCoJSTemplateContent.getTemplateContent(caseData, TEST_CASE_ID, null);

        assertThat(result).contains(
            entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(IS_DIVORCE, true),
            entry(FIRST_NAME, APPLICANT_2_FIRST_NAME),
            entry(LAST_NAME, APPLICANT_2_LAST_NAME),
            entry(ADDRESS, applicant2.getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(PARTNER, "wife"),
            entry(DATE, LocalDate.now().format(DATE_TIME_FORMATTER))
        );
    }

    @Test
    void shouldApplyContentFromCaseDataForSwitchToSoleJSConditionalOrderWithCivilPartnershipContent() {
        setMockClock(clock);

        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .languagePreferenceWelsh(YesOrNo.NO)
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(APPLICANT_2_FIRST_NAME)
            .lastName(APPLICANT_2_LAST_NAME)
            .gender(Gender.FEMALE)
            .languagePreferenceWelsh(YesOrNo.NO)
            .address(AddressGlobalUK.builder()
                .addressLine1("223b")
                .addressLine2("Baker Street")
                .postTown("Tampa")
                .county("Florida")
                .country("United States")
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(DISSOLUTION)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
            .thenReturn(new HashMap<>(Map.of(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT)));
        when(commonContent.getPartner(caseData, caseData.getApplicant1(), caseData.getApplicant2().getLanguagePreference()))
            .thenReturn("wife");

        final Map<String, Object> result = switchToSoleCoJSTemplateContent.getTemplateContent(caseData, TEST_CASE_ID, null);

        assertThat(result).contains(
            entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(IS_DIVORCE, false),
            entry(FIRST_NAME, APPLICANT_2_FIRST_NAME),
            entry(LAST_NAME, APPLICANT_2_LAST_NAME),
            entry(ADDRESS, applicant2.getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(PARTNER, "wife"),
            entry(DATE, LocalDate.now().format(DATE_TIME_FORMATTER))
        );
    }
}
