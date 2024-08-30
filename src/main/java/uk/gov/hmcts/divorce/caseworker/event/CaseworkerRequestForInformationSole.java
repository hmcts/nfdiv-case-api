package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.notification.RequestForInformationNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformation;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationList;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;

import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@RequiredArgsConstructor
@Component
public class CaseworkerRequestForInformationSole implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_REQUEST_FOR_INFORMATION_SOLE = "caseworker-request-for-information-sole";

    public static final String REQUEST_FOR_INFORMATION_NOTIFICATION_FAILED_ERROR
        = "Unable to send Request for Information Notification for Case Id: ";

    private final CaseworkerRequestForInformationHelper helper;

    private final RequestForInformationNotification requestForInformationNotification;

    private final NotificationDispatcher notificationDispatcher;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_REQUEST_FOR_INFORMATION_SOLE)
            .forAllStates()
            .name("Request For Information")
            .description("Request for information")
            .showCondition("applicationType=\"soleApplication\"")
            .showSummary()
            .showEventNotes()
            .endButtonLabel("Submit")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR, JUDGE))
            .page("requestForInformation", this::midEvent)
            .pageLabel("Request For Information")
            .complex(CaseData::getRequestForInformationList)
                .complex(RequestForInformationList::getRequestForInformation)
                    .mandatory(RequestForInformation::getRequestForInformationSoleParties)
                    .mandatory(RequestForInformation::getRequestForInformationName, "requestForInformationSoleParties=\"other\"")
                    .mandatory(RequestForInformation::getRequestForInformationEmailAddress, "requestForInformationSoleParties=\"other\"")
                    .mandatory(RequestForInformation::getRequestForInformationDetails)
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {

        log.info("{} midEvent callback invoked for Case Id: {}", CASEWORKER_REQUEST_FOR_INFORMATION_SOLE, details.getId());

        List<String> errors = helper.areEmailsValid(details.getData());
        if (!errors.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(errors)
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked for Case Id: {}", CASEWORKER_REQUEST_FOR_INFORMATION_SOLE, details.getId());

        CaseData data = helper.createRequestForInformation(details);

        try {
            notificationDispatcher.sendRequestForInformationNotification(
                requestForInformationNotification,
                data,
                details.getId()
            );
        } catch (final NotificationTemplateException e) {
            log.error("Request for Information Notification for Case Id {} failed with message: {}", details.getId(), e.getMessage(), e);
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList(REQUEST_FOR_INFORMATION_NOTIFICATION_FAILED_ERROR + details.getId()))
                .build();
        }

        //Prevent pre-populating fields for new request
        data.getRequestForInformationList().setRequestForInformation(new RequestForInformation());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(AwaitingDocuments)
            .build();
    }
}
