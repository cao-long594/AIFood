package com.example.food.domain.recognition;

import java.io.File;

public interface FoodRecognitionClient {
    interface Callback {
        void onSuccess(RecognitionResult result);

        void onError(String message);
    }

    void recognize(File image, Callback callback);
}
