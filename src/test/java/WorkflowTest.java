import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WorkflowTest {

    private Workflow workflow;

    @BeforeEach
    void setUp() {
        workflow = new Workflow("TEST-DOC-001");
    }

    // ─────────────────────────────────────────────
    // INITIAL STATE TESTS
    // ─────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("Document starts in DRAFT state")
    void testInitialStateDraft() {
        assertEquals(Workflow.State.DRAFT, workflow.getCurrentState());
    }

    @Test
    @Order(2)
    @DisplayName("Document ID is set correctly")
    void testDocumentIdAssigned() {
        assertEquals("TEST-DOC-001", workflow.getDocumentId());
    }

    // ─────────────────────────────────────────────
    // HAPPY PATH – FULL APPROVAL FLOW
    // ─────────────────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("Full approval flow: DRAFT → SUBMITTED → UNDER_REVIEW → APPROVED → FINALIZED")
    void testFullApprovalFlow() {
        assertTrue(workflow.submit());
        assertEquals(Workflow.State.SUBMITTED, workflow.getCurrentState());

        assertTrue(workflow.startReview());
        assertEquals(Workflow.State.UNDER_REVIEW, workflow.getCurrentState());

        assertTrue(workflow.approve());
        assertEquals(Workflow.State.APPROVED, workflow.getCurrentState());

        assertTrue(workflow.finalizeDoc());
        assertEquals(Workflow.State.FINALIZED, workflow.getCurrentState());
    }

    // ─────────────────────────────────────────────
    // REJECTION & REOPEN FLOW
    // ─────────────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("Rejection from UNDER_REVIEW transitions to REJECTED")
    void testRejectionFromUnderReview() {
        workflow.submit();
        workflow.startReview();
        assertTrue(workflow.reject("Incomplete data on page 2"));
        assertEquals(Workflow.State.REJECTED, workflow.getCurrentState());
    }

    @Test
    @Order(5)
    @DisplayName("Rejection stores reason/comments")
    void testRejectionCommentStored() {
        workflow.submit();
        workflow.startReview();
        workflow.reject("Missing CFO signature");
        assertEquals("Missing CFO signature", workflow.getComments());
    }

    @Test
    @Order(6)
    @DisplayName("Rejected document can be reopened to DRAFT")
    void testRejectedReopened() {
        workflow.submit();
        workflow.startReview();
        workflow.reject("Formatting issues");
        assertTrue(workflow.reopen());
        assertEquals(Workflow.State.DRAFT, workflow.getCurrentState());
    }

    @Test
    @Order(7)
    @DisplayName("Rejection from SUBMITTED transitions to REJECTED")
    void testRejectionFromSubmitted() {
        workflow.submit();
        assertTrue(workflow.reject("Non-compliant format"));
        assertEquals(Workflow.State.REJECTED, workflow.getCurrentState());
    }

    // ─────────────────────────────────────────────
    // INVALID TRANSITION TESTS
    // ─────────────────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("Cannot approve from DRAFT - throws IllegalStateException")
    void testCannotApproveFromDraft() {
        assertThrows(IllegalStateException.class, () -> workflow.approve());
    }

    @Test
    @Order(9)
    @DisplayName("Cannot finalize from SUBMITTED - throws IllegalStateException")
    void testCannotFinalizeFromSubmitted() {
        workflow.submit();
        assertThrows(IllegalStateException.class, () -> workflow.finalizeDoc());
    }

    @Test
    @Order(10)
    @DisplayName("Cannot submit from FINALIZED - throws IllegalStateException")
    void testCannotSubmitFromFinalized() {
        workflow.submit();
        workflow.startReview();
        workflow.approve();
        workflow.finalizeDoc();
        assertThrows(IllegalStateException.class, () -> workflow.submit());
    }

    @Test
    @Order(11)
    @DisplayName("Cannot approve from SUBMITTED directly - throws IllegalStateException")
    void testCannotApproveFromSubmitted() {
        workflow.submit();
        assertThrows(IllegalStateException.class, () -> workflow.approve());
    }

    @Test
    @Order(12)
    @DisplayName("Cannot reopen from APPROVED - throws IllegalStateException")
    void testCannotReopenFromApproved() {
        workflow.submit();
        workflow.startReview();
        workflow.approve();
        assertThrows(IllegalStateException.class, () -> workflow.reopen());
    }

    @Test
    @Order(13)
    @DisplayName("Cannot start review from DRAFT - throws IllegalStateException")
    void testCannotReviewFromDraft() {
        assertThrows(IllegalStateException.class, () -> workflow.startReview());
    }

    @Test
    @Order(14)
    @DisplayName("Cannot transition from FINALIZED at all - throws IllegalStateException")
    void testNoTransitionsFromFinalized() {
        workflow.submit();
        workflow.startReview();
        workflow.approve();
        workflow.finalizeDoc();
        assertThrows(IllegalStateException.class, () -> workflow.reject("Too late"));
    }

    // ─────────────────────────────────────────────
    // EDGE CASES
    // ─────────────────────────────────────────────

    @Test
    @Order(15)
    @DisplayName("Reopen after rejection allows re-submission")
    void testResubmissionAfterRejection() {
        workflow.submit();
        workflow.reject("Needs revision");
        workflow.reopen();
        assertTrue(workflow.submit());
        assertEquals(Workflow.State.SUBMITTED, workflow.getCurrentState());
    }

    @Test
    @Order(16)
    @DisplayName("Multiple documents are independent")
    void testMultipleDocumentsIndependent() {
        Workflow doc1 = new Workflow("DOC-A");
        Workflow doc2 = new Workflow("DOC-B");

        doc1.submit();
        doc1.startReview();
        doc1.approve();

        assertEquals(Workflow.State.APPROVED, doc1.getCurrentState());
        assertEquals(Workflow.State.DRAFT,    doc2.getCurrentState());
    }
}