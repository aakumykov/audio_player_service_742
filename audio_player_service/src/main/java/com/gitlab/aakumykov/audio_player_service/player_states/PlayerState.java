package com.gitlab.aakumykov.audio_player_service.player_states;

import androidx.annotation.NonNull;

import com.gitlab.aakumykov.audio_player_service.other.ePlayerMode;
import com.gitlab.aakumykov.audio_player_service.other.iMusicItem;

public abstract class PlayerState {

    @NonNull
    private final iMusicItem mMusicItem;

    public PlayerState(@NonNull iMusicItem musicItem) {
        mMusicItem = musicItem;
    }

    @NonNull
    public iMusicItem getMusicItem() {
        return mMusicItem;
    }

    // TODO: аннотировать @NonNull
    public abstract ePlayerMode getPlayerMode();

}
