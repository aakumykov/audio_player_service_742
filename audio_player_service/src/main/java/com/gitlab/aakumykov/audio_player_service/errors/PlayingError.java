package com.gitlab.aakumykov.audio_player_service.errors;

import androidx.annotation.NonNull;

import com.gitlab.aakumykov.audio_player_service.other.SoundTrack;

public class PlayingError extends MusicError {

    public PlayingError(@NonNull SoundTrack musicItem, @NonNull String errorMsg) {
        super(musicItem, errorMsg);
    }
}
