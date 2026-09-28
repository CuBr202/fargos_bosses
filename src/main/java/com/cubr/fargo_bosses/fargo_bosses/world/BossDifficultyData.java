package com.cubr.fargo_bosses.fargo_bosses.world;

import com.cubr.fargo_bosses.fargo_bosses.FargoBosses;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.EnumMap;

public class BossDifficultyData extends SavedData {
    private static final String DATA_NAME = FargoBosses.MODID + "_difficulty";
    private final EnumMap<Boss, Integer> difficulties = new EnumMap<>(Boss.class);

    public enum Boss {
        ABOMINATION("abomination"), MUTANT("mutant");

        public final String id;

        Boss(String id) {
            this.id = id;
        }
    }

    public static BossDifficultyData get(MinecraftServer server) {
        // Keep one setting per world save, shared by every dimension.
        return server.overworld().getDataStorage().computeIfAbsent(
                BossDifficultyData::load, BossDifficultyData::new, DATA_NAME);
    }

    public static BossDifficultyData load(CompoundTag tag) {
        BossDifficultyData data = new BossDifficultyData();
        for (Boss boss : Boss.values()) {
            int difficulty = tag.contains(boss.id, Tag.TAG_ANY_NUMERIC) ? tag.getInt(boss.id) : -1;
            data.difficulties.put(boss, difficulty >= -1 && difficulty <= 2 ? difficulty : -1);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        for (Boss boss : Boss.values()) {
            tag.putInt(boss.id, getDifficulty(boss));
        }
        return tag;
    }

    public int getDifficulty(Boss boss) {
        return difficulties.getOrDefault(boss, -1);
    }

    public void setDifficulty(Boss boss, int difficulty) {
        if (difficulty < -1 || difficulty > 2) {
            throw new IllegalArgumentException("Boss difficulty must be between -1 and 2");
        }
        if (getDifficulty(boss) != difficulty) {
            difficulties.put(boss, difficulty);
            setDirty();
        }
    }

    public static int resolve(Level level, Boss boss, int individualDifficulty) {
        if (level instanceof ServerLevel serverLevel) {
            int globalDifficulty = get(serverLevel.getServer()).getDifficulty(boss);
            if (globalDifficulty >= 0) {
                return globalDifficulty;
            }
        }
        return individualDifficulty;
    }
}
