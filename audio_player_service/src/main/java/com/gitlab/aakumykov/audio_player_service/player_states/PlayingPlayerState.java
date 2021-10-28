package com.gitlab.aakumykov.audio_player_service.player_states;

import androidx.annotation.NonNull;

import com.gitlab.aakumykov.audio_player_service.other.ePlayerMode;
import com.gitlab.aakumykov.audio_player_service.other.iMusicItem;

public class PlayingPlayerState extends PlayerState {

    private final iMusicItem mSoundItem;

    public PlayingPlayerState(iMusicItem musicItem) {
        this.mSoundItem = musicItem;
    }

    @Override
    public ePlayerMode getPlayerMode() {
        return ePlayerMode.PLAYING;
    }

    public iMusicItem getMusicItem() {
        return new iMusicItem() {

            @NonNull @Override
            public String getTitle() {
                return mSoundItem.getTitle();
            }

            @NonNull @Override
            public String getFilePath() {
                return mSoundItem.getFilePath();
            }
        };
    }
}
