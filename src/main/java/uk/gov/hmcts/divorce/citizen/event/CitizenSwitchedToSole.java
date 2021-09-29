package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.util.EnumSet;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Applicant2Approved;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant1Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class CitizenSwitchedToSole implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    private CcdAccessService ccdAccessService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    public static final String SWITCH_TO_SOLE = "switch-to-sole";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        EnumSet<State> stateSet = EnumSet.noneOf(State.class);
        stateSet.add(AwaitingApplicant1Response);
        stateSet.add(AwaitingApplicant2Response);
        stateSet.add(Applicant2Approved);
        stateSet.add(AwaitingPayment);

        configBuilder
            .event(SWITCH_TO_SOLE)
            .forStateTransition(stateSet, Draft)
            .name("Application switched to sole")
            .description("Application type switched to sole")
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Applicant 1 switched to sole about to submit callback invoked");
        CaseData data = details.getData();

        log.info("Unlinking Applicant 2 from Case");
        ccdAccessService.unlinkUserFromApplication(
            httpServletRequest.getHeader(AUTHORIZATION),
            details.getId(),
            data.getCaseInvite().getApplicant2UserId()
        );

        data.setApplicationType(ApplicationType.SOLE_APPLICATION);
        removeApplicant2AnswersFromCase(data);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(Draft)
            .build();
    }

    private CaseData removeApplicant2AnswersFromCase(CaseData caseData) {
        Applicant applicant2Existing = caseData.getApplicant2();
        Applicant applicant2 = Applicant.builder()
            .firstName(applicant2Existing.getFirstName())
            .middleName(applicant2Existing.getMiddleName())
            .lastName(applicant2Existing.getLastName())
            .email(applicant2Existing.getEmail())
            .gender(applicant2Existing.getGender())
            .homeAddress(applicant2Existing.isConfidentialContactDetails() ? null : applicant2Existing.getHomeAddress())
            .build();

        caseData.setApplicant2(applicant2);

        caseData.setApplicant2DocumentsUploaded(null);
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(null);
        caseData.getApplication().setApplicant2HelpWithFees(null);
        caseData.getApplication().setApplicant2StatementOfTruth(null);
        caseData.getApplication().setApplicant2AgreeToReceiveEmails(null);
        caseData.getApplication().setApplicant2CannotUploadSupportingDocument(null);
        caseData.getApplication().setApplicant2ConfirmApplicant1Information(null);
        caseData.getApplication().setApplicant2ExplainsApplicant1IncorrectInformation(null);
        caseData.getApplication().setApplicant2ReminderSent(null);

        caseData.setCaseInvite(null);
        return caseData;
    }
}
