package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

import java.util.ArrayDeque;
import java.util.Deque;

@CheckData(name = "AimComplex")
public class AimComplex extends Check implements RotationCheck {
    private final Deque<float[]> rotationHistory = new ArrayDeque<>();
    private static final int SAMPLE_SIZE = 30;

    public AimComplex(GrimPlayer player) {
        super(player);
    }

    @Override
    public void process(RotationUpdate update) {
        float deltaYaw = update.getDeltaYawRotABS();
        float deltaPitch = update.getDeltaPitchRotABS();

        // Пропускаем минимальные движения
        if (deltaYaw < 0.1f && deltaPitch < 0.1f) return;

        // Сохраняем данные
        rotationHistory.offer(new float[]{deltaYaw, deltaPitch});

        // Поддерживаем размер выборки
        while (rotationHistory.size() > SAMPLE_SIZE) {
            rotationHistory.poll();
        }

        // Нужно достаточно данных
        if (rotationHistory.size() < 15) return;

        // Анализируем сложность движений
        analyzeComplexity();
    }

    private void analyzeComplexity() {
        float[] yaws = new float[rotationHistory.size()];
        float[] pitches = new float[rotationHistory.size()];

        int i = 0;
        for (float[] point : rotationHistory) {
            yaws[i] = point[0];
            pitches[i] = point[1];
            i++;
        }

        // Вычисляем автокорреляцию
        double yawAutoCorr = calculateAutocorrelation(yaws);
        double pitchAutoCorr = calculateAutocorrelation(pitches);

        // Вычисляем изменчивость
        double yawVariance = calculateVariance(yaws);
        double pitchVariance = calculateVariance(pitches);

        // Вычисляем периодичность
        double yawPeriodicity = detectPeriodicity(yaws);
        double pitchPeriodicity = detectPeriodicity(pitches);

        // Флагаем подозрительные паттерны
        if (yawAutoCorr > 0.8 && yawVariance < 2.0) {
            flagAndAlert("High yaw autocorrelation: " + String.format("%.3f", yawAutoCorr));
        }

        if (pitchAutoCorr > 0.8 && pitchVariance < 2.0) {
            flagAndAlert("High pitch autocorrelation: " + String.format("%.3f", pitchAutoCorr));
        }

        if (yawPeriodicity > 0.6) {
            flagAndAlert("Yaw periodicity: " + String.format("%.3f", yawPeriodicity));
        }

        if (pitchPeriodicity > 0.6) {
            flagAndAlert("Pitch periodicity: " + String.format("%.3f", pitchPeriodicity));
        }
    }

    private double calculateAutocorrelation(float[] data) {
        if (data.length < 3) return 0;

        // Вычисляем среднее
        double mean = 0;
        for (float v : data) mean += v;
        mean /= data.length;

        // Вычисляем автокорреляцию с лагом 1
        double numerator = 0, denominator = 0;
        for (int i = 0; i < data.length - 1; i++) {
            numerator += (data[i] - mean) * (data[i + 1] - mean);
            denominator += Math.pow(data[i] - mean, 2);
        }

        double lastTerm = Math.pow(data[data.length - 1] - mean, 2);
        denominator += lastTerm;

        return denominator > 0 ? Math.abs(numerator / denominator) : 0;
    }

    private double calculateVariance(float[] data) {
        if (data.length < 2) return 0;

        double mean = 0;
        for (float v : data) mean += v;
        mean /= data.length;

        double variance = 0;
        for (float v : data) {
            variance += Math.pow(v - mean, 2);
        }

        return variance / data.length;
    }

    private double detectPeriodicity(float[] data) {
        if (data.length < 6) return 0;

        // Ищем повторяющиеся паттерны
        int matches = 0;
        int total = 0;

        for (int i = 2; i < data.length - 2; i++) {
            double prevDiff = Math.abs(data[i] - data[i-1]);
            double nextDiff = Math.abs(data[i+1] - data[i]);

            if (prevDiff < 0.1 && nextDiff < 0.1) {
                // Проверяем есть ли похожие изменения ранее
                for (int j = 0; j < i - 2; j++) {
                    double oldPrevDiff = Math.abs(data[j+1] - data[j]);
                    double oldNextDiff = Math.abs(data[j+2] - data[j+1]);

                    if (Math.abs(prevDiff - oldPrevDiff) < 0.05 &&
                            Math.abs(nextDiff - oldNextDiff) < 0.05) {
                        matches++;
                        break;
                    }
                }
            }
            total++;
        }

        return total > 0 ? (double) matches / total : 0;
    }
}
