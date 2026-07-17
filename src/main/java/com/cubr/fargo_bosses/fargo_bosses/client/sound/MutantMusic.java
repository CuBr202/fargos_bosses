package com.cubr.fargo_bosses.fargo_bosses.client.sound;

import com.cubr.fargo_bosses.fargo_bosses.entity.MutantEntity;
import com.cubr.fargo_bosses.fargo_bosses.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class MutantMusic extends AbstractTickableSoundInstance {
    private static final Map<Integer, MutantMusic> ACTIVE_MUSIC = new HashMap<>();

    private final MutantEntity boss;
    private final int musicPhase;

    private MutantMusic(MutantEntity boss, int musicPhase) {
        super(getTrack(musicPhase), SoundSource.MUSIC, SoundInstance.createUnseededRandom());
        this.boss = boss;
        this.musicPhase = musicPhase;
        x = boss.getX();
        y = boss.getY();
        z = boss.getZ();
        looping = true;
    }

    public static void start(MutantEntity boss) {
        int musicPhase = boss.getMusicPhase();
        MutantMusic current = ACTIVE_MUSIC.get(boss.getId());
        if (current != null && current.musicPhase == musicPhase) {
            return;
        }
        if (current != null) {
            current.stop();
        }

        MutantMusic music = new MutantMusic(boss, musicPhase);
        ACTIVE_MUSIC.put(boss.getId(), music);
        Minecraft.getInstance().getSoundManager().queueTickingSound(music);
    }

    @Override
    public void tick() {
        if (!boss.isAlive()) {
            stop();
            ACTIVE_MUSIC.remove(boss.getId(), this);
            return;
        }

        x = boss.getX();
        y = boss.getY();
        z = boss.getZ();
        if (boss.getMusicPhase() != musicPhase) {
            stop();
            Minecraft.getInstance().execute(() -> start(boss));
        }
    }

    private static SoundEvent getTrack(int musicPhase) {
        return switch (musicPhase) {
            case 2 -> ModSounds.MUTANT_PHASE_2.get();
            case 3 -> ModSounds.MUTANT_PHASE_3.get();
            case 4 -> ModSounds.MUTANT_PHASE_4.get();
            default -> ModSounds.MUTANT_PHASE_1.get();
        };
    }
}
