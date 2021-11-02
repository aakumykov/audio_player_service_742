package com.gitlab.aakumykov.audio_player_service.other;

import androidx.annotation.NonNull;

public interface iMusicItem {
    @NonNull String getId();
    @NonNull String getTitle();
    @NonNull String getFilePath();
}
