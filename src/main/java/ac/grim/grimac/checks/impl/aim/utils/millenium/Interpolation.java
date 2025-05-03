package ac.grim.grimac.checks.impl.aim.utils.millenium;

public final class Interpolation {

    public static double interpolate(double from, double to, double percent, Type type, Ease ease) {
        switch (type) {
            case SINE:
                return sineInterpolation(from, to, percent, ease);
            case QUAD:
                return quadInterpolation(from, to, percent, ease);
            case CUBIC:
                return cubicInterpolation(from, to, percent, ease);
            case QUART:
                return quartInterpolation(from, to, percent, ease);
            case QUINT:
                return quintInterpolation(from, to, percent, ease);
            case EXPO:
                return expoInterpolation(from, to, percent, ease);
            case CIRC:
                return circInterpolation(from, to, percent, ease);
            case BACK:
                return backInterpolation(from, to, percent, ease);
            case BOUNCE:
                return bounceInterpolation(from, to, percent, ease);
            case ELASTIC:
                return elasticInterpolation(from, to, percent, ease);
            default:
                return linearInterpolation(from, to, percent);
        }
    }

    public static double linearInterpolation(double from, double to, double percent) {
        return from + (to - from) * percent;
    }

    public static double sineInterpolation(double from, double to, double percent, Ease ease) {
        switch (ease) {
            case IN:
                percent = 1 - Math.cos(percent * Math.PI / 2);
                break;
            case OUT:
                percent = Math.sin(percent * Math.PI / 2);
                break;
            case IN_OUT:
                percent = -0.5 * (Math.cos(Math.PI * percent) - 1);
                break;
        }
        return from + (to - from) * percent;
    }

    public static double quadInterpolation(double from, double to, double percent, Ease ease) {
        switch (ease) {
            case IN:
                percent *= percent;
                break;
            case OUT:
                percent = percent * (2 - percent);
                break;
            case IN_OUT:
                if (percent < 0.5) {
                    percent = 2 * percent * percent;
                } else {
                    percent = -1 + (4 - 2 * percent) * percent;
                }
                break;
        }
        return from + (to - from) * percent;
    }

    public static double cubicInterpolation(double from, double to, double percent, Ease ease) {
        switch (ease) {
            case IN:
                percent *= percent * percent;
                break;
            case OUT:
                percent = (--percent) * percent * percent + 1;
                break;
            case IN_OUT:
                if (percent < 0.5) {
                    percent = 4 * percent * percent * percent;
                } else {
                    percent = (percent - 1) * (2 * percent - 2) * (2 * percent - 2) + 1;
                }
                break;
        }
        return from + (to - from) * percent;
    }

    public static double quartInterpolation(double from, double to, double percent, Ease ease) {
        switch (ease) {
            case IN:
                percent *= percent * percent * percent;
                break;
            case OUT:
                percent = 1 - (--percent) * percent * percent * percent;
                break;
            case IN_OUT:
                if (percent < 0.5) {
                    percent = 8 * percent * percent * percent * percent;
                } else {
                    percent = 1 - 8 * (--percent) * percent * percent * percent;
                }
                break;
        }
        return from + (to - from) * percent;
    }

    public static double quintInterpolation(double from, double to, double percent, Ease ease) {
        switch (ease) {
            case IN:
                percent *= percent * percent * percent * percent;
                break;
            case OUT:
                percent = 1 + (--percent) * percent * percent * percent * percent;
                break;
            case IN_OUT:
                if (percent < 0.5) {
                    percent = 16 * percent * percent * percent * percent * percent;
                } else {
                    percent = 1 + 16 * (--percent) * percent * percent * percent * percent;
                }
                break;
        }
        return from + (to - from) * percent;
    }

    public static double expoInterpolation(double from, double to, double percent, Ease ease) {
        switch (ease) {
            case IN:
                percent = (percent == 0) ? 0 : Math.pow(2, 10 * (percent - 1));
                break;
            case OUT:
                percent = (percent == 1) ? 1 : 1 - Math.pow(2, -10 * percent);
                break;
            case IN_OUT:
                if (percent == 0 || percent == 1) {
                    return from + (to - from) * percent;
                }
                if (percent < 0.5) {
                    percent = Math.pow(2, 20 * percent - 10) / 2;
                } else {
                    percent = (2 - Math.pow(2, -20 * percent + 10)) / 2;
                }
                break;
        }
        return from + (to - from) * percent;
    }

