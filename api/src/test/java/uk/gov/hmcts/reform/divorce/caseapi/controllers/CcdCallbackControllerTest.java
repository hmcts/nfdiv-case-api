package uk.gov.hmcts.reform.divorce.caseapi.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.caseapi.exceptions.NotificationException;
import uk.gov.hmcts.reform.divorce.caseapi.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.caseapi.notification.handler.SaveAndSignOutNotificationHandler;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.DivorceOrDissolution;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.D8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.D8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.D8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.TEST_USER_EMAIL;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CcdCallbackControllerTest {

    @Autowired
    private CcdCallbackController ccdCallbackController;

    @MockBean
    private SaveAndSignOutNotificationHandler saveAndSignOutNotificationHandler;

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenSendEmail() {
        ccdCallbackController.handleSaveAndSignOutCallback(
            AUTH_TOKEN, getCcdCallbackRequest()
        );

        verify(saveAndSignOutNotificationHandler).notifyApplicant(caseData());
        verifyNoMoreInteractions(saveAndSignOutNotificationHandler);
    }

    @Test
    public void givenHandlerThrowsExceptionWhenCallbackIsInvokedThenReturnBadRequest() {
        doThrow(new NotificationException(new NotificationClientException("All template params not passed")))
            .when(saveAndSignOutNotificationHandler).notifyApplicant(eq(caseData()));

        Throwable thrown = assertThrows(RuntimeException.class,
            () -> ccdCallbackController.handleSaveAndSignOutCallback(
                AUTH_TOKEN, getCcdCallbackRequest()
            )
        );

        assertThat(thrown.getCause().getMessage(), is("All template params not passed"));
    }

    private CcdCallbackRequest getCcdCallbackRequest() {
        Map<String, Object> caseData = Map.of(
            D8_PETITIONER_FIRST_NAME, TEST_FIRST_NAME,
            D8_PETITIONER_LAST_NAME, TEST_LAST_NAME,
            D8_PETITIONER_EMAIL, TEST_USER_EMAIL,
            DIVORCE_OR_DISSOLUTION, DivorceOrDissolution.DIVORCE
        );

        return CcdCallbackRequest.builder()
            .caseDetails(
                CaseDetails.builder()
                    .data(caseData)
                    .build()
            )
            .build();
    }

    private CaseData caseData() {
        CaseData caseData = new CaseData();
        caseData.setD8PetitionerFirstName(TEST_FIRST_NAME);
        caseData.setD8PetitionerLastName(TEST_LAST_NAME);
        caseData.setD8PetitionerEmail(TEST_USER_EMAIL);
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DIVORCE);
        return caseData;
    }
}
