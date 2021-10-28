package com.gitlab.aakumykov.aps_demo;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gitlab.aakumykov.aps_demo.databinding.ActivityDemoBinding;
import com.gitlab.aakumykov.audio_player_service.AudioPlayerService;
import com.gitlab.aakumykov.audio_player_service.errors.CommonError;
import com.gitlab.aakumykov.audio_player_service.errors.PlayingError;
import com.gitlab.aakumykov.audio_player_service.errors.PreparingError;
import com.gitlab.aakumykov.audio_player_service.other.MusicItem;
import com.gitlab.aakumykov.audio_player_service.other.PlayingProgress;
import com.gitlab.aakumykov.audio_player_service.other.TrackIfo;
import com.gitlab.aakumykov.audio_player_service.other.ePlayerMode;
import com.gitlab.aakumykov.audio_player_service.other.iMusicItem;
import com.gitlab.aakumykov.audio_player_service.player_states.PlayerState;
import com.gitlab.aakumykov.audio_player_service.player_states.PlayingPlayerState;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class DemoActivity extends AppCompatActivity {

    private static final String TAG = DemoActivity.class.getSimpleName();
    private ActivityDemoBinding mViewBinding;
    private File mMusicDir;
    private final List<iMusicItem> mMusicList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prepareLayout();
        prepareGUI();
        prepareLiveData();

        DemoActivityPermissionsDispatcher.prepareMusicListWithPermissionCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("PERMISSIONS", "onRequestPermissionsResult()");
        Log.d("PERMISSIONS", "requestCode: "+requestCode);
        Log.d("PERMISSIONS", "permissions: "+permissions);
        Log.d("PERMISSIONS", "grantResults: "+grantResults);
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void prepareMusicList() {

        mMusicDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);

        Pattern mp3patten = Pattern.compile("^.+\\.\\s*mp3\\s*", Pattern.CASE_INSENSITIVE);

        File[] mp3files = mMusicDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(mp3patten.pattern());
            }
        });

        if (null != mp3files) {

            Stream.of(mp3files)
                    .map(File::getName)
                    .forEach(fileName -> {
                        String filePath = mMusicDir + "/" + fileName;
                        mMusicList.add(new MusicItem(fileName, filePath));
                    });
        }
    }


    // Методы подготовки
    private void prepareLayout() {
        mViewBinding = ActivityDemoBinding.inflate(getLayoutInflater());
        setContentView(mViewBinding.getRoot());
    }

    private void prepareGUI() {

        mViewBinding.startButton.setOnClickListener(this::onPlayPauseButtonClicked);
        mViewBinding.nextButton.setOnClickListener(this::onNextButtonClicked);
        mViewBinding.prevButton.setOnClickListener(this::onPrevButtonClicked);
        mViewBinding.stopButton.setOnClickListener(this::onStopPlayingClicked);

        mViewBinding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    AudioPlayerService.seekTo(DemoActivity.this, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        disableSeekBar();
    }

    private void prepareLiveData() {
        AudioPlayerService.getPlayerStateLiveData().observe(this, this::onPlayerStateChanged);
        AudioPlayerService.getTrackInfoLiveData().observe(this, this::onTrackInfoChanged);
        AudioPlayerService.getProgressLiveData().observe(this, this::onProgressChanged);

        AudioPlayerService.getPreparingErrorLiveData().observe(this, this::onPreparingError);
        AudioPlayerService.getPlayingErrorLiveData().observe(this, this::onPlayingError);
        AudioPlayerService.getCommonErrorLiveData().observe(this, this::onCommonError);
    }


    // Методы обработки событий интерфейса
    private void onPlayPauseButtonClicked(View view) {

        hideError();

        if (AudioPlayerService.isStopped()) {
            AudioPlayerService.play(this, mMusicList);
        }
        else {
            if (AudioPlayerService.isPaused()) {
                AudioPlayerService.resume(this);
            }
            else {
                AudioPlayerService.pause(this);
            }
        }
    }

    private void onStopPlayingClicked(View view) {
        AudioPlayerService.stop(this);
    }

    private void onNextButtonClicked(View view) {
        AudioPlayerService.next(this);
    }

    private void onPrevButtonClicked(View view) {
        AudioPlayerService.prev(this);
    }


    // Обработка LiveData-событий плеера
    private void onPlayerStateChanged(@NonNull PlayerState playerState) {

        ePlayerMode playerMode = playerState.getPlayerMode();

        switch (playerMode) {
            case PLAYING:
                onPlayingState((PlayingPlayerState) playerState);
                break;
            case STOPPED:
                onStoppedState();
                break;
            case PAUSED:
                onPausedState();
                break;
            case RESUMED:
                onResumedState();
                break;
            case START_BOUNDARY:
                onStartBoundaryReached();
                break;
            case END_BOUNDARY:
                onEndBoundaryReached();
                break;
            default:
                throw new IllegalStateException("Неизвестное значение playerMode: "+playerMode);
        }
    }

    private void onTrackInfoChanged(@NonNull TrackIfo trackIfo) {
        showTrackName(trackIfo.getTitle());
    }

    private void onProgressChanged(@NonNull PlayingProgress playingProgress) {
        showProgress(playingProgress.getPosition(), playingProgress.getDuration());
    }

    private void onPreparingError(@NonNull PreparingError preparingError) {
        showToast(getString(
                R.string.error_preparing_track,
                preparingError.getMusicItem().getTitle(),
                preparingError.getErrorMsg()
        ));
    }

    private void onPlayingError(@NonNull PlayingError playingError) {
        showToast(getString(
                R.string.error_playing_track,
                playingError.getMusicItem().getTitle(),
                playingError.getErrorMsg()
        ));
    }

    private void onCommonError(@NonNull CommonError commonError) {
        showError(commonError.getErrorMsg());
    }


    // Методы обработки состояний
    private void onPlayingState(@NonNull PlayingPlayerState playingPlayerState) {
        showTrackName(playingPlayerState.getMusicItem().getTitle());
        showPauseButton();
        enableSeekBar();
    }

    private void onPausedState() {
        showPlayButton();
    }

    private void onResumedState() {
        showPauseButton();
    }

    private void onStoppedState() {
        hideTrackName();
        showPlayButton();
        disableSeekBar();
    }

    // TODO: а не передавать ли эти СОБЫТИЯ через широковещательное сообщение?
    private void onStartBoundaryReached() {
        showToast("Это начало списка");
    }

    private void onEndBoundaryReached() {
        showToast("Конец списка");
    }



    // Методы изменения интерфейса
    private void showPlayButton() {
        mViewBinding.startButton.setImageResource(R.drawable.ic_play);
    }

    private void showPauseButton() {
        mViewBinding.startButton.setImageResource(R.drawable.ic_pause);
    }

    private void showTrackName(String trackName) {
        mViewBinding.trackNameView.setText(trackName);
        ViewUtils.show(mViewBinding.trackNameView);
    }

    private void hideTrackName() {
        mViewBinding.trackNameView.setText("");
    }

    private void showError(@NonNull String errorMsg) {
        mViewBinding.errorView.setText(errorMsg);
        ViewUtils.show(mViewBinding.errorView);
    }

    private void hideError() {
        mViewBinding.errorView.setText("");
    }

    public void enableSeekBar() {
        ViewUtils.enable(mViewBinding.seekBar);
    }

    private void disableSeekBar() {
        mViewBinding.seekBar.setProgress(0);
        ViewUtils.disable(mViewBinding.seekBar);
    }

    private void showProgress(int position, int duration) {
        mViewBinding.seekBar.setProgress(position);
        mViewBinding.seekBar.setMax(duration);
    }

    private void displayLoadedFilesCount() {

        String fileQuantityString = getResources().getQuantityString(
                R.plurals.file, mMusicList.size()
        );

        String text = getString(
                R.string.files_are_loaded_from_dir,
                String.valueOf(mMusicList.size()),
                fileQuantityString,
                mMusicDir.getName()
        );

        showStatusText(text);
    }

    private void showStatusText(@NonNull String text) {
        mViewBinding.statusTextView.setText(text);
    }

    // Вмпомогательные методы
    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

}