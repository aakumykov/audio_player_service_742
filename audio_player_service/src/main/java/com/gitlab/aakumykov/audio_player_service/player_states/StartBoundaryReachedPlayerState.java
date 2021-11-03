package com.gitlab.aakumykov.audio_player_service.player_states;

import com.gitlab.aakumykov.audio_player_service.other.ePlayerMode;
import com.gitlab.aakumykov.audio_player_service.other.iMusicItem;

public class StartBoundaryReachedPlayerState extends PlayerState {

    public StartBoundaryReachedPlayerState(iMusicItem musicItem) {
        super(musicItem);
    }

    @Override
    public ePlayerMode getPlayerMode() {
        return ePlayerMode.START_BOUNDARY;
    }

}
