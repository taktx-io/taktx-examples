package io.taktx.app.workers;

import io.quarkus.runtime.Startup;
import io.taktx.client.annotation.AckStrategy;
import io.taktx.client.annotation.JobWorker;
import io.taktx.client.annotation.ThreadingStrategy;
import io.taktx.client.annotation.Variable;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Workers that handle service tasks in the DMN example processes:
 *
 * <ul>
 *   <li>{@code dmn-single-table} – uses the "creditRiskAssessment" single-table DMN
 *   <li>{@code dmn-drg} – uses the "loanApproval" DRG (Decision Requirements Graph)
 * </ul>
 *
 * <p>Both processes share the same task types, so a single worker class handles both:
 *
 * <ul>
 *   <li>{@code disburse-loan} – called when the DMN approves a loan outright
 *   <li>{@code approve-loan} – called by the DRG process for APPROVED / APPROVED_CONDITIONAL
 *   <li>{@code assign-manual-review} – called when a human reviewer is required
 *   <li>{@code send-rejection-notice} – called when the loan is rejected
 * </ul>
 */
@Startup
@ApplicationScoped
public class DmnExampleWorker {

  private static final Logger logger = LoggerFactory.getLogger(DmnExampleWorker.class);

  // ─────────────────────────────────────────────────────────────────────────
  // dmn-single-table process workers
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * Handles the "Disburse Loan" service task in the {@code dmn-single-table} process.
   *
   * <p>Receives the full DMN result object stored in {@code creditRiskResult}:
   * {@code { riskCategory, approved, requiresManualReview }}.
   *
   * @param creditScore      the applicant credit score passed into the DMN
   * @param loanAmount       the requested loan amount passed into the DMN
   * @param creditRiskResult the DMN evaluation result map
   * @return a map with disbursement confirmation fields
   */
  @JobWorker(
      type = "disburse-loan",
      autoComplete = true,
      threadingStrategy = ThreadingStrategy.VIRTUAL_THREAD_FIRE_AND_FORGET,
      ackStrategy = AckStrategy.IMPLICIT)
  public Map<String, Object> disburseLoan(
      int creditScore,
      int loanAmount,
      @Variable("creditRiskResult") Map<String, Object> creditRiskResult) {

    logger.info(
        """
        [disburse-loan] Disbursing loan:
          creditScore      = {}
          loanAmount       = {}
          riskCategory     = {}
          approved         = {}
        """,
        creditScore,
        loanAmount,
        creditRiskResult.get("riskCategory"),
        creditRiskResult.get("approved"));

    return Map.of(
        "disbursementStatus", "COMPLETED",
        "disbursedAmount", loanAmount,
        "riskCategory", creditRiskResult.get("riskCategory"));
  }

  // ─────────────────────────────────────────────────────────────────────────
  // dmn-drg process workers
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * Handles the "Approve Loan" and "Approve with Conditions" service tasks in the
   * {@code dmn-drg} process.
   *
   * <p>Receives the DRG result stored in {@code loanApprovalResult}:
   * {@code { approvalDecision, interestRate }}.
   *
   * @param creditScore        the applicant credit score passed into the DRG
   * @param debtToIncomeRatio  the debt-to-income ratio passed into the DRG
   * @param loanApprovalResult the top-level DRG decision result map
   * @return a map with approval confirmation fields
   */
  @JobWorker(
      type = "approve-loan",
      autoComplete = true,
      threadingStrategy = ThreadingStrategy.VIRTUAL_THREAD_FIRE_AND_FORGET,
      ackStrategy = AckStrategy.IMPLICIT)
  public Map<String, Object> approveLoan(
      int creditScore,
      double debtToIncomeRatio,
      @Variable("loanApprovalResult") Map<String, Object> loanApprovalResult) {

    logger.info(
        """
        [approve-loan] Approving loan:
          creditScore       = {}
          debtToIncomeRatio = {}
          approvalDecision  = {}
          interestRate      = {}%
        """,
        creditScore,
        debtToIncomeRatio,
        loanApprovalResult.get("approvalDecision"),
        loanApprovalResult.get("interestRate"));

    return Map.of(
        "approvalStatus", "APPROVED",
        "approvalDecision", loanApprovalResult.get("approvalDecision"),
        "offeredInterestRate", loanApprovalResult.get("interestRate"));
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Shared workers (used by both dmn-single-table and dmn-drg)
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * Handles the "Assign to Reviewer" / "Manual Review" service tasks.
   *
   * <p>Shared between {@code dmn-single-table} (creditRiskResult) and
   * {@code dmn-drg} (loanApprovalResult) processes.
   *
   * @param allVariables all process variables (contains whichever result variable is present)
   * @return a map with the review assignment details
   */
  @JobWorker(
      type = "assign-manual-review",
      autoComplete = true,
      threadingStrategy = ThreadingStrategy.VIRTUAL_THREAD_FIRE_AND_FORGET,
      ackStrategy = AckStrategy.IMPLICIT)
  public Map<String, Object> assignManualReview(Map<String, Object> allVariables) {
    logger.info(
        "[assign-manual-review] Assigning application for manual review. Variables: {}",
        allVariables);

    return Map.of(
        "reviewStatus", "ASSIGNED",
        "reviewQueue", "CREDIT_OPERATIONS",
        "reviewPriority", "NORMAL");
  }

  /**
   * Handles the "Send Rejection Notice" service task.
   *
   * <p>Shared between {@code dmn-single-table} (creditRiskResult) and
   * {@code dmn-drg} (loanApprovalResult) processes.
   *
   * @param allVariables all process variables (contains whichever result variable is present)
   * @return a map with the rejection notification details
   */
  @JobWorker(
      type = "send-rejection-notice",
      autoComplete = true,
      threadingStrategy = ThreadingStrategy.VIRTUAL_THREAD_FIRE_AND_FORGET,
      ackStrategy = AckStrategy.IMPLICIT)
  public Map<String, Object> sendRejectionNotice(Map<String, Object> allVariables) {
    logger.info(
        "[send-rejection-notice] Sending rejection notice. Variables: {}", allVariables);

    return Map.of(
        "notificationStatus", "SENT",
        "notificationType", "LOAN_REJECTION",
        "channel", "EMAIL");
  }
}

