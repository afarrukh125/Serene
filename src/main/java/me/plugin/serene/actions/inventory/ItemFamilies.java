package me.plugin.serene.actions.inventory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ItemFamilies {

    private static final Logger LOG = LoggerFactory.getLogger(ItemFamilies.class);

    static final int NO_FORM = 0;

    private record FormDefinition(int rank, Supplier<Tag<Material>> tag, List<String> suffixes) {}

    private static final List<FormDefinition> FORMS = List.of(
            new FormDefinition(1, () -> Tag.LOGS, List.of("_LOG", "_WOOD", "_STEM", "_HYPHAE", "_BLOCK")),
            new FormDefinition(2, () -> Tag.LEAVES, List.of("_LEAVES")),
            new FormDefinition(3, () -> Tag.SAPLINGS, List.of("_SAPLING")),
            new FormDefinition(4, () -> Tag.PLANKS, List.of("_PLANKS")),
            new FormDefinition(5, () -> Tag.STAIRS, List.of("_STAIRS")),
            new FormDefinition(6, () -> Tag.SLABS, List.of("_SLAB")),
            new FormDefinition(7, () -> Tag.WALLS, List.of("_WALL")),
            new FormDefinition(8, () -> Tag.FENCES, List.of("_FENCE")),
            new FormDefinition(9, () -> Tag.FENCE_GATES, List.of("_FENCE_GATE")),
            new FormDefinition(10, () -> Tag.DOORS, List.of("_DOOR")),
            new FormDefinition(11, () -> Tag.TRAPDOORS, List.of("_TRAPDOOR")),
            new FormDefinition(12, () -> Tag.ALL_HANGING_SIGNS, List.of("_WALL_HANGING_SIGN", "_HANGING_SIGN")),
            new FormDefinition(13, () -> Tag.SIGNS, List.of("_WALL_SIGN", "_SIGN")),
            new FormDefinition(14, () -> Tag.BUTTONS, List.of("_BUTTON")),
            new FormDefinition(15, () -> Tag.PRESSURE_PLATES, List.of("_PRESSURE_PLATE")));

    private static final String STRIPPED_PREFIX = "STRIPPED_";

    private static final class Resolved {
        static final Map<Material, Integer> ANCHOR;
        static final Map<Material, Integer> FORM_RANK;

        static {
            var anchors = new EnumMap<Material, Integer>(Material.class);
            var formRanks = new EnumMap<Material, Integer>(Material.class);
            try {
                build(anchors, formRanks);
            } catch (Throwable t) {
                anchors.clear();
                formRanks.clear();
                LOG.warn("Could not read vanilla tags, falling back to registry order for sorting", t);
            }
            ANCHOR = anchors;
            FORM_RANK = formRanks;
        }
    }

    private static void build(Map<Material, Integer> anchors, Map<Material, Integer> formRanks) {
        var familyOf = new EnumMap<Material, String>(Material.class);

        for (var form : FORMS) {
            for (var material : form.tag().get().getValues()) {
                if (material.isLegacy() || familyOf.containsKey(material)) {
                    continue;
                }
                familyName(material, form.suffixes()).ifPresent(family -> {
                    familyOf.put(material, family);
                    formRanks.put(material, form.rank());
                });
            }
        }

        var knownFamilies = new HashMap<String, List<Material>>();
        familyOf.forEach((material, family) -> knownFamilies
                .computeIfAbsent(family, ignored -> new ArrayList<>())
                .add(material));

        for (var material : Material.values()) {
            if (material.isLegacy() || familyOf.containsKey(material)) {
                continue;
            }
            var name = material.name();
            var family = knownFamilies.containsKey(name)
                    ? name
                    : name.endsWith("S") && knownFamilies.containsKey(name.substring(0, name.length() - 1))
                            ? name.substring(0, name.length() - 1)
                            : null;
            if (family != null) {
                knownFamilies.get(family).add(material);
                formRanks.put(material, NO_FORM);
            }
        }

        knownFamilies.forEach((family, members) -> {
            var anchor = members.stream().mapToInt(Material::ordinal).min().orElseThrow();
            members.forEach(member -> anchors.put(member, anchor));
        });
    }

    private static java.util.Optional<String> familyName(Material material, List<String> suffixes) {
        var name = material.name();
        if (name.startsWith(STRIPPED_PREFIX)) {
            name = name.substring(STRIPPED_PREFIX.length());
        }
        for (var suffix : suffixes) {
            if (name.endsWith(suffix) && name.length() > suffix.length()) {
                return java.util.Optional.of(name.substring(0, name.length() - suffix.length()));
            }
        }
        return java.util.Optional.empty();
    }

    static int anchor(Material material) {
        return Resolved.ANCHOR.getOrDefault(material, material.ordinal());
    }

    static int formRank(Material material) {
        return Resolved.FORM_RANK.getOrDefault(material, NO_FORM);
    }

    private ItemFamilies() {}
}
