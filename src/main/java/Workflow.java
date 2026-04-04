import java.util.HashMap;
import java.util.Map;

public class Workflow {

    public enum State {
        DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, FINALIZED
    }

    // Valid transitions map
    private static final Map<State, State[]> VALID_TRANSITIONS = new HashMap<>();

    static {
        VALID_TRANSITIONS.put(State.DRAFT,        new State[]{State.SUBMITTED});
        VALID_TRANSITIONS.put(State.SUBMITTED,    new State[]{State.UNDER_REVIEW, State.REJECTED});
        VALID_TRANSITIONS.put(State.UNDER_REVIEW, new State[]{State.APPROVED, State.REJECTED});
        VALID_TRANSITIONS.put(State.APPROVED,     new State[]{State.FINALIZED});
        VALID_TRANSITIONS.put(State.REJECTED,     new State[]{State.DRAFT});
        VALID_TRANSITIONS.put(State.FINALIZED,    new State[]{});
    }

    private String documentId;
    private State currentState;
    private String comments;

    public Workflow(String documentId) {
        this.documentId = documentId;
        this.currentState = State.DRAFT;
        this.comments = "";
    }

    /**
     * Attempts a state transition. Returns true if successful.
     */
    public boolean transition(State targetState) {
        State[] allowed = VALID_TRANSITIONS.get(currentState);
        for (State s : allowed) {
            if (s == targetState) {
                currentState = targetState;
                return true;
            }
        }
        throw new IllegalStateException(
            "Invalid transition from " + currentState + " to " + targetState
        );
    }

    public boolean approve() {
        return transition(State.APPROVED);
    }

    public boolean reject(String reason) {
        this.comments = reason;
        return transition(State.REJECTED);
    }

    public boolean submit() {
        return transition(State.SUBMITTED);
    }

    public boolean startReview() {
        return transition(State.UNDER_REVIEW);
    }

    public boolean finalizeDoc() {
        return transition(State.FINALIZED);
    }

    public boolean reopen() {
        return transition(State.DRAFT);
    }

    public State getCurrentState() {
        return currentState;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getComments() {
        return comments;
    }

    public static void main(String[] args) {
        System.out.println("=== Document Approval Workflow System ===");

        Workflow doc = new Workflow("DOC-001");
        System.out.println("Document: " + doc.getDocumentId());
        System.out.println("Initial State: " + doc.getCurrentState());

        doc.submit();
        System.out.println("After Submit: " + doc.getCurrentState());

        doc.startReview();
        System.out.println("After Review Start: " + doc.getCurrentState());

        doc.approve();
        System.out.println("After Approval: " + doc.getCurrentState());

        doc.finalizeDoc();
        System.out.println("After Finalize: " + doc.getCurrentState());

        System.out.println("\n=== Rejection Flow ===");
        Workflow doc2 = new Workflow("DOC-002");
        doc2.submit();
        doc2.startReview();
        doc2.reject("Missing signatures on page 3");
        System.out.println("After Rejection: " + doc2.getCurrentState());
        System.out.println("Rejection Reason: " + doc2.getComments());

        doc2.reopen();
        System.out.println("After Reopen: " + doc2.getCurrentState());
    }
}