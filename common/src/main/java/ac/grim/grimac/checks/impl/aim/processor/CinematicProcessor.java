package ac.grim.grimac.checks.impl.aim.processor;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;
import ac.grim.grimac.utils.graph.GraphUtil;
import com.google.common.collect.Lists;

import java.util.List;

// https://github.com/ElevatedDev/Frequency/blob/master/src/main/java/xyz/elevated/frequency/check/impl/aimassist/cinematic/Cinematic.java
@CheckData(name = "Cinematic")
public final class CinematicProcessor extends Check implements RotationCheck {

    private long lastSmooth = 0L, lastHighRate = 0L;
    private double lastDeltaXRot = 0.0d, lastDeltaYRot = 0.0d;

    private final List<Double> xSamples = Lists.newArrayList();
    private final List<Double> ySamples = Lists.newArrayList();

    public CinematicProcessor(GrimPlayer player) {
        super(player);
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        final long now = System.currentTimeMillis();

        final double deltaXRot = rotationUpdate.getDeltaXRot();
        final double deltaYRot = rotationUpdate.getDeltaYRot();

        final double differenceYaw = Math.abs(deltaXRot - lastDeltaXRot);
        final double differencePitch = Math.abs(deltaXRot - lastDeltaYRot);

        final double joltYaw = Math.abs(differenceYaw - deltaXRot);
        final double joltPitch = Math.abs(differencePitch - deltaYRot);

        final boolean cinematic = (now - lastHighRate > 250L) || now - lastSmooth < 9000L;

        if (joltYaw > 1.0 && joltPitch > 1.0) {
            this.lastHighRate = now;
        }

        if (deltaYRot > 0.0 && deltaXRot > 0.0) {
            xSamples.add(deltaXRot);
            ySamples.add(deltaYRot);
        }

        if (xSamples.size() == 20 && ySamples.size() == 20) {
            // Get the cerberus/positive graph of the sample-lists
            final GraphUtil.GraphResult resultsYaw = GraphUtil.getGraph(xSamples);
            final GraphUtil.GraphResult resultsPitch = GraphUtil.getGraph(ySamples);

            // Negative values
            final int negativesYaw = resultsYaw.getNegatives();
            final int negativesPitch = resultsPitch.getNegatives();

            // Positive values
            final int positivesYaw = resultsYaw.getPositives();
            final int positivesPitch = resultsPitch.getPositives();

            // Cinematic camera usually does this on *most* speeds and is accurate for the most part.
            if (positivesYaw > negativesYaw || positivesPitch > negativesPitch) {
                this.lastSmooth = now;
            }

            xSamples.clear();
            ySamples.clear();
        }

        rotationUpdate.setCinematic(cinematic);

        this.lastDeltaXRot = deltaXRot;
        this.lastDeltaYRot = deltaYRot;
    }
}
