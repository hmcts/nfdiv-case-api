package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.notification.CommonContent.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SMART_SURVEY;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_FIRM;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;

@ExtendWith(MockitoExtension.class)
class NocSolRemovedSelfNotificationsTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private NocSolRemovedSelfNotifications notificationHandler;

    private Map<String, String> getSolTemplateVars(Applicant applicant) {
        Map<String, String> templateVars = getTemplateVars(applicant);
        templateVars.put(SOLICITOR_REFERENCE, applicant.getSolicitor().getReference());
        return templateVars;
    }

    private Map<String, String> getTemplateVars(Applicant applicant) {
        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(FIRST_NAME, applicant.getFirstName());
        templateVars.put(LAST_NAME, applicant.getLastName());
        templateVars.put(SOLICITOR_FIRM, applicant.getSolicitor().getFirmName());
        templateVars.put(SMART_SURVEY, SMART_SURVEY);
        return templateVars;
    }

    @Test
    void testSendToApplicant1OldSolicitor() {
        CaseData caseData = createMockCaseData();
        Long id = 1L;

        when(commonContent.nocOldSolsTemplateVars(id, caseData.getApplicant1()
        )).thenReturn(getSolTemplateVars(caseData.getApplicant1()));

        notificationHandler.sendToApplicant1OldSolicitor(caseData, id);

        verify(notificationService).sendEmail(
            eq(caseData.getApplicant1().getSolicitor().getEmail()),
            eq(EmailTemplateName.NOC_TO_SOLS_EMAIL_OLD_SOL_REMOVED_SELF),
            anyMap(),
            eq(caseData.getApplicant1().getLanguagePreference()),
            eq(id)
        );
        verify(commonContent).nocOldSolsTemplateVars(id, caseData.getApplicant1()
        );
    }

    @Test
    void testSendToApplicant2OldSolicitor() {
        CaseData caseData = createMockCaseData();
        Long id = 1L;

        when(commonContent.nocOldSolsTemplateVars(id, caseData.getApplicant2()
        )).thenReturn(getSolTemplateVars(caseData.getApplicant2()));

        notificationHandler.sendToApplicant2OldSolicitor(caseData, id);

        verify(notificationService).sendEmail(
            eq(caseData.getApplicant2().getSolicitor().getEmail()),
            eq(EmailTemplateName.NOC_TO_SOLS_EMAIL_OLD_SOL_REMOVED_SELF),
            anyMap(),
            eq(caseData.getApplicant2().getLanguagePreference()),
            eq(id)
        );
        verify(commonContent).nocOldSolsTemplateVars(id, caseData.getApplicant2()
        );
    }

    private CaseData createMockCaseData() {
        CaseData caseData = CaseData.builder().build();
        caseData.setApplicant1(createApplicant("test@test.com", "App1 Solicitor Firm", YesOrNo.NO));
        caseData.setApplicant2(createApplicant("testtwo@test.com", "App2 Solicitor Firm", YesOrNo.YES));
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplication().setApplicant1StatementOfTruth(YES);
        return caseData;
    }

    private Applicant createApplicant(String email, String solicitorFirmName, YesOrNo languagePreferenceWelsh) {
        Solicitor solicitor = createSolicitor(solicitorFirmName);
        return Applicant.builder()
            .email(email)
            .languagePreferenceWelsh(languagePreferenceWelsh)
            .solicitor(solicitor)
            .build();
    }

    private Solicitor createSolicitor(String firmName) {
        return Solicitor.builder()
            .email("test@example.com")
            .firmName(firmName)
            .organisationPolicy(OrganisationPolicy.<UserRole>builder()
                .organisation(Organisation.builder().organisationId(TEST_ORG_ID).build())
                .build())
            .build();
    }

}
