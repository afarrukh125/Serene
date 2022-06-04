package me.plugin.serene.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ExperienceData {
    private static final Map<ExperienceData, ExperienceData> pool = new HashMap<>();
    private final int minExp;
    private final int maxExp;

    private ExperienceData(int minExp, int maxExp) {
        this.minExp = minExp;
        this.maxExp = maxExp;
    }

    public static synchronized ExperienceData withMinMaxExp(int minExp, int maxExp) {
        ExperienceData key = new ExperienceData(minExp, maxExp);
        ExperienceData existing = pool.get(key);
        if (existing != null)
            return existing;
        else
            pool.put(key, key);
        return key;
    }

    public static ExperienceData noExp() {
        return withMinMaxExp(0, 1);
    }

    public int minExp() {
        return minExp;
    }

    public int maxExp() {
        return maxExp;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ExperienceData) obj;
        return this.minExp == that.minExp &&
                this.maxExp == that.maxExp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minExp, maxExp);
    }

    @Override
    public String toString() {
        return "ExperienceRange[" +
                "min=" + minExp + ", " +
                "max=" + maxExp + ']';
    }

}
