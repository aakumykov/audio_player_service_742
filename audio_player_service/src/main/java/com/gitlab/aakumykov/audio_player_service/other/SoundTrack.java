package com.gitlab.aakumykov.audio_player_service.other;

import androidx.annotation.NonNull;

import com.gitlab.aakumykov.gapless_audio_player.SoundItem;

public class SoundTrack implements iSoundTrack {

    @NonNull private final String mId;
    @NonNull private final String mTitle;
    @NonNull private final String mFilePath;

    public SoundTrack(@NonNull String id, @NonNull String title, @NonNull String filePath) {
        mId = id;
        mTitle = title;
        mFilePath = filePath;
    }

    public SoundTrack(@NonNull SoundItem soundItem) {
        mId = soundItem.getId();
        mTitle = soundItem.getTitle();
        mFilePath = soundItem.getFilePath();
    }

    @NonNull
    @Override
    public String getId() {
        return mId;
    }

    @NonNull
    public String getTitle() {
        return mTitle;
    }

    @NonNull
    public String getFilePath() {
        return mFilePath;
    }

    @Override
    public String toString() {
        return "SoundTrack{" +
                "mTitle='" + mTitle + '\'' +
                ", mFilePath='" + mFilePath + '\'' +
                '}';
    }
}
