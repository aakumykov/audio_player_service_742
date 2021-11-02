package com.gitlab.aakumykov.audio_player_service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gitlab.aakumykov.audio_player_service.errors.CommonError;
import com.gitlab.aakumykov.audio_player_service.errors.PlayingError;
import com.gitlab.aakumykov.audio_player_service.errors.PreparingError;
import com.gitlab.aakumykov.audio_player_service.other.MusicItem;
import com.gitlab.aakumykov.audio_player_service.other.PlayingProgress;
import com.gitlab.aakumykov.audio_player_service.other.TrackIfo;
import com.gitlab.aakumykov.audio_player_service.other.ePlayerMode;
import com.gitlab.aakumykov.audio_player_service.other.iMusicItem;
import com.gitlab.aakumykov.audio_player_service.player_states.EndBoundaryReachedPlayerState;
import com.gitlab.aakumykov.audio_player_service.player_states.PausedPlayerState;
import com.gitlab.aakumykov.audio_player_service.player_states.PlayerState;
import com.gitlab.aakumykov.audio_player_service.player_states.PlayingPlayerState;
import com.gitlab.aakumykov.audio_player_service.player_states.ResumedPlayerState;
import com.gitlab.aakumykov.audio_player_service.player_states.StartBoundaryReachedPlayerState;
import com.gitlab.aakumykov.audio_player_service.player_states.StoppedPlayerState;
import com.gitlab.aakumykov.gapless_audio_player.BuildConfig;
import com.gitlab.aakumykov.gapless_audio_player.ErrorCode;
import com.gitlab.aakumykov.gapless_audio_player.GaplessAudioPlayer;
import com.gitlab.aakumykov.gapless_audio_player.Progress;
import com.gitlab.aakumykov.gapless_audio_player.SoundItem;
import com.gitlab.aakumykov.gapless_audio_player.iAudioPlayer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class AudioPlayerService extends Service
{
    private static final String TAG = AudioPlayerService.class.getSimpleName();

    private static final MutableLiveData<PlayerState> sPlayerStateLiveData = new MutableLiveData<>();
    private static final MutableLiveData<TrackIfo> sTrackInfoLiveData = new MutableLiveData<>();
    private static final MutableLiveData<PlayingProgress> sProgressLiveData = new MutableLiveData<>();
    private static final MutableLiveData<PreparingError> sPreparingErrorLiveData = new MutableLiveData<>();
    private static final MutableLiveData<PlayingError> sPlayingErrorLiveData = new MutableLiveData<>();
    private static final MutableLiveData<CommonError> sCommonErrorLiveData = new MutableLiveData<>();

    private static final String COMMAND_PLAY = "COMMAND_PLAY";
    private static final String COMMAND_PAUSE = "COMMAND_PAUSE";
    private static final String COMMAND_RESUME = "COMMAND_RESUME";
    private static final String COMMAND_STOP = "COMMAND_STOP";
    private static final String COMMAND_NEXT = "COMMAND_NEXT";
    private static final String COMMAND_PREV = "COMMAND_PREV";
    private static final String COMMAND_SEEK = "COMMAND_SEEK";

    private static final String BROADCAST_ACTION = "BROADCAST_ACTION_" + View.generateViewId();

    private static final String EXTRA_COMMAND = "EXTRA_COMMAND";
    private static final String EXTRA_POSITION = "EXTRA_POSITION";

    private static final int NOTIFICATION_ID = View.generateViewId();
    private static final String NOTIFICATION_CHANNEL_ID = "AUDIO_PLAYER_NOTIFICATIONS_ID";

    private static final int PI_CODE_PREV = 10;
    private static final int PI_CODE_NEXT = 20;
    private static final int PI_CODE_PAUSE = 30;
    private static final int PI_CODE_RESUME = 40;
    private static final int PI_CODE_STOP = 50;

    private static final long PROGRESS_UPDATE_PERIOD_MS = 1000;

    private NotificationCompat.Builder mPlayingNotificationBuilder;

    private PendingIntent mPrevPendingIntent;
    private PendingIntent mNextPendingIntent;
    private PendingIntent mResumePendingIntent;
    private PendingIntent mPausePendingIntent;
    private PendingIntent mStopPendingIntent;

    private NotificationCompat.Action mSkipToPrevAction;
    private NotificationCompat.Action mSkipToNextAction;
    private NotificationCompat.Action mPauseAction;
    private NotificationCompat.Action mResumeAction;
    private NotificationCompat.Action mStopAction;

    private static final ArrayList<SoundItem> sSoundItemList = new ArrayList<>();
    private iAudioPlayer mAudioPlayer;

    private BroadcastReceiver mBroadcastReceiver;

    private iAudioPlayer.Callbacks mPlayerCallbacks;

    private Timer mTimer;


    // Статические методы для управления службой без соединения (bound) с ней.
    public static void play(@NonNull Context context, List<iMusicItem> musicItemList) {

        sSoundItemList.clear();

        for (iMusicItem musicItem : musicItemList)
            sSoundItemList.add(new SoundItem(
                    musicItem.getId(),
                    musicItem.getTitle(),
                    musicItem.getFilePath()
            ));

        startServiceWithCommand(context, COMMAND_PLAY);
    }

    public static void play(@NonNull Context context, iMusicItem soundItem) {
        play(context, Collections.singletonList(soundItem));
    }

    public static void pause(@NonNull Context context) {
        startServiceWithCommand(context, COMMAND_PAUSE);
    }

    public static void resume(@NonNull Context context) {
        startServiceWithCommand(context, COMMAND_RESUME);
    }

    public static void stop(@NonNull Context context) {
        startServiceWithCommand(context, COMMAND_STOP);
    }

    public static void next(@NonNull Context context) {
        startServiceWithCommand(context, COMMAND_NEXT);
    }

    public static void prev(@NonNull Context context) {
        startServiceWithCommand(context, COMMAND_PREV);
    }

    public static void seekTo(@NonNull Context context, int progress) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_COMMAND, COMMAND_SEEK);
        bundle.putInt(EXTRA_POSITION, progress);

        startServiceWithExtra(context, bundle);
    }

    public static boolean isStopped() {
        PlayerState playerState = sPlayerStateLiveData.getValue();
        return (null == playerState || ePlayerMode.STOPPED.equals(playerState.getPlayerMode()));
    }

    public static boolean isPaused() {
        PlayerState playerState = sPlayerStateLiveData.getValue();
        return (null != playerState && ePlayerMode.PAUSED.equals(playerState.getPlayerMode()));
    }


    // Методы получения LiveData
    public static LiveData<PlayerState> getPlayerStateLiveData() {
        return sPlayerStateLiveData;
    }

    public static LiveData<TrackIfo> getTrackInfoLiveData() {
        return sTrackInfoLiveData;
    }

    public static LiveData<PlayingProgress> getProgressLiveData() {
        return sProgressLiveData;
    }

    public static LiveData<PreparingError> getPreparingErrorLiveData() {
        return sPreparingErrorLiveData;
    }

    public static LiveData<PlayingError> getPlayingErrorLiveData() {
        return sPlayingErrorLiveData;
    }

    public static LiveData<CommonError> getCommonErrorLiveData() {
        return sCommonErrorLiveData;
    }


    // "Системные" методы службы
    @Override
    public void onCreate() {
        super.onCreate();

        preparePlayerCallbacks();
        prepareMediaPlayer();
        prepareNotification();
        prepareBroadcastReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        processStartCommand(intent);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    // Методы подготовки к работе
    private void preparePlayerCallbacks() {

        mPlayerCallbacks = new iAudioPlayer.Callbacks() {

            @Override
            public void onStarted(@NonNull SoundItem soundItem) {

                MusicItem musicItem = new MusicItem(
                        soundItem.getId(),
                        soundItem.getTitle(),
                        soundItem.getFilePath()
                );

                changePlayerStateLiveData(new PlayingPlayerState(musicItem));

                changeTrackInfoLiveData(new TrackIfo(soundItem.getTitle()));

                showPlayingNotification(soundItem.getTitle(), null);

                startProgressTracking();
            }

            @Override
            public void onStopped() {
                stopProgressTracking();
                changePlayerStateLiveData(new StoppedPlayerState());
                removeNotification();
            }

            @Override
            public void onPaused() {
                stopProgressTracking();
                changePlayerStateLiveData(new PausedPlayerState());
                showPauseNotification();
            }

            @Override
            public void onResumed() {
                startProgressTracking();
                changePlayerStateLiveData(new ResumedPlayerState());
                showPlayingNotification(mAudioPlayer.getTitle(), mAudioPlayer.getProgress());
            }

            @Override
            public void onProgress(int position, int duration) {

            }

            @Override
            public void onPreparingError(@NonNull SoundItem soundItem, @NonNull String errorMsg) {
                sPreparingErrorLiveData.setValue(new PreparingError(
                        new MusicItem(soundItem),
                        errorMsg
                ));
            }

            @Override
            public void onPlayingError(@NonNull SoundItem soundItem, @NonNull String errorMsg) {
                sPlayingErrorLiveData.setValue(new PlayingError(
                        new MusicItem(soundItem),
                        errorMsg
                ));
            }

            @Override
            public void onCommonError(@NonNull ErrorCode errorCode, @Nullable String errorDetails) {

                String errorText;

                switch (errorCode) {
                    case NOTHING_TO_PLAY:
                        errorText = getString(R.string.nothing_to_play);
                        break;
                    default:
                        errorText = getString(R.string.unknown_error);
                }

                if (null != errorDetails)
                    errorText = getString(R.string.error_details, errorText, errorDetails);

                sCommonErrorLiveData.setValue(new CommonError(errorText));
            }

            @Override
            public void onNoNextTracks() {
                changePlayerStateLiveData(new EndBoundaryReachedPlayerState());
            }

            @Override
            public void onNoPrevTracks() {
                changePlayerStateLiveData(new StartBoundaryReachedPlayerState());
            }
        };
    }

    private void prepareMediaPlayer() {
        mAudioPlayer = new GaplessAudioPlayer(mPlayerCallbacks);
    }

    private void prepareNotification() {
        preparePendingIntents();
        prepareNotificationActions();
        prepareNotificationsChannel();
        prepareNotificationBuilder();
    }

    private void preparePendingIntents() {

        int piFlag = PendingIntent.FLAG_CANCEL_CURRENT;

        Intent prevIntent = new Intent(BROADCAST_ACTION);
        prevIntent.putExtra(EXTRA_COMMAND, COMMAND_PREV);
        mPrevPendingIntent = PendingIntent.getBroadcast(this, PI_CODE_PREV, prevIntent, piFlag);

        Intent nextIntent = new Intent(BROADCAST_ACTION);
        nextIntent.putExtra(EXTRA_COMMAND, COMMAND_NEXT);
        mNextPendingIntent = PendingIntent.getBroadcast(this, PI_CODE_NEXT, nextIntent, piFlag);

        Intent pauseIntent = new Intent(BROADCAST_ACTION);
        pauseIntent.putExtra(EXTRA_COMMAND, COMMAND_PAUSE);
        mPausePendingIntent = PendingIntent.getBroadcast(this, PI_CODE_PAUSE, pauseIntent, piFlag);

        Intent resumeIntent = new Intent(BROADCAST_ACTION);
        resumeIntent.putExtra(EXTRA_COMMAND, COMMAND_RESUME);
        mResumePendingIntent = PendingIntent.getBroadcast(this, PI_CODE_RESUME, resumeIntent, piFlag);

        Intent stopIntent = new Intent(BROADCAST_ACTION);
        stopIntent.putExtra(EXTRA_COMMAND, COMMAND_STOP);
        mStopPendingIntent = PendingIntent.getBroadcast(this, PI_CODE_STOP, stopIntent, piFlag);
    }

    private void prepareNotificationActions() {

        mSkipToPrevAction = new NotificationCompat.Action(
                R.drawable.ic_prev,
                getString(R.string.skip_to_prev),
                mPrevPendingIntent
        );

        mSkipToNextAction = new NotificationCompat.Action(
                R.drawable.ic_next,
                getString(R.string.skip_to_next),
                mNextPendingIntent
        );

        mResumeAction = new NotificationCompat.Action(
                R.drawable.ic_play,
                getString(R.string.notification_action_resume),
                mResumePendingIntent
        );

        mPauseAction = new NotificationCompat.Action(
                R.drawable.ic_pause,
                getString(R.string.notification_action_pause),
                mPausePendingIntent
        );

        mStopAction = new NotificationCompat.Action(
                R.drawable.ic_close,
                getString(R.string.notification_action_stop),
                mStopPendingIntent
        );
    }

    private void prepareNotificationBuilder() {

        mPlayingNotificationBuilder = new NotificationCompat
                .Builder(this, NOTIFICATION_CHANNEL_ID)
                .setPriority(Notification.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setStyle(
                        new androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0,1,2,3)
                )
        ;
    }

    private void prepareNotificationsChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence name = getString(R.string.notifications_channel_name);
            String description = getString(R.string.notifications_channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManagerCompat.from(this)
                    .createNotificationChannel(channel);
        }
    }

    private void prepareBroadcastReceiver() {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_ACTION);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                processStartCommand(intent);
            }
        };

        registerReceiver(mBroadcastReceiver, intentFilter);
    }


    // Общие методы обработки команд
    private void processStartCommand(@NonNull Intent intent) {

        @Nullable
        String command = intent.getStringExtra(EXTRA_COMMAND);

        if (null != command)
        {
            switch (command) {
                case COMMAND_PLAY:
                    startPlaying();
                    break;

                case COMMAND_PAUSE:
                    pausePlaying();
                    break;

                case COMMAND_RESUME:
                    resumePlaying();
                    break;

                case COMMAND_STOP:
                    stopPlaying();
                    break;

                case COMMAND_NEXT:
                    skipToNext();
                    break;

                case COMMAND_PREV:
                    skipToPrev();
                    break;

                case COMMAND_SEEK:
                    seekToPosition(intent);
                    break;

                default:
                    throw new IllegalArgumentException("Неизвестное действие (EXTRA_COMMAND): "+command);
            }
        }
        else {
            throw new IllegalArgumentException("Intent не содержит EXTRA_COMMAND.");
        }
    }


    // Методы конкретных команд
    private void startPlaying() {
        mAudioPlayer.play(sSoundItemList);
    }

    private void pausePlaying() {
        mAudioPlayer.pause(true);
    }

    private void resumePlaying() {
        mAudioPlayer.resume();
    }

    private void stopPlaying() {
        mAudioPlayer.stop();
    }

    private void skipToNext() {
        mAudioPlayer.next();
    }

    private void skipToPrev() {
        mAudioPlayer.prev();
    }

    private void seekToPosition(@NonNull Intent intent) {
        int position = intent.getIntExtra(EXTRA_POSITION, -1);

        if (-1 != position)
            mAudioPlayer.seekTo(position);
        else
            Log.e(TAG, "В Intent отсутствуют данные о позиции.");
    }



    // Методы манипулирования уведомлением
    private void showPlayingNotification(@NonNull String title, @Nullable Progress progress) {

        mPlayingNotificationBuilder
                .setSmallIcon(R.drawable.ic_play)
                .setContentTitle(title)
                .setContentText(getString(R.string.notification_text_playing))
                .clearActions()
                .addAction(mSkipToPrevAction)
                .addAction(mPauseAction)
                .addAction(mSkipToNextAction)
                .addAction(mStopAction);


        if (null != progress) {

            String format = (progress.getDuration() >= 3600 * 1000) ?
                    "HH:mm:ss" :
                    "mm:ss";

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.getDefault());

            String trackPlayingTime = simpleDateFormat.format(new Date((long) progress.getPosition()));

            mPlayingNotificationBuilder.setSubText(trackPlayingTime);
        }

        startForeground(
                NOTIFICATION_ID,
                mPlayingNotificationBuilder.build()
        );
    }

    private void showPauseNotification() {

        debugLog("showPauseNotification()");

        mPlayingNotificationBuilder
                .setSmallIcon(R.drawable.ic_pause)
                .setContentText(getString(R.string.notification_text_paused))
                .clearActions()
                .addAction(mSkipToPrevAction)
                .addAction(mResumeAction)
                .addAction(mSkipToNextAction)
                .addAction(mStopAction)
                .build();

        startForeground(
                NOTIFICATION_ID,
                mPlayingNotificationBuilder.build()
        );
    }

    private void removeNotification() {
        debugLog("removeNotification()");
        stopForeground(true);
    }



    // Вспомогательные методы
    private static void startServiceWithCommand(@NonNull Context context, @NonNull String command) {
        Intent intent = new Intent(context, AudioPlayerService.class);
        intent.putExtra(EXTRA_COMMAND, command);
        context.startService(intent);
    }

    private static void startServiceWithExtra(@NonNull Context context, @NonNull Bundle extras) {
        Intent intent = new Intent(context, AudioPlayerService.class);
        intent.putExtras(extras);
        context.startService(intent);
    }


    // Методы отправки данных в LiveData
    private void changePlayerStateLiveData(PlayerState playerState) {
        sPlayerStateLiveData.setValue(playerState);
    }

    private void changeTrackInfoLiveData(TrackIfo trackIfo) {
        sTrackInfoLiveData.setValue(trackIfo);
    }

    private void changeProgressLiveData(@NonNull PlayingProgress playingProgress) {
        sProgressLiveData.postValue(playingProgress);
    }


    // Методы отслеживания позиции воспроизведения
    private void startProgressTracking() {

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                @Nullable
                Progress progress = mAudioPlayer.getProgress();
                if (null != progress) {

                    String title = mAudioPlayer.getTitle();
                    if (null != title)
                        showPlayingNotification(title, progress);

                    changeProgressLiveData(new PlayingProgress(
                            progress.getPosition(),
                            progress.getDuration()
                    ));
                }
            }
        };

        mTimer = new Timer();

        mTimer.scheduleAtFixedRate(timerTask, 0, PROGRESS_UPDATE_PERIOD_MS);
    }

    private void stopProgressTracking() {
        if (null != mTimer)
            mTimer.cancel();
    }


    private void debugLog(@Nullable String text) {
        if (BuildConfig.DEBUG) {
            if (null == text)
                text = "null";
            Log.d(TAG, text);
        }
    }
}