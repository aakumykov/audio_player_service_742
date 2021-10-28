package com.gitlab.aakumykov.audio_player_service.errors;

import androidx.annotation.NonNull;

import com.gitlab.aakumykov.audio_player_service.other.MusicItem;

public class PreparingError extends MusicError {

    public PreparingError(@NonNull MusicItem musicItem, @NonNull String errorMsg) {
        super(musicItem, errorMsg);
    }
}
