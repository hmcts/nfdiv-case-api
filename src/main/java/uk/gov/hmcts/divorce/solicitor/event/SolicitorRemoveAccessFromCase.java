package uk.gov.hmcts.divorce.solicitor.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.divorce.solicitor.event.page.Applicant2ServiceDetails;
import uk.gov.hmcts.divorce.solicitor.event.page.FinancialOrders;
import uk.gov.hmcts.divorce.solicitor.event.page.JurisdictionApplyForDivorce;
import uk.gov.hmcts.divorce.solicitor.event.page.MarriageCertificateDetails;
import uk.gov.hmcts.divorce.solicitor.event.page.MarriageIrretrievablyBroken;
import uk.gov.hmcts.divorce.solicitor.event.page.OtherLegalProceedings;
import uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant1;
import uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant2;
import uk.gov.hmcts.divorce.solicitor.event.page.SolAboutTheSolicitor;
import uk.gov.hmcts.divorce.solicitor.event.page.SolHowDoYouWantToApplyForDivorce;
import uk.gov.hmcts.divorce.solicitor.event.page.UploadDocument;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorUpdateApplicationService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import static java.util.Arrays.asList;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.sortByNewest;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Archived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant1Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.PRE_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationReview;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class SolicitorRemoveAccessFromCase implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_REMOVE_ACCESS = "solicitor-remove-access";
    public static final String REPRESENTATIVE_REMOVED_LABEL = "# Representative removed";

    public static final String REPRESENTATIVE_REMOVED_STATUS_LABEL = """
        ### What happens next

        The court will consider your withdrawal request.""";
    public static final String IS_NO_LONGER_REPRESENTING = " is no longer representing ";
    public static final String IN_THIS_CASE = " in this case.";
    public static final String ALL_OTHER_PARTIES_HAVE_BEEN_NOTIFIED_ABOUT_THIS_CHANGE = " All other parties have been notified about this change\n\n";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(configBuilder
                .event(SOLICITOR_REMOVE_ACCESS)
                .forStates(POST_SUBMISSION_STATES)
                .name("Solicitor remove access")
                .description(SOLICITOR_REMOVE_ACCESS)
                .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR)
                .grantHistoryOnly(SUPER_USER)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
        );
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Solicitor update application about to submit callback invoked for Case Id: {}", details.getId());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        log.info("Applicant2 Apply For Final Order event submitted callback invoked for Case Id: {}", details.getId());

        StringBuilder legalRepAndLipNames = new StringBuilder();
        Map<String, List<String>> legalRepAndLipNameMapping = new HashMap<>();

        legalRepAndLipNames.append("\n");
        legalRepAndLipNameMapping.forEach((key, value) -> legalRepAndLipNames.append(key)
                .append(IS_NO_LONGER_REPRESENTING)
                .append(String.join(", ", value))
                .append(IN_THIS_CASE)
        );

        String representativeRemovedBodyPrefix = legalRepAndLipNames.append(
                        ALL_OTHER_PARTIES_HAVE_BEEN_NOTIFIED_ABOUT_THIS_CHANGE)
                .append(REPRESENTATIVE_REMOVED_STATUS_LABEL).toString();
        return SubmittedCallbackResponse.builder().confirmationHeader(
                REPRESENTATIVE_REMOVED_LABEL).confirmationBody(
                representativeRemovedBodyPrefix
        ).build();    }
}
