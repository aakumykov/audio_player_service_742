package com.gitlab.aakumykov.audio_player_service.other;

import androidx.annotation.NonNull;

import com.gitlab.aakumykov.gapless_audio_player.SoundItem;

public class MusicItem implements iMusicItem {

    @NonNull private final String mTitle;
    @NonNull private final String mFilePath;

    public MusicItem(@NonNull String title, @NonNull String filePath) {
        mTitle = title;
        mFilePath = filePath;
    }

    public MusicItem(@NonNull SoundItem soundItem) {
        mTitle = soundItem.getTitle();
        mFilePath = soundItem.getFilePath();
    }

    @NonNull
    public String getTitle() {
        return mTitle;
    }

    @NonNull
    public String getFilePath() {
        return mFilePath;
    }
}
