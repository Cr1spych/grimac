package ac.grim.grimac.checks.impl.aim.utils;

import java.util.List;

public class PatternUtils {

    public static boolean matchesPatternStructure(List<Float> list, int... pattern) {
        int patternLength = pattern.length;
        if (list.size() < patternLength) return false;

        for (int i = 0; i <= list.size() - patternLength; i++) {
            boolean matches = true;
            Float[] mappedValues = new Float[patternLength];

            for (int j = 0; j < patternLength; j++) {
                mappedValues[j] = list.get(i + j);
            }

            outer:
            for (int a = 0; a < patternLength; a++) {
                for (int b = a + 1; b < patternLength; b++) {
                    if (pattern[a] == pattern[b]) {
                        if (!approximatelyEqual(mappedValues[a], mappedValues[b])) {
                            matches = false;
                            break outer;
                        }
                    } else {
                        if (approximatelyEqual(mappedValues[a], mappedValues[b])) {
                            matches = false;
                            break outer;
                        }
                    }
                }
            }

            if (matches) return true;
        }

        return false;
    }

    private static boolean approximatelyEqual(float a, float b) {
        float epsilon = 0.0001f;
        return Math.abs(a - b) < epsilon;
    }
}
