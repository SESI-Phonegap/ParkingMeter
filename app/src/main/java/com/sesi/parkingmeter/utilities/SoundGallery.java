package com.sesi.parkingmeter.utilities;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.sesi.parkingmeter.R;

public class SoundGallery {

    final String TAG = this.getClass().getSimpleName();

    private Context context;

    public void setSoundUri(Uri soundUri) {
        this.soundUri = soundUri;
    }

    private Uri soundUri;

    public SoundGallery(Context context){
        this.context = context;
    }

    public Intent openGalleryIntent(){
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        return Intent.createChooser(intent, getChooserTitle());
    }

    public String getChooserTitle(){
        return context.getString(R.string.titleSound);
    }

    public String getPath() {

        String path;
        // SDK < API11
        if (Build.VERSION.SDK_INT < 11) {
            path = FilePath.getRealPathFromURI_BelowAPI11(context, soundUri);
        }

        // SDK >= 11 && SDK < 19
        else if (Build.VERSION.SDK_INT < 19){
            path = FilePath.getRealPathFromURI_API11to18(context, soundUri);
        }
        // SDK > 19 (Android 4.4)
        else
            path = FilePath.getPath(context, soundUri);

        return path;
    }
}
