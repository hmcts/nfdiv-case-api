package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.ccd.sdk.api.TypedPropertyGetter;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationD11JourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class GeneralApplicationD11ApplicantPage2 implements CcdPageConfiguration {

    private static final String PRE_ISSUE_STATES =
        "[STATE]=\"AwaitingHWFDecision\" OR [STATE]=\"AwaitingDocuments\" OR [STATE]=\"AwaitingRequestedInformation\" "
            + "OR [STATE]=\"NewPaperCase\" OR [STATE]=\"RequestedInformationSubmitted\" OR [STATE]=\"Submitted\"";

    private static final String POST_ISSUE_STATES =
        "[STATE]=\"Holding\" OR [STATE]=\"AwaitingAos\" OR [STATE]=\"AosDrafted\" OR [STATE]=\"AosOverdue\" "
            + "OR [STATE]=\"AwaitingService\" OR [STATE]=\"AwaitingServicePayment\" OR [STATE]=\"AwaitingServiceConsideration\" "
            + "OR [STATE]=\"AwaitingConditionalOrder\" OR [STATE]=\"ConditionalOrderPending\" OR [STATE]=\"ConditionalOrderDrafted\" "
            + "OR [STATE]=\"ConditionalOrderPronounced\" OR [STATE]=\"AwaitingFinalOrder\" OR [STATE]=\"FinalOrderRequested\" "
            + "OR [STATE]=\"FinalOrderPending\" OR [STATE]=\"FinalOrderComplete\" "
            + "OR [STATE]=\"AwaitingGeneralApplicationPayment\" OR [STATE]=\"GeneralApplicationReceived\" "
            + "OR [STATE]=\"AwaitingGeneralConsideration\" OR [STATE]=\"GeneralConsiderationComplete\" "
            + "OR [STATE]=\"AwaitingGeneralReferralPayment\" OR [STATE]=\"PendingHearingDate\" OR [STATE]=\"PendingHearingOutcome\"";

    private static final String SOLE_PRE_ISSUE = "(" + PRE_ISSUE_STATES + ") AND applicationType=\"soleApplication\"";
    private static final String JOINT_PRE_ISSUE = "(" + PRE_ISSUE_STATES + ") AND applicationType=\"jointApplication\"";

    private static final String SOLE_POST_ISSUE = "(" + POST_ISSUE_STATES + ") AND applicationType=\"soleApplication\"";
    private static final String JOINT_POST_ISSUE = "(" + POST_ISSUE_STATES + ") AND applicationType=\"jointApplication\"";

    private final TypedPropertyGetter<CaseData, Applicant> applicantRef;
    private final String otherApplicationCondition;
    private final String urgentCaseCondition;
    private final boolean isApplicant2Journey;

    public static final String OTHER_MORE_INFO_LABEL = """
        Please provide more information about the general application
        """;

    public static final String STATEMENT_LABEL = """
       Do you want to provide a statement or upload evidence?
        """;

    public static final String STATEMENT_HINT = """
        You should provide as much detail as you can. Your statement and any evidence you provide will help the court decide whether to
        grant your application.
        """;

    public GeneralApplicationD11ApplicantPage2(TypedPropertyGetter<CaseData, Applicant> applicantRef, String applicantFieldPrefix) {
        this.applicantRef = applicantRef;
        this.isApplicant2Journey = "applicant2".equals(applicantFieldPrefix);
        if (isApplicant2Journey) {
            this.otherApplicationCondition =
                applicantFieldPrefix + "GenAppRespondentSolePostIssue=\"OTHER\" OR "
                    + applicantFieldPrefix + "GenAppApplicantJointPreIssue=\"OTHER\"  OR "
                    + applicantFieldPrefix + "GenAppApplicantJointPostIssue=\"OTHER\"";
        } else {
            this.otherApplicationCondition =
                applicantFieldPrefix + "GenAppApplicantSolePreIssue=\"OTHER\" OR "
                    + applicantFieldPrefix + "GenAppApplicantJointPreIssue=\"OTHER\" OR "
                    + applicantFieldPrefix + "GenAppApplicantSolePostIssue=\"OTHER\" OR "
                    + applicantFieldPrefix + "GenAppApplicantJointPostIssue=\"OTHER\"";
        }
        this.urgentCaseCondition = applicantFieldPrefix + "GenAppUrgentCase=\"Yes\"";
    }

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("SolGenAppD11DetailsApplicant");

        if (isNotBlank(pageShowCondition)) {
            page.showCondition(pageShowCondition);
        }

        page.readonly(CaseData::getApplicationType, "[STATE]=\"NEVER_SHOW\"");
        var genApp = page.complex(applicantRef)
            .complex(Applicant::getInterimApplicationOptions)
            .complex(InterimApplicationOptions::getGeneralApplicationD11JourneyOptions);

        if (isApplicant2Journey) {
            genApp
                .mandatory(GeneralApplicationD11JourneyOptions::getRespondentSolePostIssue, SOLE_POST_ISSUE)
                .mandatory(GeneralApplicationD11JourneyOptions::getApplicantJointPostIssue, JOINT_POST_ISSUE)
                .mandatory(GeneralApplicationD11JourneyOptions::getApplicantJointPreIssue, JOINT_PRE_ISSUE);
        } else {
            genApp
                .mandatory(GeneralApplicationD11JourneyOptions::getApplicantSolePreIssue, SOLE_PRE_ISSUE)
                .mandatory(GeneralApplicationD11JourneyOptions::getApplicantJointPreIssue, JOINT_PRE_ISSUE)
                .mandatory(GeneralApplicationD11JourneyOptions::getApplicantSolePostIssue, SOLE_POST_ISSUE)
                .mandatory(GeneralApplicationD11JourneyOptions::getApplicantJointPostIssue, JOINT_POST_ISSUE);
        }
        genApp
            .mandatory(GeneralApplicationD11JourneyOptions::getTypeOtherDetails, otherApplicationCondition, null, OTHER_MORE_INFO_LABEL)
            .mandatory(GeneralApplicationD11JourneyOptions::getUrgentCase)
            .mandatory(GeneralApplicationD11JourneyOptions::getUrgentCaseReason, urgentCaseCondition)
            .mandatory(GeneralApplicationD11JourneyOptions::getReason)
            .done()
            .mandatory(InterimApplicationOptions::getInterimAppsCanUploadEvidence, ALWAYS_SHOW, null, STATEMENT_LABEL, STATEMENT_HINT)
            .done()
            .done();
    }
}
