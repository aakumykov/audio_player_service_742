package com.gitlab.aakumykov.audio_player_service.player_states;

import com.gitlab.aakumykov.audio_player_service.other.ePlayerMode;
import com.gitlab.aakumykov.audio_player_service.other.iMusicItem;

public class StoppedPlayerState extends PlayerState {

    public StoppedPlayerState(iMusicItem musicItem) {
        super(musicItem);
    }

    @Override
    public ePlayerMode getPlayerMode() {
        return ePlayerMode.STOPPED;
    }

}
