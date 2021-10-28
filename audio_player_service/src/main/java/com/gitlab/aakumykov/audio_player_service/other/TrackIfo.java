package com.gitlab.aakumykov.audio_player_service.other;

import androidx.annotation.NonNull;

public class TrackIfo {

    @NonNull
    private final String title;

    public TrackIfo(@NonNull String title) {
        this.title = title;
    }

    @NonNull
    public String getTitle() {
        return title;
    }
}
