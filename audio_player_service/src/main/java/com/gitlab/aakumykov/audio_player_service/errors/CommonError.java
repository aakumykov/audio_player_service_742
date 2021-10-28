package com.gitlab.aakumykov.audio_player_service.errors;

import androidx.annotation.NonNull;

public class CommonError extends AbstractError {

    public CommonError(@NonNull String errorMsg) {
        super(errorMsg);
    }
}
