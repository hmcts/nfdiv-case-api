package uk.gov.hmcts.reform.divorce.caseapi.service.solicitor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.caseapi.util.Handler;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.OrganisationPolicy;

import java.util.Optional;
import java.util.function.BiConsumer;

import static java.util.Optional.ofNullable;

@Component
public class SolicitorOrganisationPolicyReference implements Handler<CaseData> {

    private final BiConsumer<Optional<String>, Optional<OrganisationPolicy>> setPolicyReference = (reference, policy) -> {
        reference.ifPresent(referenceValue ->
            policy.ifPresent(organisationPolicy ->
                organisationPolicy.setOrgPolicyReference(referenceValue)));
    };

    @Override
    public void handle(final CaseData caseData) {

        setPolicyReference.accept(
            ofNullable(caseData.getSolicitorReference()),
            ofNullable(caseData.getPetitionerOrganisationPolicy()));

        setPolicyReference.accept(
            ofNullable(caseData.getRespondentSolicitorReference()),
            ofNullable(caseData.getRespondentOrganisationPolicy()));
    }
}
