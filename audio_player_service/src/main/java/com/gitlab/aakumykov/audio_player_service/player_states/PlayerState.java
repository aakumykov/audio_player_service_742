package com.gitlab.aakumykov.audio_player_service.player_states;

import androidx.annotation.Nullable;

import com.gitlab.aakumykov.audio_player_service.other.ePlayerMode;
import com.gitlab.aakumykov.audio_player_service.other.iSoundTrack;

public abstract class PlayerState {

    @Nullable
    private final iSoundTrack mSoundTrack;

    public PlayerState(@Nullable iSoundTrack soundTrack) {
        mSoundTrack = soundTrack;
    }

    @Nullable
    public iSoundTrack getSoundTrack() {
        return mSoundTrack;
    }

    // TODO: аннотировать @NonNull
    public abstract ePlayerMode getPlayerMode();

}
