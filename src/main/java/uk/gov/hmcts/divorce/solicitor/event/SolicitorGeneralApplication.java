package uk.gov.hmcts.divorce.solicitor.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationPaymentConfirmation;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationPaymentSummary;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationSelectApplicationType;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationSelectFee;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationUploadDocument;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.addDocumentToTop;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralApplicationReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_ISSUE_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.PRE_GENERAL_APPLICATION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

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

        final CaseData data = details.getData();

        if (AwaitingPronouncement == details.getState()
            && !isEmpty(data.getBulkListCaseReference())) {

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList(
                    "General Application cannot be submitted as this case is currently linked to an active bulk action case"
                ))
                .build();
        }

        data.getDocuments().setDocumentsUploaded(
            addDocumentToTop(data.getDocuments().getDocumentsUploaded(), data.getGeneralApplication().getGeneralApplicationDocument())
        );

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(GeneralApplicationReceived)
            .build();
    }

    private PageBuilder addEventConfig(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder
            .event(SOLICITOR_GENERAL_APPLICATION)
            .forStates(PRE_GENERAL_APPLICATION_STATES)
            .name("General Application")
            .description("General Application")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR));
    }
}
