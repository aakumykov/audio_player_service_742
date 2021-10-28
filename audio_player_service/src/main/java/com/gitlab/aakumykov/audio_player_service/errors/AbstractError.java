package com.gitlab.aakumykov.audio_player_service.errors;

import androidx.annotation.NonNull;

public abstract class AbstractError {

    @NonNull
    private final String mErrorMsg;

    public AbstractError(@NonNull String errorMsg) {
        mErrorMsg = errorMsg;
    }

    @NonNull
    public String getErrorMsg() {
        return mErrorMsg;
    }
}
