package uk.gov.hmcts.divorce.bulkaction.task;

public class BulkActionCaseTaskException extends RuntimeException {

    private static final long serialVersionUID = 8390188957636657171L;

    public BulkActionCaseTaskException(final String message) {
        super(message);
    }
}
