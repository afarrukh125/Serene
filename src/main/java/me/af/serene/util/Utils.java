package me.af.serene.util;

import java.util.concurrent.ThreadLocalRandom;

public class Utils {
    public static boolean shouldTakeDamage(int unbreakingLevel) {
        // Uses formula from https://minecraft.fandom.com/wiki/Unbreaking#Usage
        if (unbreakingLevel == 0)
            return true;
        return ThreadLocalRandom.current().nextInt(unbreakingLevel + 1) == 0;
    }

}
