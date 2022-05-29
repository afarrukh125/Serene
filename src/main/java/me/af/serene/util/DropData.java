package me.af.serene.util;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class DropData {
    private static final Map<DropData, DropData> pool = new HashMap<>();
    private final int minExp;
    private final int maxExp;
    private Material materialToDrop;

    private DropData(int minExp, int maxExp, Material materialToDrop) {
        this.minExp = minExp;
        this.maxExp = maxExp;
        this.materialToDrop = materialToDrop;
    }

    public static synchronized DropData withMinMaxExp(int minExp, int maxExp, Material material) {
        DropData key = new DropData(minExp, maxExp, material);
        DropData existing = pool.get(key);
        if (existing != null)
            return existing;
        else
            pool.put(key, key);
        return key;
    }

    public static DropData noExp(Material material) {
        return withMinMaxExp(0, 1, material);
    }

    public int minExp() {
        return minExp;
    }

    public int maxExp() {
        return maxExp;
    }

    public Material materialToDrop() {
        return materialToDrop;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DropData) obj;
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
