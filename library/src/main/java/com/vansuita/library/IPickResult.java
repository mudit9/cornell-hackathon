package com.vansuita.library;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Created by jrvansuita on 01/11/16.
 */

public class IPickResult {

    public interface IPickResultBitmap {
        void onPickImageResult(Bitmap bitmap);
    }


    public interface IPickResultUri {
        void onPickImageResult(Uri bitmap);
    }

}