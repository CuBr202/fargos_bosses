package com.cubr.fargo_bosses.fargo_bosses.entity;

import com.cubr.fargo_bosses.fargo_bosses.FargoBosses;
import lykrast.meetyourfight.entity.BossEntity;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;

import java.lang.reflect.Field;

final class BossBarUtil {
    private static final Field BOSS_INFO_FIELD = findBossInfoField();

    private BossBarUtil() {
    }

    static void useNotchedTwelve(BossEntity boss) {
        if (BOSS_INFO_FIELD == null) {
            return;
        }

        try {
            ((ServerBossEvent) BOSS_INFO_FIELD.get(boss)).setOverlay(BossEvent.BossBarOverlay.NOTCHED_12);
        }
        catch (IllegalAccessException exception) {
            FargoBosses.LOGGER.error("Could not configure the boss bar overlay for {}", boss.getType(), exception);
        }
    }

    private static Field findBossInfoField() {
        try {
            Field field = BossEntity.class.getDeclaredField("bossInfo");
            field.setAccessible(true);
            return field;
        }
        catch (ReflectiveOperationException exception) {
            FargoBosses.LOGGER.error("Could not access Meet Your Fight's boss bar", exception);
            return null;
        }
    }
}
