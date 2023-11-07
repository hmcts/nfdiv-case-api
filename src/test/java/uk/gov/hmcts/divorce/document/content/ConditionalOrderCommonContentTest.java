package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ClarificationReason;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.RefusalOption;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_ADDRESS;

@ExtendWith(MockitoExtension.class)
class ConditionalOrderCommonContentTest {
    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private ConditionalOrderCommonContent conditionalOrderCommonContent;

    @Test
    void shouldGenerateLegalAdvisorCommentsForMoreInfo() {
        final List<ConditionalOrderCommonContent.RefusalReason> refusalReasons =
            List.of(
                new ConditionalOrderCommonContent.RefusalReason("Jurisdiction details"),
                new ConditionalOrderCommonContent.RefusalReason("Court does not have jurisdiction"));

        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .applicationType(JOINT_APPLICATION)
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .applicant1(
                Applicant.builder()
                    .firstName("Bob")
                    .lastName("Smith")
                    .address(APPLICANT_ADDRESS)
                    .languagePreferenceWelsh(NO)
                    .build()
            )
            .applicant2(
                Applicant.builder()
                    .gender(FEMALE)
                    .build()
            )
            .conditionalOrder(
                ConditionalOrder.builder()
                    .refusalDecision(RefusalOption.MORE_INFO)
                    .refusalClarificationReason(Collections.singleton(ClarificationReason.JURISDICTION_DETAILS))
                    .refusalClarificationAdditionalInfo("Court does not have jurisdiction")
                    .build()
            )
            .build();

        List<ConditionalOrderCommonContent.RefusalReason> result = conditionalOrderCommonContent.generateLegalAdvisorComments(
            caseData.getConditionalOrder());

        assertThat(result).isEqualTo(refusalReasons);
    }

    @Test
    void shouldGenerateLegalAdvisorCommentsForAmendment() {
        final List<ConditionalOrderCommonContent.RefusalReason> refusalReasons =
            List.of(new ConditionalOrderCommonContent.RefusalReason("Court does not have jurisdiction"));

        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .applicationType(JOINT_APPLICATION)
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .applicant1(
                Applicant.builder()
                    .firstName("Bob")
                    .lastName("Smith")
                    .address(APPLICANT_ADDRESS)
                    .languagePreferenceWelsh(NO)
                    .build()
            )
            .applicant2(
                Applicant.builder()
                    .gender(FEMALE)
                    .build()
            )
            .conditionalOrder(
                ConditionalOrder.builder()
                    .refusalDecision(RefusalOption.REJECT)
                    .refusalRejectionAdditionalInfo("Court does not have jurisdiction")
                    .build()
            )
            .build();

        List<ConditionalOrderCommonContent.RefusalReason> result = conditionalOrderCommonContent.generateLegalAdvisorComments(
            caseData.getConditionalOrder());

        assertThat(result).isEqualTo(refusalReasons);
    }

    @Test
    void shouldReturnSpouseForOfflineApplicant() {
        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .applicant1(
                Applicant.builder()
                    .gender(MALE)
                    .languagePreferenceWelsh(NO)
                    .offline(YES)
                    .build()
            )
            .applicant2(
                Applicant.builder()
                    .gender(FEMALE)
                    .build()
            )
            .build();

        final String result = conditionalOrderCommonContent.getPartner(caseData);

        assertThat(result).isEqualTo(CommonContent.SPOUSE);
    }
}
