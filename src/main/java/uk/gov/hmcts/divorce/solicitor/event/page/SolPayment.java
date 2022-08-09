package uk.gov.hmcts.divorce.solicitor.event.page;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaService;

@Component
@Slf4j
public class SolPayment implements CcdPageConfiguration {

    @Autowired
    private PbaService pbaService;

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolPayment", this::midEvent)
            .page("SolPayment")
            .pageLabel("Payment for this application")
            .label(
                "LabelSolPaymentPara-1",
                "Amount to pay: **£${solApplicationFeeInPounds}**")
            .complex(CaseData::getApplication)
            .mandatory(Application::getSolPaymentHowToPay)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {

        final Long caseId = details.getId();
        log.info("Mid-event callback triggered for SolPayment page Case Id: {}", caseId);

        CaseData caseData = details.getData();

        if (!caseData.getApplication().isSolicitorPaymentMethodPba()) {
            log.info("Payment method is not PBA for case id {}  :", caseId);
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .build();
        }

        try {
            DynamicList pbaNumbersDynamicList = pbaService.populatePbaDynamicList();

            log.info("DynamicList {}, Case Id: {}", pbaNumbersDynamicList, caseId);
            caseData.getApplication().setPbaNumbers(pbaNumbersDynamicList);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .build();
        } catch (final FeignException e) {
            log.error("Failed to retrieve PBA numbers for Case Id: {}", caseId);
            throw e;
        }
    }
}
