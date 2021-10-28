package com.gitlab.aakumykov.audio_player_service.player_states;

import com.gitlab.aakumykov.audio_player_service.other.ePlayerMode;

public class StartBoundaryReachedPlayerState extends PlayerState {
    @Override
    public ePlayerMode getPlayerMode() {
        return ePlayerMode.START_BOUNDARY;
    }
}
