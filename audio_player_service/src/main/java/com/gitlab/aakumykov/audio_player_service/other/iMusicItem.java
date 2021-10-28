package com.gitlab.aakumykov.audio_player_service.other;

import androidx.annotation.NonNull;

public interface iMusicItem {
    @NonNull String getTitle();
    @NonNull String getFilePath();
}
