package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Bailiff;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.CertificateOfServiceContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingBailiffService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.IssuedToBailiff;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_SERVICE_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_SERVICE_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_SERVICE;

@Component
@Slf4j
public class CaseworkerIssueBailiffPack implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_ISSUE_BAILIFF_PACK = "caseworker-issue-bailiff-pack";

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private CertificateOfServiceContent certificateOfServiceContent;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder<>(configBuilder
            .event(CASEWORKER_ISSUE_BAILIFF_PACK)
            .forStateTransition(AwaitingBailiffService, IssuedToBailiff)
            .name("Issue bailiff pack")
            .description("Issue bailiff pack")
            .explicitGrants()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grant(READ, SUPER_USER, LEGAL_ADVISOR, SOLICITOR, CITIZEN))
            .page("issueBailiffPack")
            .pageLabel("Local court details - Issue Bailiff Pack")
            .label("localCourtDetailsIntro",
                "Caseworker will send the Bailiff application together with the divorce application, "
                    + "invitation letter for the respondent and a certificate of service to the local court."
            )
            .label("localCourtDetailsLabel", "### Local court details")
            .complex(CaseData::getBailiff)
                .mandatory(Bailiff::getLocalCourtName)
                .mandatory(Bailiff::getLocalCourtEmail)
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        var caseDataCopy = details.getData().toBuilder().build();

        final Long caseId = details.getId();

        log.info(
            "Generating certificate of service document for templateId : {} case and caseId: {}",
            CERTIFICATE_OF_SERVICE_TEMPLATE_ID,
            caseId
        );

        var certificateOfServiceDoc = caseDataDocumentService.renderDocument(
            certificateOfServiceContent.apply(caseDataCopy, caseId),
            caseId,
            CERTIFICATE_OF_SERVICE_TEMPLATE_ID,
            caseDataCopy.getApplicant1().getLanguagePreference(),
            CERTIFICATE_OF_SERVICE_DOCUMENT_NAME
        );

        var cosDivorceDocument = DivorceDocument
            .builder()
            .documentLink(certificateOfServiceDoc)
            .documentFileName(certificateOfServiceDoc.getFilename())
            .documentType(CERTIFICATE_OF_SERVICE)
            .build();

        caseDataCopy.getBailiff().setCertificateOfServiceDocument(cosDivorceDocument);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseDataCopy)
            .build();
    }
}
