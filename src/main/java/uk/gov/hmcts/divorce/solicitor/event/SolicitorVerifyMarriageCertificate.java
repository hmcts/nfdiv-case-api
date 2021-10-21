package uk.gov.hmcts.divorce.solicitor.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
public class SolicitorVerifyMarriageCertificate implements CCDConfig<CaseData, State, UserRole> {
    public static final String SOLICITOR_VERIFY_CERTIFICATE = "solicitor-verify-certificate";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder<>(configBuilder
            .event(SOLICITOR_VERIFY_CERTIFICATE)
            .forStates(Submitted)
            .name("Verify marriage certificate")
            .description("Verify marriage certificate")
            .showSummary()
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, SOLICITOR)
            .grant(READ,
                SUPER_USER,
                CASE_WORKER,
                LEGAL_ADVISOR))
            .page("marriageCertificateDetailsVerification")
            .pageLabel("Marriage Certificate Details")
            .complex(CaseData::getApplication)
                .complex(Application::getMarriageDetails)
                    .mandatory(MarriageDetails::getCertifyMarriageCertificateIsCorrect)
                    .mandatory(
                        MarriageDetails::getMarriageCertificateIsIncorrectDetails,
                        "marriageCertifyMarriageCertificateIsCorrect=\"No\"")
                    .mandatory(MarriageDetails::getIssueApplicationWithoutMarriageCertificate)
                    .done()
                .done();
    }
}
