package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
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

@ExtendWith(MockitoExtension.class)
public class GeneralLetterTemplateContentTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private GeneralLetterTemplateContent generalLetterTemplateContent;

    private static final CtscContactDetails CTSC_CONTACT = CtscContactDetails
        .builder()
        .emailAddress("divorcecase@justice.gov.uk")
        .poBox("PO Box 13226")
        .town("Harlow")
        .postcode("CM20 9UG")
        .phoneNumber("0300 303 0642")
        .build();

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(generalLetterTemplateContent, "poBox", CTSC_CONTACT.getPoBox());
        ReflectionTestUtils.setField(generalLetterTemplateContent, "town", CTSC_CONTACT.getTown());
        ReflectionTestUtils.setField(generalLetterTemplateContent, "postcode", CTSC_CONTACT.getPostcode());
        ReflectionTestUtils.setField(generalLetterTemplateContent, "email", CTSC_CONTACT.getEmailAddress());
        ReflectionTestUtils.setField(generalLetterTemplateContent, "phoneNumber", CTSC_CONTACT.getPhoneNumber());

        setMockClock(clock, LocalDate.of(2022, 3, 16));
    }

    @Test
    public void shouldMapTemplateContentWhenRecipientIsApplicant1() {
        var caseData = buildCaseDataWithGeneralLetter(GeneralParties.APPLICANT);

        when(commonContent.getPartner(any(), any())).thenReturn("wife");

        final Map<String, Object> templateContent = generalLetterTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(ISSUE_DATE, "16 March 2022"),
            entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(RELATION, "wife"),
            entry(RECIPIENT_NAME, "test_first_name test_last_name"),
            entry(RECIPIENT_ADDRESS, "line 1\ntown\npostcode"),
            entry(IS_JOINT, true),
            entry(FEEDBACK, "some feedback"),
            entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT)
        );
    }

    @Test
    public void shouldMapTemplateContentWhenRecipientIsApplicant2() {
        var caseData = buildCaseDataWithGeneralLetter(GeneralParties.RESPONDENT);

        when(commonContent.getPartner(any(), any())).thenReturn("husband");

        final Map<String, Object> templateContent = generalLetterTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(ISSUE_DATE, "16 March 2022"),
            entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(RELATION, "husband"),
            entry(RECIPIENT_NAME, "test_first_name test_last_name"),
            entry(RECIPIENT_ADDRESS, "line 1\ntown\npostcode"),
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

        final Map<String, Object> templateContent = generalLetterTemplateContent.apply(caseData, TEST_CASE_ID);

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
