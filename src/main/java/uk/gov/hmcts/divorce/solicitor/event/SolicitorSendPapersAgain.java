package uk.gov.hmcts.divorce.solicitor.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.service.IssueApplicationService;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.notification.SolicitorSendPapersAgainNotification;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateServiceMethod;

@Component
@Slf4j
@RequiredArgsConstructor
public class SolicitorSendPapersAgain implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_RESEND_PAPERS = "solicitor-send-papers-again";
    private static final String SEND_PAPERS_AGAIN = "Send papers again";
    private static final String RESPONDENT_NEW_ADDRESS_LABEL = """
         You can inform the court that you want to resend the papers to the respondent. We will resend the papers \
         to their updated address at no additional cost if the service method is court service. You can only use this \
         option once.

         If you want to serve the papers yourself, choose "solicitor service". We will generate the documents you \
         need to send to the respondent. You can download the papers from the case and arrange for them to be \
         served on the respondent. The documents are usually available in the Documents tab.

         If the respondent has new contact details and you want to send the papers to their new address, you can \
         update their email or postal address using the “update respondent’s details” event before using this event

         """;

    private static final String ALWAYS_HIDE = "serviceMethod=\"ALWAYS_HIDE\"";

    private final IssueApplicationService issueApplicationService;
    private final NotificationDispatcher notificationDispatcher;
    private final SolicitorSendPapersAgainNotification solicitorSendPapersAgainNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SOLICITOR_RESEND_PAPERS)
            .forStates(AosOverdue)
            .name(SEND_PAPERS_AGAIN)
            .description(SEND_PAPERS_AGAIN)
            .showCondition("applicationType=\"soleApplication\" AND solicitorSentPapersAgain!=\"Yes\"")
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, LEGAL_ADVISOR, JUDGE))
            .page("sendPapersAgain", this::midEvent)
            .pageLabel(SEND_PAPERS_AGAIN)
            .label("respondentNewAddressLabel", RESPONDENT_NEW_ADDRESS_LABEL)
            .readonlyNoSummary(CaseData::getApplicationType, ALWAYS_HIDE)
            .complex(CaseData::getApplication)
            .mandatory(Application::getServiceMethod)
            .label("LabelSolicitorService",
                "After service is complete you must notify the court by completing the ‘Confirm Service’ event in CCD. "
                    + "Refer to the notification that will be sent upon the issuing of the the case",
                "serviceMethod=\"solicitorService\" AND applicationType=\"soleApplication\"");
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Solicitor {} about to submit callback invoked with Case Id: {}", SEND_PAPERS_AGAIN, details.getId());

        CaseData caseData = details.getData();
        State state = details.getState();

        final CaseDetails<CaseData, State> afterServiceType = issueApplicationService.updateServiceType(details);

        List<String> validationErrors = issueApplicationService.validateIssueApplication(afterServiceType);

        if (CollectionUtils.isNotEmpty(validationErrors)) {
            log.info("Data not valid for application issue, case id: {}, errors: {}", details.getId(), validationErrors);
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(validationErrors)
                .build();
        }

        final CaseDetails<CaseData, State> result = issueApplicationService.issueApplication(afterServiceType);

        caseData.getApplication().setSolicitorSentPapersAgain(YesOrNo.YES);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(result.getData())
            .state(result.getState())
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData caseData = details.getData();

        List<String> validationErrors = validateServiceMethod(caseData);

        if (isNotEmpty(validationErrors)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(validationErrors)
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details, CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} submitted callback invoked for case: {}", SEND_PAPERS_AGAIN, details.getId());

        notificationDispatcher.send(solicitorSendPapersAgainNotification, details.getData(), details.getId());

        return SubmittedCallbackResponse.builder().build();
    }
}
