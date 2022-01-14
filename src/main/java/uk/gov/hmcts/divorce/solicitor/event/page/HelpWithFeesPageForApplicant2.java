package uk.gov.hmcts.divorce.solicitor.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;

@Slf4j
@Component
public class HelpWithFeesPageForApplicant2 implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("HelpWithFeesPageForApplicant2", this::midEvent)
            .pageLabel("Help with fees")
            .complex(CaseData::getApplication)
                .mandatory(Application::getApplicant2NeedsHelpWithFees)
                .complex(Application::getApplicant2HelpWithFees)
                    .mandatory(HelpWithFees::getReferenceNumber, "applicant2NeedsHelpWithFees=\"Yes\"")
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(final CaseDetails<CaseData, State> details,
                                                                  final CaseDetails<CaseData, State> detailsBefore
    ) {
        log.info("Mid-event callback to set applicant 1 solicitor answers link");

        final CaseData data = details.getData();

        data.getDocumentsGenerated()
            .stream()
            .filter(document -> APPLICATION.equals(document.getValue().getDocumentType()))
            .findFirst()
            .ifPresent(draftDivorceApplication ->
                data.getApplication().setApplicant1SolicitorAnswersLink(draftDivorceApplication.getValue().getDocumentLink())
            );

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
