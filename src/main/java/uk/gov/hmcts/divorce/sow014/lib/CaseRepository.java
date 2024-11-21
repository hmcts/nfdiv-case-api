package uk.gov.hmcts.divorce.sow014.lib;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface CaseRepository {

    ObjectNode getCase(long caseRef, ObjectNode data);
}
