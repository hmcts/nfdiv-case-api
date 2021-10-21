package uk.gov.hmcts.divorce.solicitor.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaOrganisationResponse;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaRefDataClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@Slf4j
public class SolPayment implements CcdPageConfiguration {

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private IdamService idamService;

    @Autowired
    private PbaRefDataClient pbaRefDataClient;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Override
    public void addTo(final PageBuilder<CaseData, UserRole, State> pageBuilder) {

        pageBuilder
            //.page("SolPayment", this::midEvent) // Temporarily commenting as midevent has a bug which clears our fields
            .page("SolPayment")
            .pageLabel("Payment")
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
        log.info("Mid-event callback triggered for SolPayment page");

        CaseData caseData = details.getData();

        /* Temporarily disable this as it  will be triggered from about to start
        if (!caseData.getApplication().isSolicitorPaymentMethodPba()) {
            log.info("Payment method is not PBA for case id {}  :", details.getId());
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .build();
        }
        */

        List<DynamicListElement> pbaAccountNumbers = retrievePbaNumbers()
            .stream()
            .map(pbaNumber -> DynamicListElement.builder().label(pbaNumber).code(UUID.randomUUID()).build())
            .collect(Collectors.toList());

        DynamicList pbaNumbersDynamicList = DynamicList
            .builder()
            .value(DynamicListElement.builder().build())
            .listItems(pbaAccountNumbers)
            .build();

        log.info("DynamicList {}", pbaNumbersDynamicList);
        caseData.getApplication().setPbaNumbers(pbaNumbersDynamicList);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private List<String> retrievePbaNumbers() {
        String solicitorAuthToken = httpServletRequest.getHeader(AUTHORIZATION);
        UserDetails solUserDetails = idamService.retrieveUser(solicitorAuthToken).getUserDetails();
        String solicitorEmail = solUserDetails.getEmail();

        ResponseEntity<PbaOrganisationResponse> responseEntity =
            pbaRefDataClient.retrievePbaNumbers(solicitorAuthToken, authTokenGenerator.generate(), solicitorEmail);

        PbaOrganisationResponse pbaOrganisationResponse = Objects.requireNonNull(responseEntity.getBody());

        return pbaOrganisationResponse.getOrganisationEntityResponse().getPaymentAccount();
    }
}
