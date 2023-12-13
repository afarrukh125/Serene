package me.plugin.serene.util;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.HashMap;
import java.util.Map;

public final class ExperienceData {
    private static final Map<ExperienceData, ExperienceData> pool = new HashMap<>();
    private final int minExp;
    private final int maxExp;

    private ExperienceData(int minExp, int maxExp) {
        this.minExp = minExp;
        this.maxExp = maxExp;
    }

    public static synchronized ExperienceData withMinMaxExp(int minExp, int maxExp) {
        var key = new ExperienceData(minExp, maxExp);
        var existing = pool.get(key);
        if (existing != null) {
            return existing;
        } else {
            pool.put(key, key);
        }
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
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
