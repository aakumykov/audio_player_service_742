package com.gitlab.aakumykov.audio_player_service.player_states;

import androidx.annotation.Nullable;

import com.gitlab.aakumykov.audio_player_service.other.ePlayerMode;
import com.gitlab.aakumykov.audio_player_service.other.iMusicItem;

public abstract class PlayerState {

    @Nullable
    private final iMusicItem mMusicItem;

    public PlayerState(@Nullable iMusicItem musicItem) {
        mMusicItem = musicItem;
    }

    @Nullable
    public iMusicItem getMusicItem() {
        return mMusicItem;
    }

    // TODO: аннотировать @NonNull
    public abstract ePlayerMode getPlayerMode();

}
