package uk.gov.hmcts.divorce.solicitor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class GeneralApplicationTypeOptionsServiceTest {

    @InjectMocks
    private GeneralApplicationTypeOptionsService generalApplicationTypeOptionsService;

    @Test
    void shouldGetCorrectOptionsForRespondentSolicitorInPreIssueState() {
        CaseData caseData = CaseData.builder()
            .applicationType(ApplicationType.JOINT_APPLICATION)
            .applicant2(Applicant.builder().solicitorRepresented(YesOrNo.NO).build())
            .build();

        DynamicList result = generalApplicationTypeOptionsService.buildOptions(State.Draft, caseData, null);

        assertThat(result.getListItems()).extracting(DynamicListElement::getLabel)
            .containsExactly(
                GeneralApplicationType.WITHDRAW_POST_ISSUE.getLabel(),
                GeneralApplicationType.ISSUE_DIVORCE_WITHOUT_CERT.getLabel(),
                GeneralApplicationType.EXPEDITE.getLabel(),
                GeneralApplicationType.AMEND_APPLICATION.getLabel(),
                GeneralApplicationType.OTHER.getLabel());
        assertThat(result.getValue()).isNull();
    }

    @Test
    void shouldGetCorrectOptionsForApplicant2SolicitorInPostIssueState() {
        CaseData caseData = CaseData.builder()
            .applicationType(ApplicationType.JOINT_APPLICATION)
            .applicant2(Applicant.builder().build())
            .build();

        DynamicList result = generalApplicationTypeOptionsService.buildOptions(State.AwaitingService, caseData, null);

        assertThat(result.getListItems()).extracting(DynamicListElement::getLabel)
            .containsExactly(
                GeneralApplicationType.WITHDRAW_POST_ISSUE.getLabel(),
                GeneralApplicationType.DELAY.getLabel(),
                GeneralApplicationType.EXPEDITE.getLabel(),
                GeneralApplicationType.AMEND_APPLICATION.getLabel(),
                GeneralApplicationType.OTHER.getLabel());
    }

    @Test
    void shouldIncludePostIssueSoleApplicantOptionsForApplicantSolicitor() {
        CaseData caseData = CaseData.builder()
            .applicationType(ApplicationType.SOLE_APPLICATION)
            .applicant1(Applicant.builder().build())
            .build();

        DynamicList result = generalApplicationTypeOptionsService.buildOptions(State.AwaitingService, caseData, null);

        assertThat(result.getListItems()).extracting(DynamicListElement::getLabel)
            .containsExactly(
                GeneralApplicationType.WITHDRAW_POST_ISSUE.getLabel(),
                GeneralApplicationType.DELAY.getLabel(),
                GeneralApplicationType.EXTEND.getLabel(),
                GeneralApplicationType.EXPEDITE.getLabel(),
                GeneralApplicationType.AMEND_APPLICATION.getLabel(),
                GeneralApplicationType.OTHER.getLabel());
    }

    @Test
    void shouldIncludeRespondentSolicitorRestrictedOptionsForPostIssueSoleCase() {
        CaseData caseData = CaseData.builder()
            .applicationType(ApplicationType.SOLE_APPLICATION)
            .applicant2(Applicant.builder().solicitorRepresented(YesOrNo.YES).build())
            .build();

        DynamicList result = generalApplicationTypeOptionsService.buildOptions(State.AwaitingService, caseData, null);

        assertThat(result.getListItems()).extracting(DynamicListElement::getLabel)
            .containsExactly(
                GeneralApplicationType.WITHDRAW_POST_ISSUE.getLabel(),
                GeneralApplicationType.DELAY.getLabel(),
                GeneralApplicationType.EXPEDITE.getLabel(),
                GeneralApplicationType.OTHER.getLabel());
    }

    @Test
    void shouldPreserveSelectionWhenExistingSelectedLabelStillAvailable() {
        CaseData caseData = CaseData.builder()
            .applicationType(ApplicationType.SOLE_APPLICATION)
            .applicant2(Applicant.builder().solicitorRepresented(YesOrNo.NO).build())
            .build();

        DynamicList existing = DynamicList.builder()
            .value(DynamicListElement.builder().label(GeneralApplicationType.EXTEND.getLabel()).build())
            .build();

        DynamicList result = generalApplicationTypeOptionsService.buildOptions(State.AwaitingService, caseData, existing);

        assertThat(result.getValue()).isNotNull();
        assertThat(result.getValue().getLabel()).isEqualTo(GeneralApplicationType.EXTEND.getLabel());
    }

    @Test
    void shouldClearSelectionWhenExistingSelectedLabelIsNotAvailableForComputedOptions() {
        CaseData caseData = CaseData.builder()
            .applicationType(ApplicationType.SOLE_APPLICATION)
            .applicant2(Applicant.builder().solicitorRepresented(YesOrNo.YES).build())
            .build();

        DynamicList existing = DynamicList.builder()
            .value(DynamicListElement.builder().label(GeneralApplicationType.AMEND_APPLICATION.getLabel()).build())
            .build();

        DynamicList result = generalApplicationTypeOptionsService.buildOptions(State.AwaitingService, caseData, existing);

        assertThat(result.getValue()).isNull();
    }
}
