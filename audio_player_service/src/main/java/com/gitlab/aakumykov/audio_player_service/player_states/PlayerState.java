package com.gitlab.aakumykov.audio_player_service.player_states;

import androidx.annotation.Nullable;

import com.gitlab.aakumykov.audio_player_service.other.ePlayerMode;
import com.gitlab.aakumykov.audio_player_service.other.iSoundTrack;

public abstract class PlayerState {

    @Nullable
    private final iSoundTrack mMusicItem;

    public PlayerState(@Nullable iSoundTrack musicItem) {
        mMusicItem = musicItem;
    }

    @Nullable
    public iSoundTrack getMusicItem() {
        return mMusicItem;
    }

    // TODO: аннотировать @NonNull
    public abstract ePlayerMode getPlayerMode();

}
