package uk.gov.hmcts.divorce.solicitor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod.FEE_PAY_BY_HWF;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEES_HELP_WITH;

@Service
@RequiredArgsConstructor
public class ServiceApplicationFactory {

    private final Clock clock;

    public AlternativeService createFromInterimOptions(InterimApplicationOptions options) {
        List<ListValue<DivorceDocument>> docs = options.getInterimAppsEvidenceDocs();

        AlternativeService serviceApplication = AlternativeService.builder()
            .receivedServiceApplicationDate(LocalDate.now(clock))
            .receivedServiceAddedDate(LocalDate.now(clock))
            .alternativeServiceType(options.getInterimApplicationType().getServiceType())
            .serviceApplicationSubmittedOnline(YesOrNo.YES)
            .serviceApplicationDocsUploadedPreSubmission(YesOrNo.from(docs != null && !docs.isEmpty()))
            .serviceApplicationDocuments(docs)
            .alternativeServiceFeeRequired(YesOrNo.YES)
            .build();

        serviceApplication.getServicePaymentFee().setPaymentMethod(
            FEES_HELP_WITH.equals(options.getInterimAppsPaymentMethod()) ? FEE_PAY_BY_HWF : FEE_PAY_BY_ACCOUNT
        );

        return serviceApplication;
    }
}
