package com.gitlab.aakumykov.audio_player_service.other;

public class PlayingProgress {

    private final int position;
    private final int duration;

    public PlayingProgress(int position, int duration) {
        this.position = position;
        this.duration = duration;
    }

    public int getPosition() {
        return position;
    }

    public int getDuration() {
        return duration;
    }
}
