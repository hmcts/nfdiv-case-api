package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.notification.NoticeOfProceedingsNotification;
import uk.gov.hmcts.divorce.caseworker.service.notification.PersonalServiceNotification;
import uk.gov.hmcts.divorce.caseworker.service.print.AosPackPrinter;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.Clock;
import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Issued;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_RDU;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
@Slf4j
public class CaseworkerIssueAos implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_ISSUE_AOS = "caseworker-issue-aos";

    @Value("${aos_pack.due_date_offset_days}")
    private long dueDateOffsetDays;

    @Autowired
    private PersonalServiceNotification personalServiceNotification;

    @Autowired
    private NoticeOfProceedingsNotification noticeOfProceedingsNotification;

    @Autowired
    private AosPackPrinter aosPackPrinter;

    @Autowired
    private Clock clock;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_ISSUE_AOS)
            .forStateTransition(Issued, AwaitingAos)
            .name("Issue AOS pack")
            .description("Issue AOS pack to respondent")
            .displayOrder(1)
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE,
                CASEWORKER_SYSTEMUPDATE)
            .grant(READ,
                SOLICITOR,
                CASEWORKER_COURTADMIN_CTSC,
                CASEWORKER_COURTADMIN_RDU,
                CASEWORKER_SUPERUSER,
                CASEWORKER_LEGAL_ADVISOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = details.getData();
        final Long caseId = details.getId();
        log.info("Caseworker issue AOS about to submit callback invoked. Case ID: {}", caseId);

        if (!caseData.getApplication().isPersonalServiceMethod()) {

            final Applicant respondent = caseData.getApplicant2();
            final Solicitor respondentSolicitor = respondent.getSolicitor();

            if (respondent.isRepresented()) {
                log.info("Sending respondent AOS pack to bulk print, "
                    + "respondent is represented by digital solicitor.  Case ID: {}:", caseId);
                aosPackPrinter.print(caseData, caseId);
                setNoticeOfProceedingsInformation(caseData, respondentSolicitor);
            } else {
                log.info("Sending respondent AOS pack to bulk print, respondent is not represented.  CaseID: {}", caseId);
                aosPackPrinter.print(caseData, caseId);
            }

            final LocalDate dueDate = LocalDate.now(clock).plusDays(dueDateOffsetDays);

            log.info("Setting due date.  Case ID: {}", caseId);
            caseData.setDueDate(dueDate);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }


    public SubmittedCallbackResponse submitted(final CaseDetails<CaseData, State> details,
                                               final CaseDetails<CaseData, State> beforeDetails) {

        final Long caseId = details.getId();
        final CaseData caseData = details.getData();
        log.info("Caseworker issue AOS submitted callback invoked.  Case ID: {}", caseId);

        if (caseData.getApplication().isPersonalServiceMethod()) {
            personalServiceNotification.send(caseData, caseId);
        } else {
            noticeOfProceedingsNotification.send(caseData, caseId);
        }

        return SubmittedCallbackResponse.builder().build();
    }

    private void setNoticeOfProceedingsInformation(final CaseData caseData, final Solicitor solicitor) {
        caseData.getAcknowledgementOfService().setDigitalNoticeOfProceedings(YES);
        caseData.getAcknowledgementOfService().setNoticeOfProceedingsEmail(solicitor.getEmail());
        caseData.getAcknowledgementOfService().setNoticeOfProceedingsSolicitorFirm(
            solicitor.getOrganisationPolicy().getOrganisation().getOrganisationName());
    }
}
