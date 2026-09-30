package uk.gov.hmcts.divorce.solicitor.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;

@Service
public class GeneralApplicationTypeOptionsService {

    public DynamicList buildOptions(State state, CaseData data, DynamicList existingSelection) {
        boolean postIssue = state != Draft;
        boolean sole = data.getApplicationType() == ApplicationType.SOLE_APPLICATION;
        boolean respondentSolicitor = isRespondentSolicitor(data);

        List<GeneralApplicationType> allowed = allowedTypes(postIssue, sole, respondentSolicitor);

        List<DynamicListElement> elements = allowed.stream()
            .map(t -> DynamicListElement.builder()
                .code(UUID.randomUUID())
                .label(t.getLabel())
                .build())
            .toList();

        String existingLabel = existingSelection != null && existingSelection.getValue() != null
            ? existingSelection.getValue().getLabel()
            : null;

        DynamicListElement selected = elements.stream()
            .filter(e -> e.getLabel().equals(existingLabel))
            .findFirst()
            .orElse(null);

        return DynamicList.builder()
            .listItems(elements)
            .value(selected)
            .build();
    }

    private boolean isRespondentSolicitor(CaseData data) {
        Applicant applicant2 = data.getApplicant2();
        return data.getApplicationType() == ApplicationType.SOLE_APPLICATION
            && null != applicant2
            && YesOrNo.YES.equals(applicant2.getSolicitorRepresented());
    }

    private List<GeneralApplicationType> allowedTypes(boolean postIssue, boolean sole, boolean respondentSolicitor) {
        if (respondentSolicitor && postIssue && sole) {
            return List.of(
                GeneralApplicationType.WITHDRAW_POST_ISSUE,
                GeneralApplicationType.DELAY,
                GeneralApplicationType.EXPEDITE,
                GeneralApplicationType.OTHER
            );
        }

        List<GeneralApplicationType> out = new ArrayList<>();
        out.add(GeneralApplicationType.WITHDRAW_POST_ISSUE);
        out.add(postIssue ? GeneralApplicationType.DELAY : GeneralApplicationType.ISSUE_DIVORCE_WITHOUT_CERT);

        if (sole) {
            out.add(GeneralApplicationType.EXTEND);
        }

        out.add(GeneralApplicationType.EXPEDITE);
        out.add(GeneralApplicationType.AMEND_APPLICATION);
        out.add(GeneralApplicationType.OTHER);
        return out;
    }
}
