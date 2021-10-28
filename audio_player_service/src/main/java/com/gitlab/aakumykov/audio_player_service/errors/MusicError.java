package com.gitlab.aakumykov.audio_player_service.errors;

import androidx.annotation.NonNull;

import com.gitlab.aakumykov.audio_player_service.other.MusicItem;

public abstract class MusicError extends AbstractError {

    @NonNull
    private final MusicItem mMusicItem;

    public MusicError(@NonNull MusicItem musicItem, @NonNull String errorMsg) {
        super(errorMsg);
        mMusicItem = musicItem;
    }

    @NonNull
    public MusicItem getMusicItem() {
        return mMusicItem;
    }
}
