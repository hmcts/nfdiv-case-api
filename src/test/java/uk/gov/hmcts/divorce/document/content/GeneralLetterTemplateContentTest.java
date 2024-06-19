package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FEEDBACK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_JOINT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATION;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.buildCaseDataWithGeneralLetter;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContentWithCtscContactDetails;

@ExtendWith(MockitoExtension.class)
public class GeneralLetterTemplateContentTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private GeneralLetterTemplateContent generalLetterTemplateContent;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

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

    @BeforeEach
    public void setUp() {
        setMockClock(clock, LocalDate.of(2022, 3, 16));
    }

    @Test
    public void shouldMapTemplateContentWhenRecipientIsApplicant1() {
        var caseData = buildCaseDataWithGeneralLetter(GeneralParties.APPLICANT);

        when(commonContent.getPartner(any(), any())).thenReturn("wife");
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
                .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        final Map<String, Object> templateContent = generalLetterTemplateContent
                .apply(caseData, TEST_CASE_ID, caseData.getApplicant1().getLanguagePreference());

        assertThat(templateContent).contains(
                entry(ISSUE_DATE, "16 March 2022"),
                entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
                entry(RELATION, "wife"),
                entry(RECIPIENT_NAME, "test_first_name test_last_name"),
                entry(RECIPIENT_ADDRESS, "line 1\ntown\nUK\npostcode"),
                entry(IS_JOINT, true),
                entry(FEEDBACK, "some feedback"),
                entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT)
        );
    }

    @Test
    public void shouldMapTemplateContentWhenRecipientIsApplicant2() {
        var caseData = buildCaseDataWithGeneralLetter(GeneralParties.RESPONDENT);

        when(commonContent.getPartner(any(), any())).thenReturn("husband");
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant2().getLanguagePreference()))
                .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        final Map<String, Object> templateContent = generalLetterTemplateContent.apply(caseData, TEST_CASE_ID,
                caseData.getApplicant2().getLanguagePreference());

        assertThat(templateContent).contains(
                entry(ISSUE_DATE, "16 March 2022"),
                entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
                entry(RELATION, "husband"),
                entry(RECIPIENT_NAME, "test_first_name test_last_name"),
                entry(RECIPIENT_ADDRESS, "line 1\ntown\nUK\npostcode"),
                entry(IS_JOINT, true),
                entry(FEEDBACK, "some feedback"),
                entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT)
        );
    }

    @Test
    public void shouldMapTemplateContentWhenRecipientIsOther() {
        var caseData = buildCaseDataWithGeneralLetter(GeneralParties.OTHER);
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.getGeneralLetter().setOtherRecipientName("Other Recipient");
        caseData.getGeneralLetter().setOtherRecipientAddress(AddressGlobalUK.builder()
                .addressLine1("New lane")
                .postTown("Birmingham")
                .postCode("B1 XXX")
                .build());

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
                .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        final Map<String, Object> templateContent = generalLetterTemplateContent
                .apply(caseData, TEST_CASE_ID, caseData.getApplicant1().getLanguagePreference());

        verifyNoInteractions(commonContent);

        assertThat(templateContent).contains(
                entry(ISSUE_DATE, "16 March 2022"),
                entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
                entry(RELATION, "civil partner"),
                entry(RECIPIENT_NAME, "Other Recipient"),
                entry(RECIPIENT_ADDRESS, "New lane\nBirmingham\nB1 XXX"),
                entry(IS_JOINT, false),
                entry(FEEDBACK, "some feedback"),
                entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT)
        );
    }
}
