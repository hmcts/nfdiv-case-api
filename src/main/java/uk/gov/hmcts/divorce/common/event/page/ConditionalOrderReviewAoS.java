package uk.gov.hmcts.divorce.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;

@Slf4j
@Component
public class ConditionalOrderReviewAoS implements CcdPageConfiguration {

    private static final String NEVER_SHOW = "coApplicant1ConfirmInformationStillCorrect=\"NEVER_SHOW\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("ConditionalOrderReviewAoS")
            .pageLabel("Review Acknowledgement of Service - Draft Conditional Order Application")
            .readonlyNoSummary(CaseData::getApplicationType, NEVER_SHOW)
            .complex(CaseData::getAcknowledgementOfService)
                .readonlyNoSummary(AcknowledgementOfService::getDateAosSubmitted, NEVER_SHOW)
            .done()
            .complex(CaseData::getConditionalOrder)
                .readonly(ConditionalOrder::getLastAlternativeServiceDocumentLink,
                " applicationType=\"soleApplication\" AND dateAosSubmitted!=\"*\"")
                .readonly(ConditionalOrder::getRespondentAnswersLink,
                    "applicationType=\"soleApplication\" AND dateAosSubmitted=\"*\"")
                .complex(ConditionalOrder::getConditionalOrderApplicant1Questions)
                    .mandatory(ConditionalOrderQuestions::getApplyForConditionalOrder)
                .done()
            .done();
    }
}
