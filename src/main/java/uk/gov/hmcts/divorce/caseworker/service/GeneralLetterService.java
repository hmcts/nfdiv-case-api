package uk.gov.hmcts.divorce.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateGeneralLetter;
import uk.gov.hmcts.divorce.caseworker.service.task.SendGeneralLetter;
import uk.gov.hmcts.divorce.caseworker.service.task.UploadGeneralLetterAttachments;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
public class GeneralLetterService {

    @Autowired
    private GenerateGeneralLetter generateGeneralLetter;

    @Autowired
    private SendGeneralLetter sendGeneralLetter;

    public CaseDetails<CaseData, State> processGeneralLetter(final CaseDetails<CaseData, State> caseDetails) {
        return caseTasks(
            generateGeneralLetter,
            sendGeneralLetter
        ).run(caseDetails);
    }
}
