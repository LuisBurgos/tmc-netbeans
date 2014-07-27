package fi.helsinki.cs.tmc.data;

import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;
import fi.helsinki.cs.tmc.ui.TestResultWindow;

import java.util.List;

public final class ResultCollector {

    private List<TestCaseResult> testCaseResults;
    private ValidationResult validationResults;

    private boolean testCaseResultLock = true;
    private boolean validationResultLock = true;

    private boolean isReturnable;
    private Runnable submissionCallback;

    public synchronized void setValidationResult(final ValidationResult result) {

        this.validationResults = result;
        this.validationResultLock = false;

        if (!testCaseResultLock) {
            showAllResults();
        }
    }

    public synchronized void setTestCaseResults(final List<TestCaseResult> results) {

        this.testCaseResults = results;
        this.testCaseResultLock = false;

        if (!validationResultLock) {
            showAllResults();
        }
    }

    private synchronized void showAllResults() {

        this.validationResultLock = true;
        this.testCaseResultLock = true;
        this.isReturnable = false;
        this.submissionCallback = null;

        TestResultWindow.get().showResults(testCaseResults, validationResults, submissionCallback, isSubmittable());
    }

    public void setSubmissionCallback(final Runnable submissionCallback) {

        this.submissionCallback = submissionCallback;
    }

    public void setReturnable(final boolean returnable) {

        isReturnable = returnable;
    }

    private boolean isSubmittable() {

        for (TestCaseResult result : testCaseResults) {

            if (!result.isSuccessful()) {
                return false;
            }
        }

        if (!validationResults.getValidationErrors().isEmpty()) {
            return false;
        }

        return isReturnable;
    }
}