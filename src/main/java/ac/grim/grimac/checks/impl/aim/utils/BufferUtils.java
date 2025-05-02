package ac.grim.grimac.checks.impl.aim.utils;

public class BufferUtils {

    public static float updateBuffer(boolean condition, float buffer, float increase, float decay, float threshold, Runnable onThreshold) {
        if (condition) {
            buffer += increase;
        } else {
            buffer = Math.max(0, buffer - decay);
        }

        if (buffer > threshold) {
            onThreshold.run();
            buffer = 0;
        }

        return buffer;
    }
}
