package is.w_ax.cordova.imageeditor;

/**
 * Created by skonb on 2018/02/25.
 */

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;

public class PhotoLoader implements Target {
    private final String outputPath;
    private Callback callback;

    public PhotoLoader(String outputPath, Callback callback) {
        this.outputPath = outputPath;
        this.callback = callback;
    }

    @Override
    public void onPrepareLoad(Drawable arg0) {
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom arg1) {
        File file = new File(outputPath);
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream ostream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
            ostream.close();
            this.callback.onSuccess();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBitmapFailed(Drawable arg0) {
        this.callback.onError();
    }
}
