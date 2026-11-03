package com.shopsmart;

import com.shopsmart.recommendation.EvaluationResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit test of the evaluation maths - no Spring, no database.
 * It pins down precision, recall and F1 so a future change to the formulas
 * cannot silently break the research metrics.
 */
class EvaluationResultTest {

    @Test
    void computesPrecisionRecallF1FromCounts() {
        // 3 correct, 1 wrong recommendation, 2 missed purchases.
        EvaluationResult r = new EvaluationResult("test", 3, 1, 2, 5, 8);

        // precision = TP / (TP + FP) = 3 / 4 = 0.75
        assertThat(r.getPrecision()).isEqualTo(0.75);
        // recall = TP / (TP + FN) = 3 / 5 = 0.60
        assertThat(r.getRecall()).isEqualTo(0.60);
        // F1 = 2PR / (P + R) = 2*0.75*0.6 / 1.35 = 0.6667
        assertThat(r.getF1Score()).isCloseTo(0.6667, org.assertj.core.data.Offset.offset(0.0005));

        assertThat(r.getPrecisionPct()).isEqualTo(75.0);
        assertThat(r.getRecallPct()).isEqualTo(60.0);
    }

    @Test
    void handlesEmptyCountsWithoutDivideByZero() {
        EvaluationResult r = new EvaluationResult("empty", 0, 0, 0, 0, 8);
        assertThat(r.getPrecision()).isZero();
        assertThat(r.getRecall()).isZero();
        assertThat(r.getF1Score()).isZero();
    }
}