    public static double circInterpolation(double from, double to, double percent, Ease ease) {
        switch (ease) {
            case IN:
                percent = 1 - Math.sqrt(1 - percent * percent);
                break;
            case OUT:
                percent = Math.sqrt(1 - (--percent) * percent);
                break;
            case IN_OUT:
                if (percent < 0.5) {
                    percent = (1 - Math.sqrt(1 - 4 * percent * percent)) / 2;
                } else {
                    percent = (Math.sqrt(1 - 4 * (--percent) * percent) + 1) / 2;
                }
                break;
        }
        return from + (to - from) * percent;
    }

    public static double backInterpolation(double from, double to, double percent, Ease ease) {
        double s = 1.70158;
        switch (ease) {
            case IN:
                percent = percent * percent * ((s + 1) * percent - s);
                break;
            case OUT:
                percent = (--percent) * percent * ((s + 1) * percent + s) + 1;
                break;
            case IN_OUT:
                s *= 1.525;
                if (percent < 0.5) {
                    percent = (percent * 2) * percent * ((s + 1) * percent - s) * 0.5;
                } else {
                    percent = ((--percent) * 2 * percent * ((s + 1) * percent + s) + 1) * 0.5;
                }
                break;
        }
        return from + (to - from) * percent;
    }

    public static double bounceInterpolation(double from, double to, double percent, Ease ease) {
        switch (ease) {
            case IN:
                percent = 1 - bounceOut(1 - percent);
                break;
            case OUT:
                percent = bounceOut(percent);
                break;
            case IN_OUT:
                if (percent < 0.5) {
                    percent = (1 - bounceOut(1 - 2 * percent)) * 0.5;
                } else {
                    percent = (bounceOut(2 * percent - 1) + 1) * 0.5;
                }
                break;
        }
        return from + (to - from) * percent;
    }

    // Helper for Bounce Out interpolation
    private static double bounceOut(double percent) {
        if (percent < 1 / 2.75) {
            return 7.5625 * percent * percent;
        } else if (percent < 2 / 2.75) {
            percent -= 1.5 / 2.75;
            return 7.5625 * percent * percent + 0.75;
        } else if (percent < 2.5 / 2.75) {
            percent -= 2.25 / 2.75;
            return 7.5625 * percent * percent + 0.9375;
        } else {
            percent -= 2.625 / 2.75;
            return 7.5625 * percent * percent + 0.984375;
        }
    }

    public static double elasticInterpolation(double from, double to, double percent, Ease ease) {
        double p = 0.3;
        switch (ease) {
            case IN:
                if (percent == 0 || percent == 1) return percent;
                percent = -(Math.pow(2, 10 * (percent - 1)) * Math.sin((percent - 1 - p / 4) * (2 * Math.PI) / p));
                break;
            case OUT:
                if (percent == 0 || percent == 1) return percent;
                percent = Math.pow(2, -10 * percent) * Math.sin((percent - p / 4) * (2 * Math.PI) / p) + 1;
                break;
            case IN_OUT:
                if (percent == 0 || percent == 1) return percent;
                if (percent < 0.5) {
                    percent = -(Math.pow(2, 10 * (percent * 2 - 1)) * Math.sin((percent * 2 - 1 - p / 4) * (2 * Math.PI) / p)) * 0.5;
                } else {
                    percent = Math.pow(2, -10 * (percent * 2 - 1)) * Math.sin((percent * 2 - 1 - p / 4) * (2 * Math.PI) / p) * 0.5 + 1;
                }
                break;
        }
        return from + (to - from) * percent;
    }

    public enum Ease {
        IN, OUT, IN_OUT
    }

    public enum Type {
        LINEAR, SINE, QUAD, CUBIC, QUART, QUINT, EXPO, CIRC, BACK, BOUNCE, ELASTIC
    }
}
