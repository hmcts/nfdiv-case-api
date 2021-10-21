package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.notification.GeneralEmailNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralEmail;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
@Slf4j
public class CaseworkerGeneralEmail implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_CREATE_GENERAL_EMAIL = "caseworker-create-general-email";

    @Autowired
    private GeneralEmailNotification generalEmailNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder<>(configBuilder
            .event(CASEWORKER_CREATE_GENERAL_EMAIL)
            .forAllStates()
            .name("Create general email")
            .description("Create general email")
            .explicitGrants()
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grant(READ, SUPER_USER, LEGAL_ADVISOR, SOLICITOR, CITIZEN))
            .page("createGeneralEmail")
            .pageLabel("Create general email")
            .complex(CaseData::getGeneralEmail)
                .mandatory(GeneralEmail::getGeneralEmailParties)
                .mandatory(GeneralEmail::getGeneralEmailOtherRecipientEmail, "generalEmailParties=\"other\"")
                .mandatory(GeneralEmail::getGeneralEmailOtherRecipientName, "generalEmailParties=\"other\"")
                .mandatory(GeneralEmail::getGeneralEmailDetails)
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker create general email about to submit callback invoked");

        var caseData = details.getData();

        generalEmailNotification.send(caseData, details.getId());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
