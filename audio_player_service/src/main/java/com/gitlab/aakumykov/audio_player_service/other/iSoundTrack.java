package com.gitlab.aakumykov.audio_player_service.other;

import androidx.annotation.NonNull;

public interface iSoundTrack {
    @NonNull String getId();
    @NonNull String getTitle();
    @NonNull String getFilePath();
}
