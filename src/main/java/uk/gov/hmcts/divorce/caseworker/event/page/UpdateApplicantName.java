package uk.gov.hmcts.divorce.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.*;

import static java.util.Collections.singletonList;

@Component
@Slf4j
public class UpdateApplicantName implements CcdPageConfiguration {
    public static final String TITLE = "Update applicant name";
    private static final String PAGE_ID = "CaseworkerUpdateApplicantName";
    private static final String FIRST_NAME_LABEL = "${%s} first name";
    private static final String MIDDLE_NAME_LABEL = "${%s} middle name";
    private static final String LAST_NAME_LABEL = "${%s} last name";
    private static final String APPLICANTS_OR_APPLICANT1S = "labelContentApplicantsOrApplicant1s";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        FieldCollection.FieldCollectionBuilder<CaseData, State, Event.EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder
            = pageBuilder
            .page(PAGE_ID, this::midEvent)
            .pageLabel(TITLE)
            .complex(CaseData::getLabelContent)
            .done();
        buildApplicantFields(fieldCollectionBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(final CaseDetails<CaseData, State> details,
                                                                  final CaseDetails<CaseData, State> detailsBefore) {
        log.info("Start callback invoked for Case Id: {}", details.getId());

        CaseData caseData = details.getData();
        if (!validApplicantName(caseData)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(singletonList("Firstname and Lastname should have atleast 3 characters each."))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    protected boolean validApplicantName(CaseData caseData) {
        if (caseData.getApplicant1().getFirstName().length() <= 3 || caseData.getApplicant1().getLastName().length() <= 3) {
            return false;
        }
        return true;
    }

    private void buildApplicantFields(final FieldCollection.FieldCollectionBuilder<CaseData, State,
        Event.EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {
        fieldCollectionBuilder
            .complex(CaseData::getApplicant1)
            .mandatoryWithLabel(Applicant::getFirstName, getLabel(FIRST_NAME_LABEL, APPLICANTS_OR_APPLICANT1S))
            .optionalWithLabel(Applicant::getMiddleName, getLabel(MIDDLE_NAME_LABEL, APPLICANTS_OR_APPLICANT1S))
            .mandatoryWithLabel(Applicant::getLastName, getLabel(LAST_NAME_LABEL, APPLICANTS_OR_APPLICANT1S))
            .done();
    }

    private String getLabel(final String label, final Object... value) {
        return String.format(label, value);
    }
}
