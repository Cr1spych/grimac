package ac.grim.grimac.checks.impl.autoclicker.utils;

import java.util.List;

public class AutoclickerUtils {

    // size: 4 -> 8 -> 12...
    private boolean isAlternatingPattern(List<Long> samples) {

        long a = samples.get(0);
        long b = samples.get(1);

        for (int i = 0; i < samples.size(); i++) {
            long expected = (i % 2 == 0) ? a : b;
            long actual = samples.get(i);

        }

        return a != b;
    }
}
