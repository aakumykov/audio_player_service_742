package com.gitlab.aakumykov.audio_player_service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gitlab.aakumykov.media_player_module.MediaPlayer526;

public class AudioPlayerService extends Service {

    private static final String ACTION_PLAY = "ACTION_PLAY";
    private static final String ACTION_PAUSE = "ACTION_PAUSE";
    private MediaPlayer526 mMediaPlayer;
    private MediaPlayer526.Callbacks mCallbacks;

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


    // "Динамические" методы
    public void play() {
        mMediaPlayer.play();
    }

    public void pause() {
        mMediaPlayer.pause();
    }


    // "Системные" методы службы
    @Override
    public void onCreate() {
        super.onCreate();

        mCallbacks = new MediaPlayer526.Callbacks() {
            @Override
            public void onPlay() {
                Toast.makeText(AudioPlayerService.this, "Музыка играет", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPause() {
                Toast.makeText(AudioPlayerService.this, "Музыка на паузе", Toast.LENGTH_SHORT).show();
            }
        };

        mMediaPlayer = new MediaPlayer526(mCallbacks);
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

    @Nullable @Override public IBinder onBind(Intent intent) {
        return new Binder(this);
    }


    // Binder
    public static class Binder extends android.os.Binder {

        private final AudioPlayerService mAudioPlayerService;

        public Binder(AudioPlayerService audioPlayerService) {
            super();
            mAudioPlayerService = audioPlayerService;
        }

        public AudioPlayerService getAudioPlayerService() {
            return mAudioPlayerService;
        }
    }
}
