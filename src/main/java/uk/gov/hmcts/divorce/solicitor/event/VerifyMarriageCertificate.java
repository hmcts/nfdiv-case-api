package uk.gov.hmcts.divorce.solicitor.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.MarriageDetails;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import static uk.gov.hmcts.divorce.common.model.State.Submitted;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_COURTADMIN_RDU;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.common.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_PREVIEW_BANNER;

@Component
public class VerifyMarriageCertificate implements CCDConfig<CaseData, State, UserRole> {
    public static final String VERIFY_MARRIAGE_CERTIFICATE_DETAILS = "verify-marriage-certificate";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(VERIFY_MARRIAGE_CERTIFICATE_DETAILS)
            .forStates(Submitted)
            .name("Verify marriage certificate")
            .description("Verify marriage certificate")
            .showSummary()
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, SOLICITOR, CASEWORKER_COURTADMIN_RDU)
            .grant(READ,
                CASEWORKER_SUPERUSER,
                CASEWORKER_COURTADMIN_CTSC,
                CASEWORKER_LEGAL_ADVISOR))
            .page("marriageCertificateDetailsVerification")
            .pageLabel("Marriage Certificate Details")
            .label("LabelNFDBanner-VerifyMarriageCert", SOLICITOR_NFD_PREVIEW_BANNER)
            .complex(CaseData::getMarriageDetails)
                .mandatory(MarriageDetails::getCertifyMarriageCertificateIsCorrect)
                .mandatory(MarriageDetails::getMarriageCertificateIsIncorrectDetails,"marriageCertifyMarriageCertificateIsCorrect=\"No\"")
                .mandatory(MarriageDetails::getIssueApplicationWithoutMarriageCertificate)
                .done();
    }
}
