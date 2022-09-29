package uk.gov.hmcts.divorce.solicitor.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.CaseInfo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorCreateApplicationService;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class SolAboutTheSolicitorTest {

    @Mock
    private SolicitorCreateApplicationService solicitorCreateApplicationService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private SolAboutTheSolicitor page;

    @Test
    public void shouldReturnErrorIfEmailValidationFails() {
        final CaseData caseData = caseData();
        caseData.setApplicant1(Applicant.builder()
                .solicitorRepresented(YES)
                .solicitor(Solicitor.builder()
                    .email("invalidEmail")
                    .build())
            .build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        CaseInfo caseInfo = CaseInfo.builder().caseData(caseData).errors(new ArrayList<>()).build();

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(solicitorCreateApplicationService.validateSolicitorOrganisation(caseData,
            TEST_CASE_ID,
            TEST_AUTHORIZATION_TOKEN))
            .thenReturn(caseInfo);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(response.getErrors().size(), 1);
    }

    @Test
    public void shouldNotReturnErrorIfEmailValidationPasses() {
        final CaseData caseData = caseData();
        caseData.setApplicant1(Applicant.builder()
            .solicitorRepresented(YES)
            .solicitor(Solicitor.builder()
                .email(TEST_SOLICITOR_EMAIL)
                .build())
            .build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        CaseInfo caseInfo = CaseInfo.builder().caseData(caseData).errors(new ArrayList<>()).build();

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(solicitorCreateApplicationService.validateSolicitorOrganisation(caseData,
            TEST_CASE_ID,
            TEST_AUTHORIZATION_TOKEN))
            .thenReturn(caseInfo);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(response.getErrors().size(), 0);
    }
}
