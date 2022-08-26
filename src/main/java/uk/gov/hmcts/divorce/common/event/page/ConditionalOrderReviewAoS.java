package uk.gov.hmcts.divorce.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ConditionalOrderReviewAoS implements CcdPageConfiguration {

    private static final String NEVER_SHOW = "coApplicant1ConfirmInformationStillCorrect=\"NEVER_SHOW\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("ConditionalOrderReviewAoS", this::midEvent)
            .pageLabel("Draft Conditional Order")
            .readonlyNoSummary(CaseData::getApplicationType, NEVER_SHOW)
            .complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getUnionType, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getDivorceOrCivilPartnershipApplication, NEVER_SHOW)
            .done()
            .complex(CaseData::getAcknowledgementOfService)
                .readonlyNoSummary(AcknowledgementOfService::getDateAosSubmitted, NEVER_SHOW)
            .done()
            .complex(CaseData::getConditionalOrder)
                .readonlyNoSummary(ConditionalOrder::getLastApprovedServiceApplicationIsBailiffApplication, NEVER_SHOW)
                .readonly(ConditionalOrder::getSuccessfulServedByBailiff,
                    "coLastApprovedServiceApplicationIsBailiffApplication=\"Yes\"")
                .readonly(ConditionalOrder::getCertificateOfServiceDate,
                    "coLastApprovedServiceApplicationIsBailiffApplication=\"Yes\"")
                .readonly(ConditionalOrder::getLastAlternativeServiceDocumentLink,
                    "applicationType=\"soleApplication\" AND coLastApprovedServiceApplicationIsBailiffApplication=\"No\""
                        + " AND dateAosSubmitted!=\"*\" AND coServiceConfirmed!=\"Yes\"")
                .readonly(ConditionalOrder::getRespondentAnswersLink,
                    "applicationType=\"soleApplication\" AND coLastApprovedServiceApplicationIsBailiffApplication=\"No\""
                        + " AND dateAosSubmitted=\"*\" AND coServiceConfirmed!=\"Yes\"")
            .readonly(ConditionalOrder::getServiceConfirmed, NEVER_SHOW)
                .optionalWithoutDefaultValue(ConditionalOrder::getProofOfServiceUploadDocuments,
                    "applicationType=\"soleApplication\" AND dateAosSubmitted!=\"*\" AND coServiceConfirmed=\"Yes\"",
                    "Please upload proof of service below")
                .label("CertificateOfServiceWarning",
                    "If you are progressing using a certificate of service, then you must upload proof of service here",
                    "applicationType=\"soleApplication\" AND dateAosSubmitted!=\"*\" AND coServiceConfirmed=\"Yes\"")
                .complex(ConditionalOrder::getConditionalOrderApplicant1Questions)
                    .mandatory(ConditionalOrderQuestions::getApplyForConditionalOrder)
                    .done()
                .done()
            .label(
                "ConditionalOrderReviewAoSNo",
                "You must select yes to apply for a conditional order",
                "coApplicant1ApplyForConditionalOrder=\"No\" AND applicationType=\"soleApplication\""
            );
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        log.info("Mid-event callback triggered for ConditionalOrderReviewAoS");

        CaseData data = details.getData();
        List<String> errors = new ArrayList<>();
        ConditionalOrder conditionalOrder = data.getConditionalOrder();

        if (data.getApplicationType().isSole()
            && !conditionalOrder.getConditionalOrderApplicant1Questions().getApplyForConditionalOrder().toBoolean()) {

            errors.add("Applicant must select yes to apply for a conditional order");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .errors(errors)
            .build();
    }
}
