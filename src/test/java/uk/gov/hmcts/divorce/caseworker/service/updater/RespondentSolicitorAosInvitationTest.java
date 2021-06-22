package uk.gov.hmcts.divorce.caseworker.service.updater;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.model.Applicant;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.updater.CaseDataContext;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChain;
import uk.gov.hmcts.divorce.common.util.AccessCodeGenerator;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.RespondentSolicitorAosInvitationTemplateContent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.common.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESP_AOS_INVITATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESP_SOLICITOR_AOS_INVITATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DOCUMENT_TYPE_RESPONDENT_INVITATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ACCESS_CODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;

@ExtendWith(MockitoExtension.class)
public class RespondentSolicitorAosInvitationTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private RespondentSolicitorAosInvitationTemplateContent templateContent;

    @Mock
    private CaseDataUpdaterChain caseDataUpdaterChain;

    @InjectMocks
    private RespondentSolicitorAosInvitation respondentSolicitorAosInvitation;

    @Test
    void shouldCallDocAssemblyServiceAndReturnCaseDataWithAosInvitationDocument() {

        final var caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .languagePreferenceWelsh(NO)
                .build())
            .build();

        final var caseDataContext = caseDataContext(caseData);

        final Supplier<Map<String, Object>> templateContentSupplier = HashMap::new;

        MockedStatic<AccessCodeGenerator> classMock = mockStatic(AccessCodeGenerator.class);
        classMock.when(AccessCodeGenerator::generateAccessCode).thenReturn(ACCESS_CODE);

        caseData.setAccessCode(ACCESS_CODE);

        when(templateContent.apply(caseData, TEST_CASE_ID, LOCAL_DATE)).thenReturn(templateContentSupplier);
        when(caseDataDocumentService
            .renderDocumentAndUpdateCaseData(
                caseData,
                DOCUMENT_TYPE_RESPONDENT_INVITATION,
                templateContentSupplier,
                TEST_CASE_ID,
                TEST_AUTHORIZATION_TOKEN,
                    RESP_SOLICITOR_AOS_INVITATION,
                RESP_AOS_INVITATION_DOCUMENT_NAME,
                ENGLISH))
            .thenReturn(caseData);

        when(caseDataUpdaterChain.processNext(caseDataContext)).thenReturn(caseDataContext);

        final var result = respondentSolicitorAosInvitation.updateCaseData(caseDataContext, caseDataUpdaterChain);

        assertThat(result.getCaseData()).isEqualTo(caseData);

        classMock.close();
    }

    private CaseDataContext caseDataContext(CaseData caseData) {
        return CaseDataContext
            .builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .createdDate(LOCAL_DATE)
            .userAuthToken(TEST_AUTHORIZATION_TOKEN)
            .build();
    }
}
