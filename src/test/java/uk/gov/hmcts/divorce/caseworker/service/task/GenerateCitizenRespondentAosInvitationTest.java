package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.util.AccessCodeGenerator;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.CitizenRespondentAosInvitationTemplateContent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CITIZEN_RESP_AOS_INVITATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESP_AOS_INVITATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.RESPONDENT_INVITATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ACCESS_CODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondentWithDigitalSolicitor;

@ExtendWith(MockitoExtension.class)
class GenerateCitizenRespondentAosInvitationTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private CitizenRespondentAosInvitationTemplateContent templateContent;

    @InjectMocks
    private GenerateCitizenRespondentAosInvitation generateCitizenRespondentAosInvitation;

    @Test
    void shouldCallDocAssemblyServiceAndReturnCaseDataWithAosInvitationDocumentIfRespondentIsNotRepresented() {

        final var caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YES);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final Supplier<Map<String, Object>> templateContentSupplier = HashMap::new;

        final MockedStatic<AccessCodeGenerator> classMock = mockStatic(AccessCodeGenerator.class);
        classMock.when(AccessCodeGenerator::generateAccessCode).thenReturn(ACCESS_CODE);

        when(templateContent.apply(caseData, TEST_CASE_ID, LOCAL_DATE)).thenReturn(templateContentSupplier);

        final var result = generateCitizenRespondentAosInvitation.apply(caseDetails);

        assertThat(result.getData().getCaseInvite().getAccessCode()).isEqualTo(ACCESS_CODE);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                RESPONDENT_INVITATION,
                templateContentSupplier,
                TEST_CASE_ID,
                CITIZEN_RESP_AOS_INVITATION,
                ENGLISH,
                RESP_AOS_INVITATION_DOCUMENT_NAME + TEST_CASE_ID);

        classMock.close();
    }

    @Test
    void shouldDoNothingIfRespondentIsRepresented() {

        final var caseData = caseData();
        caseData.setApplicant2(respondentWithDigitalSolicitor());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final var result = generateCitizenRespondentAosInvitation.apply(caseDetails);

        assertThat(result.getData()).isEqualTo(caseData);
        verifyNoInteractions(templateContent, caseDataDocumentService);
    }
}
