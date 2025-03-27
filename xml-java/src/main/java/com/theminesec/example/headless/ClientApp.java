package com.theminesec.example.headless;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.theminesec.sdk.headless.HeadlessSetup;
import com.theminesec.sdk.headless.model.WrappedResult;
import com.theminesec.sdk.headless.model.setup.SdkInitResp;

public class ClientApp extends Application {
    private final MutableLiveData<WrappedResult<SdkInitResp>> _sdkInitStatus = new MutableLiveData<>();
    public LiveData<WrappedResult<SdkInitResp>> sdkInitStatus = _sdkInitStatus;

    @Override
    public void onCreate() {
        super.onCreate();

        HeadlessSetup.INSTANCE.initSoftPosCallBack(
                this,
                "test.license",
                result -> {
                    Log.d(Constants.TAG, "Application init: " + result.toString());
                    _sdkInitStatus.setValue(result);
                    return null;
                }
        );

    }
}

