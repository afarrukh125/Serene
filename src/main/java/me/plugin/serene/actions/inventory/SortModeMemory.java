package me.plugin.serene.actions.inventory;

import static me.plugin.serene.actions.inventory.CompactingSortingStrategy.FillDirection.COLUMN_MAJOR;
import static me.plugin.serene.actions.inventory.CompactingSortingStrategy.FillDirection.ROW_MAJOR;

import java.util.LinkedHashMap;
import java.util.Map;
import me.plugin.serene.actions.inventory.CompactingSortingStrategy.FillDirection;
import me.plugin.serene.model.SortTarget;

/**
 * Remembers how each container was last laid out so that sorting it again flips between rows and columns.
 * Bounded, because a busy server sorts far more containers than are worth remembering; evicting simply means
 * the next sort of that container starts from rows again.
 */
class SortModeMemory {

    static final int MAX_REMEMBERED = 1024;

    private final Map<SortTarget, FillDirection> lastUsed = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<SortTarget, FillDirection> eldest) {
            return size() > MAX_REMEMBERED;
        }
    };

    FillDirection nextFor(SortTarget sortTarget) {
        var next = lastUsed.get(sortTarget) == ROW_MAJOR ? COLUMN_MAJOR : ROW_MAJOR;
        lastUsed.put(sortTarget, next);
        return next;
    }

    int size() {
        return lastUsed.size();
    }
}
