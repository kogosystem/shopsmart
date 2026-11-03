package com.shopsmart.recommendation;

/**
 * Holds the outcome of evaluating ONE method (either the personalised engine or
 * the popular-products baseline) so the admin page can show them side by side.
 *
 * TP / FP / FN are the raw counts; precision, recall and F1 are derived from
 * them. All are explained in simple words on the evaluation page.
 */
public class EvaluationResult {

    private final String methodName;
    private final long truePositives;    // recommended AND later bought  (good)
    private final long falsePositives;   // recommended but NOT bought     (wasted slot)
    private final long falseNegatives;   // bought but NOT recommended     (missed)
    private final int customersEvaluated;
    private final int topN;

    public EvaluationResult(String methodName, long tp, long fp, long fn,
                            int customersEvaluated, int topN) {
        this.methodName = methodName;
        this.truePositives = tp;
        this.falsePositives = fp;
        this.falseNegatives = fn;
        this.customersEvaluated = customersEvaluated;
        this.topN = topN;
    }

    /** Precision = TP / (TP + FP): of what we recommended, how much was actually wanted. */
    public double getPrecision() {
        long denom = truePositives + falsePositives;
        return denom == 0 ? 0.0 : (double) truePositives / denom;
    }

    /** Recall = TP / (TP + FN): of everything they later bought, how much we caught. */
    public double getRecall() {
        long denom = truePositives + falseNegatives;
        return denom == 0 ? 0.0 : (double) truePositives / denom;
    }

    /** F1 = harmonic mean of precision and recall (balances the two). */
    public double getF1Score() {
        double p = getPrecision();
        double r = getRecall();
        return (p + r) == 0 ? 0.0 : 2 * p * r / (p + r);
    }

    public String getMethodName() { return methodName; }
    public long getTruePositives() { return truePositives; }
    public long getFalsePositives() { return falsePositives; }
    public long getFalseNegatives() { return falseNegatives; }
    public int getCustomersEvaluated() { return customersEvaluated; }
    public int getTopN() { return topN; }

    // Convenience getters that round to whole percentages for display.
    public double getPrecisionPct() { return Math.round(getPrecision() * 1000) / 10.0; }
    public double getRecallPct() { return Math.round(getRecall() * 1000) / 10.0; }
    public double getF1Pct() { return Math.round(getF1Score() * 1000) / 10.0; }
}
