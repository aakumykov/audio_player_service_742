package com.gitlab.aakumykov.audio_player_service.player_states;

import androidx.annotation.Nullable;

import com.gitlab.aakumykov.audio_player_service.other.ePlayerMode;
import com.gitlab.aakumykov.audio_player_service.other.iSoundTrack;

public class StartBoundaryReachedPlayerState extends PlayerState {

    public StartBoundaryReachedPlayerState(@Nullable iSoundTrack musicItem) {
        super(musicItem);
    }

    @Override
    public ePlayerMode getPlayerMode() {
        return ePlayerMode.START_BOUNDARY;
    }

}
