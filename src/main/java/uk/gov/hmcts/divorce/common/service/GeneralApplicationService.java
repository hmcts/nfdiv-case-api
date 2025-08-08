package uk.gov.hmcts.divorce.common.service;

import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;

import java.util.List;
import java.util.Optional;

public class GeneralApplicationService {
    public Optional<GeneralApplication> findGeneralApplication(
        String serviceRequest,
        List<ListValue<GeneralApplication>> generalApplications
    ) {
        if (serviceRequest == null) {
            return Optional.empty();
        }

        return generalApplications.stream()
            .map(ListValue::getValue)
            .filter(application ->
                serviceRequest.equals(
                    Optional.ofNullable(application)
                        .map(GeneralApplication::getGeneralApplicationFee)
                        .map(FeeDetails::getServiceRequestReference)
                        .orElse(null)
                )
            ).findFirst();
    }
}
