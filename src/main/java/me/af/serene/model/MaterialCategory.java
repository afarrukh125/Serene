package me.af.serene.model;

import org.bukkit.Material;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.bukkit.Material.APPLE;
import static org.bukkit.Material.BEEF;
import static org.bukkit.Material.BREAD;
import static org.bukkit.Material.CHICKEN;
import static org.bukkit.Material.COAL_ORE;
import static org.bukkit.Material.COD;
import static org.bukkit.Material.COOKED_BEEF;
import static org.bukkit.Material.COOKED_CHICKEN;
import static org.bukkit.Material.COOKED_COD;
import static org.bukkit.Material.COOKED_MUTTON;
import static org.bukkit.Material.COOKED_RABBIT;
import static org.bukkit.Material.COOKED_SALMON;
import static org.bukkit.Material.COPPER_ORE;
import static org.bukkit.Material.DEEPSLATE_COAL_ORE;
import static org.bukkit.Material.DEEPSLATE_COPPER_ORE;
import static org.bukkit.Material.DEEPSLATE_DIAMOND_ORE;
import static org.bukkit.Material.DEEPSLATE_EMERALD_ORE;
import static org.bukkit.Material.DEEPSLATE_GOLD_ORE;
import static org.bukkit.Material.DEEPSLATE_IRON_ORE;
import static org.bukkit.Material.DEEPSLATE_LAPIS_ORE;
import static org.bukkit.Material.DEEPSLATE_REDSTONE_ORE;
import static org.bukkit.Material.DIAMOND_ORE;
import static org.bukkit.Material.EMERALD_ORE;
import static org.bukkit.Material.GOLD_ORE;
import static org.bukkit.Material.IRON_ORE;
import static org.bukkit.Material.LAPIS_ORE;
import static org.bukkit.Material.MUTTON;
import static org.bukkit.Material.NETHER_GOLD_ORE;
import static org.bukkit.Material.NETHER_QUARTZ_ORE;
import static org.bukkit.Material.RABBIT;
import static org.bukkit.Material.REDSTONE_ORE;
import static org.bukkit.Material.SALMON;

public enum MaterialCategory {
    FOOD(Set.of(APPLE,
            BREAD,
            BEEF,
            CHICKEN,
            MUTTON,
            RABBIT,
            COD,
            SALMON,
            COOKED_BEEF,
            COOKED_CHICKEN,
            COOKED_MUTTON,
            COOKED_RABBIT,
            COOKED_COD,
            COOKED_SALMON)),
    ITEM(Constants.filteredByPredicate(Material::isItem)),
    MISC(Collections.emptySet()),
    ORE(Set.of(COAL_ORE,
            IRON_ORE,
            COPPER_ORE,
            DIAMOND_ORE,
            GOLD_ORE,
            EMERALD_ORE,
            LAPIS_ORE,
            REDSTONE_ORE,
            NETHER_GOLD_ORE,
            NETHER_QUARTZ_ORE,
            DEEPSLATE_COAL_ORE,
            DEEPSLATE_IRON_ORE,
            DEEPSLATE_COPPER_ORE,
            DEEPSLATE_DIAMOND_ORE,
            DEEPSLATE_GOLD_ORE,
            DEEPSLATE_EMERALD_ORE,
            DEEPSLATE_LAPIS_ORE,
            DEEPSLATE_REDSTONE_ORE)),
    SOLID(Constants.filteredByPredicate(Material::isSolid));

    private final Set<Material> materials;

    MaterialCategory(Set<Material> materials) {
        this.materials = materials;
    }

    public Set<Material> getMaterials() {
        return Collections.unmodifiableSet(materials);
    }

    public MaterialCategory getCategoryFor(Material material) {
        for (MaterialCategory category : MaterialCategory.values()) {
            if (category.materials.contains(material))
                return category;
        }
        return MISC;
    }

    private static class Constants {
        public static final EnumSet<Material> ALL_MATERIALS = EnumSet.allOf(Material.class);

        public static Set<Material> filteredByPredicate(Predicate<? super Material> predicate) {
            return ALL_MATERIALS.stream().filter(predicate).collect(Collectors.toSet());
        }
    }
}
