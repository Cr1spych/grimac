package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.aim.utils.millenium.Interpolation;
import ac.grim.grimac.checks.impl.aim.utils.millenium.Statistics;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

import java.util.ArrayList;
import java.util.List;

@CheckData(name = "AimD")
public class AimD extends Check implements RotationCheck {

    private final List<Float> stack = new ArrayList<>();
    private float buffer = 0;

    public AimD(GrimPlayer player) {
        super(player);
    }

    private static List<Float> predict(final float a, final float b, final Interpolation.Type type, final Interpolation.Ease ease) {
        final List<Float> predicted = new ArrayList<>();
        for (double d = 0.0d; d <= 1.0d; d += 0.05)
            predicted.add((float) Interpolation.interpolate(a, b, d, type, ease));
        return predicted;
    }

    @Override
    public void process(RotationUpdate rotationUpdate) {

        if (rotationUpdate.isCinematic() || player.inVehicle()) {
            return;
        }

        stack.add(rotationUpdate.getDeltaXRotABS());
        if (stack.size() >= 20) {

            final float a = stack.get(0), b = stack.get(stack.size() - 1);
            final List<Float> linearPredict = predict(a, b, Interpolation.Type.LINEAR, Interpolation.Ease.IN_OUT);
            final List<Float> jiffA = Statistics.getJiffDelta(stack, 2);
            final List<Float> jiffB = Statistics.getJiffDelta(linearPredict, 2);
            final double r = Math.abs(Statistics.getRSquared(jiffA, jiffB));

            stack.clear();
        }
    }
}
