package uk.gov.hmcts.divorce.document.content.templatecontent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.NAME;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;

@ExtendWith(MockitoExtension.class)
class AppliedForCoTemplateContentTest {

    private static final String DATE_D84_RECEIVED = "dateD84Received";
    private static final String GRANTED_DATE = "grantedDate";

    @Mock
    private Clock clock;

    @InjectMocks
    private AppliedForCoTemplateContent appliedForCoTemplateContent;

    @Test
    void shouldApplyContentFromCaseDataForConditionalOrderCoverLetter() {

        setMockClock(clock);
        final LocalDateTime localDateTime = LocalDateTime.of(2021, 11, 8, 14, 56);

        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .address(AddressGlobalUK.builder()
                .addressLine1("223b")
                .addressLine2("Baker Street")
                .postTown("Tampa")
                .county("Florida")
                .country("United States")
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .conditionalOrder(ConditionalOrder.builder().dateD84FormScanned(localDateTime).build())
            .build();

        final Map<String, Object> result = appliedForCoTemplateContent.getTemplateContent(caseData, TEST_CASE_ID, applicant1);

        assertThat(result).contains(
            entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME),
            entry(ADDRESS, applicant1.getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(DATE, LocalDate.now().format(DATE_TIME_FORMATTER)),
            entry(DATE_D84_RECEIVED, "8 November 2021"),
            entry(GRANTED_DATE, LocalDate.now().plusWeeks(4).format(DATE_TIME_FORMATTER))
        );
    }
}
