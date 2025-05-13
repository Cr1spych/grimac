package ac.grim.grimac.checks.impl.aim.utils;

import java.util.List;

// GPT CODE MONSTER WWW YOO
public class AimUtils {

    public static float getAverage(List<Float> list) {
        if (list.isEmpty()) return 0f;

        float sum = 0f;
        for (float value : list) {
            sum += value;
        }
        return sum / list.size();
    }

    public static boolean hasDuplicates(List<Float> list, int threshold) {
        for (int i = 0; i < list.size(); i++) {
            float value = list.get(i);
            int count = 1;

            for (int j = i + 1; j < list.size(); j++) {
                if (Float.compare(value, list.get(j)) == 0) {
                    count++;
                    if (count >= threshold) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasConsecutiveRepeats(List<Float> list, int threshold) {
        if (list.size() < threshold) return false;

        int count = 1;
        for (int i = 1; i < list.size(); i++) {
            if (Float.compare(list.get(i), list.get(i - 1)) == 0) {
                count++;
                if (count >= threshold) return true;
            } else {
                count = 1;
            }
        }
        return false;
    }

    public static boolean hasTooManyExceeding(List<Float> list, float limit, int threshold) {
        int count = 0;

        for (Float value : list) {
            if (value > limit) {
                count++;
                if (count >= threshold) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasAllEqual(List<Float> list) {
        if (list.isEmpty()) return false;

        float first = list.get(0);
        for (float val : list) {
            if (Float.compare(val, first) != 0) {
                return false;
            }
        }
        return true;
    }

    public static float getMax(List<Float> list) {
        if (list.isEmpty()) return 0f;
        float max = Float.MIN_VALUE;
        for (float value : list) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    public static float getMin(List<Float> list) {
        if (list.isEmpty()) return 0f;
        float min = Float.MAX_VALUE;
        for (float value : list) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    public static boolean hasAlternatingPattern(List<Float> list) {
        for (int i = 2; i < list.size(); i++) {
            if (!Float.valueOf(list.get(i)).equals(list.get(i - 2))) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasRepeatingPattern(List<Float> list, int patternSize) {
        if (list.size() < patternSize * 2) return false;

        for (int i = 0; i < list.size() - patternSize; i++) {
            for (int j = 0; j < patternSize; j++) {
                if (i + patternSize + j >= list.size()) return false;
                if (!Float.valueOf(list.get(i + j)).equals(list.get(i + patternSize + j))) {
                    return false;
                }
            }
        }
        return true;
    }
}
