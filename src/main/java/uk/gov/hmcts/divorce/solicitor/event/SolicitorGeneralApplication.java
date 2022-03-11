package uk.gov.hmcts.divorce.solicitor.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationPaymentConfirmation;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationPaymentSummary;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationSelectApplicationType;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationSelectFee;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationUploadDocument;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralApplicationReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_ISSUE_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
public class SolicitorGeneralApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_GENERAL_APPLICATION = "solicitor-general-application";

    @Autowired
    private GeneralApplicationSelectFee generalApplicationSelectFee;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);

        final List<CcdPageConfiguration> pages = asList(
            new GeneralApplicationSelectApplicationType(),
            generalApplicationSelectFee,
            new GeneralApplicationUploadDocument(),
            new GeneralApplicationPaymentConfirmation(),
            new GeneralApplicationPaymentSummary()
        );

        pages.forEach(page -> page.addTo(pageBuilder));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = details.getData();

        if (AwaitingPronouncement == details.getState()
            && !isEmpty(caseData.getBulkListCaseReference())) {

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList(
                    "General Application cannot be submitted as this case is currently linked to an active bulk action case"
                ))
                .build();
        }

        ListValue<DivorceDocument> generalApplicationDocument =
            ListValue.<DivorceDocument>builder()
                .id(String.valueOf(UUID.randomUUID()))
                .value(caseData.getGeneralApplication().getGeneralApplicationDocument())
                .build();

        caseData.addToDocumentsUploaded(generalApplicationDocument);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(GeneralApplicationReceived)
            .build();
    }

    private PageBuilder addEventConfig(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder
            .event(SOLICITOR_GENERAL_APPLICATION)
            .forStates(POST_ISSUE_STATES)
            .name("General Application")
            .description("General Application")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, SOLICITOR)
            .grant(READ, CASE_WORKER, LEGAL_ADVISOR, SUPER_USER));
    }
}
