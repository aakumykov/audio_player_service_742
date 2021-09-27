package com.gitlab.aakumykov.audio_player_service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gitlab.aakumykov.media_player_module.MediaPlayer526;

public class AudioPlayerService
        extends Service
        implements MediaPlayer526.Callbacks
{
    private static final String ACTION_PLAY = "ACTION_PLAY";
    private static final String ACTION_PAUSE = "ACTION_PAUSE";
    private MediaPlayer526 mMediaPlayer;


    // Статическе методы для удобства
    public static void play(@NonNull Context context) {
        Intent intent = new Intent(context, MediaPlayer526.class);
        intent.setAction(ACTION_PLAY);
        context.startService(intent);
    }

    public static void pause(@NonNull Context context) {
        Intent intent = new Intent(context, MediaPlayer526.class);
        intent.setAction(ACTION_PAUSE);
        context.startService(intent);
    }


    // "Системные" методы службы
    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer526(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = intent.getAction();

        switch (action) {
            case ACTION_PLAY:
                mMediaPlayer.play();
                break;
            case ACTION_PAUSE:
                mMediaPlayer.pause();
                break;
            default:
                throw new IllegalArgumentException("Неизвестное действие в Intent: "+action);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable @Override public IBinder onBind(Intent intent) { return null; }


    // MediaPlayer526.Callbacks
    @Override
    public void onPlay() {
        Toast.makeText(this, "Музыка играет", Toast.LENGTH_SHORT).show();
        // TODO: уведомление
    }

    @Override
    public void onPause() {
        Toast.makeText(this, "Музыка на паузе", Toast.LENGTH_SHORT).show();
        // TODO: уведомление
    }
}
