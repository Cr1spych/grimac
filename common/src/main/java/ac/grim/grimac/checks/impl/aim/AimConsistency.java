package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

import java.util.ArrayDeque;
import java.util.Deque;

@CheckData(name = "AimConsistency")
public class AimConsistency extends Check implements RotationCheck {
    private final Deque<Float> yawDeltas = new ArrayDeque<>();
    private final Deque<Float> pitchDeltas = new ArrayDeque<>();
    private static final int SAMPLE_SIZE = 20;

    private float lastDeltaYaw = 0;
    private float lastDeltaPitch = 0;

    public AimConsistency(GrimPlayer player) {
        super(player);
    }

    @Override
    public void process(RotationUpdate update) {
        float deltaYaw = update.getDeltaYawRotABS();
        float deltaPitch = update.getDeltaPitchRotABS();

        // Skip zero movements
        if (deltaYaw == 0 && deltaPitch == 0) return;

        // Add to sample buffer
        yawDeltas.offer(deltaYaw);
        pitchDeltas.offer(deltaPitch);

        // Maintain buffer size
        if (yawDeltas.size() > SAMPLE_SIZE) {
            yawDeltas.poll();
            pitchDeltas.poll();
        }

        // Need minimum samples for analysis
        if (yawDeltas.size() < 10) {
            lastDeltaYaw = deltaYaw;
            lastDeltaPitch = deltaPitch;
            return;
        }

        // Calculate statistical metrics
        double yawMean = calculateMean(yawDeltas);
        double pitchMean = calculateMean(pitchDeltas);

        double yawStdDev = calculateStandardDeviation(yawDeltas, yawMean);
        double pitchStdDev = calculateStandardDeviation(pitchDeltas, pitchMean);

        // Check for artificial low variance (but allow legitimate smooth movement)
        if (yawStdDev > 0.01 && yawStdDev < 0.3) {
            double yawAutoCorrelation = calculateAutoCorrelation(yawDeltas, yawMean, yawStdDev);
            // Only flag if there's also high autocorrelation AND significant movement
            if (yawAutoCorrelation > 0.85 && yawMean > 2.0) {
                flagAndAlert("Artificial yaw pattern: std=" + String.format("%.3f", yawStdDev) +
                        " corr=" + String.format("%.3f", yawAutoCorrelation) +
                        " mean=" + String.format("%.3f", yawMean));
            }
        }

        if (pitchStdDev > 0.01 && pitchStdDev < 0.3) {
            double pitchAutoCorrelation = calculateAutoCorrelation(pitchDeltas, pitchMean, pitchStdDev);
            // Only flag if there's also high autocorrelation AND significant movement
            if (pitchAutoCorrelation > 0.85 && pitchMean > 2.0) {
                flagAndAlert("Artificial pitch pattern: std=" + String.format("%.3f", pitchStdDev) +
                        " corr=" + String.format("%.3f", pitchAutoCorrelation) +
                        " mean=" + String.format("%.3f", pitchMean));
            }
        }

        // Advanced pattern detection - check for mechanical movement signatures
        double yawPatternScore = calculatePatternScore(yawDeltas);
        double pitchPatternScore = calculatePatternScore(pitchDeltas);

        if (yawPatternScore > 0.9 && yawMean > 1.0) {
            flagAndAlert("Mechanical yaw movement: score=" + String.format("%.3f", yawPatternScore));
        }

        if (pitchPatternScore > 0.9 && pitchMean > 1.0) {
            flagAndAlert("Mechanical pitch movement: score=" + String.format("%.3f", pitchPatternScore));
        }

        lastDeltaYaw = deltaYaw;
        lastDeltaPitch = deltaPitch;
    }

    private double calculateMean(Deque<Float> values) {
        return values.stream().mapToDouble(Float::doubleValue).average().orElse(0);
    }

    private double calculateStandardDeviation(Deque<Float> values, double mean) {
        double sumSquaredDiffs = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .sum();
        return Math.sqrt(sumSquaredDiffs / values.size());
    }

    private double calculateAutoCorrelation(Deque<Float> values, double mean, double stdDev) {
        if (stdDev <= 0.01) return 0;

        Float[] array = values.toArray(new Float[0]);
        double correlation = 0;
        int n = array.length - 1;

        if (n <= 0) return 0;

        for (int i = 0; i < n; i++) {
            correlation += (array[i] - mean) * (array[i + 1] - mean);
        }

        return correlation / (n * stdDev * stdDev);
    }

    private double calculatePatternScore(Deque<Float> values) {
        Float[] array = values.toArray(new Float[0]);
        int n = array.length;

        // Look for repeating patterns or mechanical signatures
        double score = 0;
        int patternMatches = 0;

        // Check for small differences between consecutive values (mechanical precision)
        for (int i = 1; i < n; i++) {
            double diff = Math.abs(array[i] - array[i-1]);
            // Very small, precise changes indicate artificial movement
            if (diff > 0 && diff < 0.1) {
                patternMatches++;
            }
        }

        score = (double) patternMatches / (n - 1);

        // Also check for values that are suspiciously round
        int roundValues = 0;
        for (Float value : array) {
            if (Math.abs(value - Math.round(value)) < 0.05) {
                roundValues++;
            }
        }

        double roundScore = (double) roundValues / n;
        score = Math.max(score, roundScore);

        return score;
    }
}
