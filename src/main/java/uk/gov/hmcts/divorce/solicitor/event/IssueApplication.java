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
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_PREVIEW_BANNER;

@Component
public class IssueApplication implements CCDConfig<CaseData, State, UserRole> {
    public static final String ISSUE_APPLICATION = "issue-application";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(ISSUE_APPLICATION)
            .forStates(Submitted)
            .name("Issue Application")
            .description("Issue Application")
            .showSummary()
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASEWORKER_DIVORCE_COURTADMIN)
            .grant(READ,
                CASEWORKER_DIVORCE_SOLICITOR,
                CASEWORKER_DIVORCE_SUPERUSER,
                CASEWORKER_DIVORCE_COURTADMIN_BETA,
                CASEWORKER_DIVORCE_COURTADMIN_LA))
            .page("issueApplication")
            .pageLabel("Issue Divorce Application")
            .label("LabelNFDBanner-MarriageIrretrievablyBroken", SOLICITOR_NFD_PREVIEW_BANNER)
            .complex(CaseData::getMarriageDetails)
                .optional(MarriageDetails::getDate)
                .optional(MarriageDetails::getApplicant1Name)
                .optional(MarriageDetails::getApplicant2Name)
                .mandatory(MarriageDetails::getPlaceOfMarriage)
                .done();
    }
}
