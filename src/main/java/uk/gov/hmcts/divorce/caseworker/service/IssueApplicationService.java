package uk.gov.hmcts.divorce.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.notification.NoticeOfProceedingsNotification;
import uk.gov.hmcts.divorce.caseworker.service.notification.PersonalServiceNotification;
import uk.gov.hmcts.divorce.caseworker.service.print.AosPackPrinter;
import uk.gov.hmcts.divorce.caseworker.service.updater.MiniApplication;
import uk.gov.hmcts.divorce.caseworker.service.updater.RespondentSolicitorAosInvitation;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataContext;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdater;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdaterChainFactory;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

@Service
@Slf4j
public class IssueApplicationService {

    @Autowired
    private MiniApplication miniApplication;

    @Autowired
    private CaseDataUpdaterChainFactory caseDataUpdaterChainFactory;

    @Autowired
    private RespondentSolicitorAosInvitation respondentSolicitorAosInvitation;

    @Autowired
    private Clock clock;

    @Value("${aos_pack.due_date_offset_days}")
    private long dueDateOffsetDays;

    @Autowired
    private PersonalServiceNotification personalServiceNotification;

    @Autowired
    private NoticeOfProceedingsNotification noticeOfProceedingsNotification;

    @Autowired
    private AosPackPrinter aosPackPrinter;

    public CaseData aboutToSubmit(final CaseData caseData,
                                  final Long caseId,
                                  final LocalDate createdDate,
                                  final String idamAuthToken) {

        List<CaseDataUpdater> caseDataUpdaters;

        if (caseData.getApplicant2().isRepresented()) {
            caseDataUpdaters = asList(
                miniApplication,
                respondentSolicitorAosInvitation
            );
        } else {
            caseDataUpdaters = singletonList(miniApplication);
        }

        final CaseDataContext caseDataContext = CaseDataContext.builder()
            .caseData(caseData)
            .caseId(caseId)
            .createdDate(createdDate)
            .userAuthToken(idamAuthToken)
            .build();

        CaseData updatedCaseData = caseDataUpdaterChainFactory
            .createWith(caseDataUpdaters)
            .processNext(caseDataContext)
            .getCaseData();

        updatedCaseData.getApplication().setIssueDate(LocalDate.now(clock));

        return updatedCaseData;
    }

    public AboutToStartOrSubmitResponse<CaseData, State> sendAosPack(final CaseDetails<CaseData, State> details,
                                                                     final CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = details.getData();
        final Long caseId = details.getId();
        log.info("Caseworker issue AOS about to submit callback invoked. Case ID: {}", caseId);

        if (caseData.getApplication().isPersonalServiceMethod()) {
            personalServiceNotification.send(caseData, caseId);
        } else {

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

            noticeOfProceedingsNotification.send(caseData, caseId);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private void setNoticeOfProceedingsInformation(final CaseData caseData, final Solicitor solicitor) {
        caseData.getAcknowledgementOfService().setDigitalNoticeOfProceedings(YES);
        caseData.getAcknowledgementOfService().setNoticeOfProceedingsEmail(solicitor.getEmail());
        caseData.getAcknowledgementOfService().setNoticeOfProceedingsSolicitorFirm(
            solicitor.getOrganisationPolicy().getOrganisation().getOrganisationName());
    }
}
