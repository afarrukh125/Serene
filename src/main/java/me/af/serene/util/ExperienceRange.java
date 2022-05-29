package me.af.serene.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ExperienceRange {

    private static final ExperienceRange NONE = new ExperienceRange(0, 1);

    private static final Map<ExperienceRange, ExperienceRange> pool = new HashMap<>();
    private final int min;
    private final int max;

    private ExperienceRange(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public static ExperienceRange none() {
        return NONE;
    }

    public static synchronized ExperienceRange of(int min, int max) {
        ExperienceRange key = new ExperienceRange(min, max);
        ExperienceRange existing = pool.get(key);
        if (existing != null)
            return existing;
        else
            pool.put(key, key);
        return key;
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ExperienceRange) obj;
        return this.min == that.min &&
                this.max == that.max;
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }

    @Override
    public String toString() {
        return "ExperienceRange[" +
                "min=" + min + ", " +
                "max=" + max + ']';
    }

}
