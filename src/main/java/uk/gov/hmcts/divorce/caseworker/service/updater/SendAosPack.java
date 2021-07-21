package uk.gov.hmcts.divorce.caseworker.service.updater;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.caseworker.service.print.AosPackPrinter;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataContext;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdater;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdaterChain;

import java.time.Clock;
import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

@Component
@Slf4j
public class SendAosPack implements CaseDataUpdater {

    @Value("${aos_pack.due_date_offset_days}")
    private long dueDateOffsetDays;

    @Autowired
    private AosPackPrinter aosPackPrinter;

    @Autowired
    private Clock clock;

    @Override
    public CaseDataContext updateCaseData(final CaseDataContext caseDataContext,
                                          final CaseDataUpdaterChain caseDataUpdaterChain) {

        final CaseData originalCaseData = caseDataContext.getCaseData();
        final CaseData updatedCaseData = caseDataContext.copyOfCaseData();
        final Long caseId = caseDataContext.getCaseId();

        if (!originalCaseData.getApplication().isPersonalServiceMethod()) {

            final Applicant respondent = originalCaseData.getApplicant2();
            final Solicitor respondentSolicitor = respondent.getSolicitor();

            if (respondent.isRepresented()) {
                log.info("Sending respondent AOS pack to bulk print, "
                    + "respondent is represented by digital solicitor.  Case ID: {}:", caseId);
                aosPackPrinter.print(originalCaseData, caseId);

                log.info("Setting Notice Of Proceedings information. CaseID: {}", caseId);
                setNoticeOfProceedingsInformation(updatedCaseData, respondentSolicitor);
            } else {
                log.info("Sending respondent AOS pack to bulk print, respondent is not represented.  CaseID: {}", caseId);
                aosPackPrinter.print(originalCaseData, caseId);
            }

            log.info("Setting due date.  Case ID: {}", caseId);
            updatedCaseData.setDueDate(LocalDate.now(clock).plusDays(dueDateOffsetDays));
        }

        return caseDataUpdaterChain.processNext(caseDataContext.handlerContextWith(updatedCaseData));
    }

    private void setNoticeOfProceedingsInformation(final CaseData caseData, final Solicitor solicitor) {
        caseData.getAcknowledgementOfService().setDigitalNoticeOfProceedings(YES);
        caseData.getAcknowledgementOfService().setNoticeOfProceedingsEmail(solicitor.getEmail());
        caseData.getAcknowledgementOfService().setNoticeOfProceedingsSolicitorFirm(
            solicitor.getOrganisationPolicy().getOrganisation().getOrganisationName());
    }
}
