package uk.gov.hmcts.divorce.solicitor.event;

import jakarta.servlet.http.HttpServletRequest;
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
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationContactInformation;
import uk.gov.hmcts.divorce.solicitor.event.page.CheckApplicant1SolicitorAnswers;
import uk.gov.hmcts.divorce.solicitor.event.page.FinancialOrdersForApplicant2;
import uk.gov.hmcts.divorce.solicitor.event.page.HelpWithFeesPageForApplicant2;
import uk.gov.hmcts.divorce.solicitor.event.page.MarriageIrretrievablyBrokenForApplicant2;
import uk.gov.hmcts.divorce.solicitor.event.page.SolStatementOfTruthApplicant2;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorSubmitJointApplicationService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.List;

import static java.util.Arrays.asList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.util.SolicitorAddressPopulator.parseOrganisationAddress;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;

@Slf4j
@Component
public class SolicitorSubmitJointApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_SUBMIT_JOINT_APPLICATION = "solicitor-submit-joint-application";

    @Autowired
    private MarriageIrretrievablyBrokenForApplicant2 marriageIrretrievablyBrokenForApplicant2;

    @Autowired
    private HelpWithFeesPageForApplicant2 helpWithFeesPageForApplicant2;

    @Autowired
    private SolicitorSubmitJointApplicationService solicitorSubmitJointApplicationService;

    @Autowired
    private OrganisationClient organisationClient;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final List<CcdPageConfiguration> pages = asList(
            marriageIrretrievablyBrokenForApplicant2,
            new FinancialOrdersForApplicant2(),
            helpWithFeesPageForApplicant2,
            new CheckApplicant1SolicitorAnswers(),
            new SolStatementOfTruthApplicant2()
        );

        final PageBuilder pageBuilder = addEventConfig(configBuilder);

        pages.forEach(page -> page.addTo(pageBuilder));
    }

    private PageBuilder addEventConfig(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder.event(SOLICITOR_SUBMIT_JOINT_APPLICATION)
            .forStates(AwaitingApplicant2Response, Draft)
            .name("Submit joint application")
            .description("Submit joint application")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .showSummary()
            .endButtonLabel("Submit Application")
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR)
            .grantHistoryOnly(
                APPLICANT_1_SOLICITOR,
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("{} about to start callback invoked for Case Id: {}", SOLICITOR_SUBMIT_JOINT_APPLICATION, details.getId());
        CaseData data = details.getData();

        data.getDocuments()
            .getDocumentsGenerated()
            .stream()
            .filter(document -> APPLICATION.equals(document.getValue().getDocumentType()))
            .findFirst()
            .ifPresent(draftDivorceApplication ->
                data.getApplication().setApplicant1SolicitorAnswersLink(draftDivorceApplication.getValue().getDocumentLink())
            );
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked for Case Id: {}", SOLICITOR_SUBMIT_JOINT_APPLICATION, details.getId());
        CaseData data = details.getData();

        if (details.getData().getApplicant2().isRepresented()) {
            setApplicant2SolicitorAddress(details);
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

    public SubmittedCallbackResponse submitted(final CaseDetails<CaseData, State> details,
                                               final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Solicitor submit joint application submitted callback invoked for case id: {}", details.getId());

        solicitorSubmitJointApplicationService.submitEventForApprovalOrRequestingChanges(details);

        return SubmittedCallbackResponse.builder().build();
    }

    private void setApplicant2SolicitorAddress(CaseDetails<CaseData, State> caseDetails) {
        final List<OrganisationContactInformation> contactInformation = organisationClient
            .getUserOrganisation(request.getHeader(AUTHORIZATION), authTokenGenerator.generate())
            .getContactInformation();

        if (!isEmpty(contactInformation)) {
            final String solicitorAddress = parseOrganisationAddress(contactInformation);

            log.info("Setting Applicant 2 solicitor address.  Case ID: {}", caseDetails.getId());
            caseDetails.getData().getApplicant2().getSolicitor().setAddress(solicitorAddress);
        }
    }
}

