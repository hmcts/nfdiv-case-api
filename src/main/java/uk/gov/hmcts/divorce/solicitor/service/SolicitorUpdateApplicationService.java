package uk.gov.hmcts.divorce.solicitor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.task.DivorceApplicationRemover;
import uk.gov.hmcts.divorce.solicitor.service.task.DivorceApplicationDraft;
import uk.gov.hmcts.divorce.solicitor.service.task.SetApplicant1SolicitorAddress;
import uk.gov.hmcts.divorce.solicitor.service.task.SetApplicantAddresses;
import uk.gov.hmcts.divorce.solicitor.service.task.SetApplicantGender;

import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
@RequiredArgsConstructor
public class SolicitorUpdateApplicationService {

    private final DivorceApplicationRemover divorceApplicationRemover;

    private final DivorceApplicationDraft divorceApplicationDraft;

    private final SetApplicant1SolicitorAddress setApplicant1SolicitorAddress;

    private final SetApplicantAddresses setApplicantAddresses;

    private final SetApplicantGender setApplicantGender;

    public CaseDetails<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> caseDetails) {

        return caseTasks(
            setApplicant1SolicitorAddress,
            setApplicantAddresses,
            divorceApplicationRemover,
            divorceApplicationDraft,
            setApplicantGender
        ).run(caseDetails);
    }
}
