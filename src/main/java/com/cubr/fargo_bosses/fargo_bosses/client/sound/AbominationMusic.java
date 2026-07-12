package com.cubr.fargo_bosses.fargo_bosses.client.sound;

import com.cubr.fargo_bosses.fargo_bosses.entity.AbominationEntity;
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
public class AbominationMusic extends AbstractTickableSoundInstance {
    private static final Map<Integer, AbominationMusic> ACTIVE_MUSIC = new HashMap<>();

    private final AbominationEntity boss;
    private final boolean phaseTwoTrack;

    public AbominationMusic(AbominationEntity boss, boolean phaseTwoTrack) {
        super(getTrack(phaseTwoTrack), SoundSource.MUSIC, SoundInstance.createUnseededRandom());
        this.boss = boss;
        this.phaseTwoTrack = phaseTwoTrack;
        x = boss.getX();
        y = boss.getY();
        z = boss.getZ();
        looping = true;
    }

    public static void start(AbominationEntity boss) {
        boolean phaseTwoTrack = boss.isUsingPhaseTwoMusic();
        AbominationMusic current = ACTIVE_MUSIC.get(boss.getId());
        if (current != null && current.phaseTwoTrack == phaseTwoTrack) {
            return;
        }
        if (current != null) {
            current.stop();
        }

        AbominationMusic music = new AbominationMusic(boss, phaseTwoTrack);
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
        if (boss.isUsingPhaseTwoMusic() != phaseTwoTrack) {
            stop();
            start(boss);
        }
    }

    private static SoundEvent getTrack(boolean phaseTwoTrack) {
        return phaseTwoTrack ? ModSounds.ABOM_PHASE_2.get() : ModSounds.ABOM_PHASE_1.get();
    }
}
