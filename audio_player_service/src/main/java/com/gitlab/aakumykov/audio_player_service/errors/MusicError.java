package com.gitlab.aakumykov.audio_player_service.errors;

import androidx.annotation.NonNull;

import com.gitlab.aakumykov.audio_player_service.other.SoundTrack;

public abstract class MusicError extends AbstractError {

    @NonNull
    private final SoundTrack mMusicItem;

    public MusicError(@NonNull SoundTrack musicItem, @NonNull String errorMsg) {
        super(errorMsg);
        mMusicItem = musicItem;
    }

    @NonNull
    public SoundTrack getMusicItem() {
        return mMusicItem;
    }
}
