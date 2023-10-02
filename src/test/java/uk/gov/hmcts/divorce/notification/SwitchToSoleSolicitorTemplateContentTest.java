package uk.gov.hmcts.divorce.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;

import java.time.Clock;
import java.util.Map;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_PLUS_14_DAYS;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.SwitchToSoleSolicitorTemplateContent.APPLICANT_1_NAME;
import static uk.gov.hmcts.divorce.notification.SwitchToSoleSolicitorTemplateContent.APPLICANT_2_NAME;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SwitchToSoleSolicitorTemplateContentTest {

    @Mock
    CommonContent commonContent;

    @Mock
    Clock clock;

    @InjectMocks
    SwitchToSoleSolicitorTemplateContent solicitorTemplateContent;


    @Test
    void templatevars() {

        setMockClock(clock);

        final Applicant applicant1 = Applicant.builder()
            .firstName("Julie")
            .lastName("Smith")
            .solicitor(
                Solicitor.builder()
                    .name("app1 sol")
                    .reference("sol ref")
                    .build()
            )
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName("Bob")
            .lastName("Smith")
            .solicitor(
                Solicitor.builder()
                    .name("app2 sol")
                    .reference("sol ref")
                    .build()
            )
            .build();

        final CaseData caseData = CaseData.builder()
            .applicant1(
                applicant1
            )
            .applicant2(
                applicant2
            )
            .finalOrder(FinalOrder.builder().doesApplicant2IntendToSwitchToSole(YES).build())
            .build();

        final Map<String, String> templateVars = solicitorTemplateContent.templatevars(caseData, TEST_CASE_ID, applicant1, applicant2);

        verify(commonContent).mainTemplateVars(eq(caseData), eq(TEST_CASE_ID), eq(applicant1), eq(applicant2));
        assertThat(templateVars)
            .contains(
                entry(APPLICANT_1_NAME, "Julie Smith"),
                entry(APPLICANT_2_NAME, "Bob Smith"),
                entry(SOLICITOR_REFERENCE, "sol ref"),
                entry(SOLICITOR_NAME, "app1 sol"),
                entry(DATE_PLUS_14_DAYS, now(clock).plusDays(14).format(DATE_TIME_FORMATTER))
            );
    }
}
