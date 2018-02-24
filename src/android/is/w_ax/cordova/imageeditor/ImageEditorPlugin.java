package is.w_ax.cordova.imageeditor;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.xinlan.imageeditlibrary.editimage.EditImageActivity;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created by skonb on 2018/02/24.
 */

public class ImageEditorPlugin extends CordovaPlugin {

    protected final static String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    protected static final int ACTION_REQUEST_EDITIMAGE = 100;
    protected static final int PERMISSION_REQUEST = 1000;
    public static final int PERMISSION_DENIED_ERROR = 20;

    protected CallbackContext callbackContext;
    protected JSONArray lastArgs;


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.lastArgs = args;
        this.callbackContext = callbackContext;
        if (assurePermissions()) {
            if (action.equals("edit")) {
                return editAction(args);
            } else {
                return super.execute(action, args, callbackContext);
            }
        } else {
            return true;
        }
    }

    public void downloadImageAndOpenEditor(String url, String destination) {
        Picasso.with(cordova.getContext()).
                load(url).
                into(new PhotoLoader(destination, new Callback() {
                    @Override
                    public void onSuccess() {
                        File outputFile = FileUtils.genEditFile();
                        Intent intent = new Intent(cordova.getContext(), EditImageActivity.class);
                        intent.putExtra("file_path", destination);
                        intent.putExtra("extra_output", outputFile.getAbsolutePath());
                        cordova.startActivityForResult(ImageEditorPlugin.this, intent, ACTION_REQUEST_EDITIMAGE);
                    }

                    @Override
                    public void onError() {
                        callbackContext.error("Failed to download image");
                    }
                }));
    }

    public boolean editAction(JSONArray args) {
        try {
            String destination = FileUtils.genDownloadFile().getAbsolutePath();
            String targetImageUrl = args.getString(1);
            downloadImageAndOpenEditor(targetImageUrl, destination);
            return true;
        } catch (JSONException e) {
            return false;
        }

    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
                return;
            }
        }
        switch (requestCode) {
            case PERMISSION_REQUEST:
                editAction(this.lastArgs);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case ACTION_REQUEST_EDITIMAGE:
                    String newFilePath = intent.getStringExtra(EditImageActivity.EXTRA_OUTPUT);
                    boolean isImageEdit = intent.getBooleanExtra(EditImageActivity.IMAGE_IS_EDIT, false);

                    if (!isImageEdit) {
                        newFilePath = intent.getStringExtra(EditImageActivity.FILE_PATH);
                    }
                    Bitmap bmp = BitmapFactory.decodeFile(newFilePath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
                    byte[] b = baos.toByteArray();
                    String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
                    this.callbackContext.success(encodedImage);

                    break;
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            this.callbackContext.error("Editor Canceled");
        }
    }


    public boolean assurePermissions() {
        boolean writePermission = PermissionHelper.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        boolean readPermission = PermissionHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (!writePermission || !readPermission) {
            PermissionHelper.requestPermissions(this, PERMISSION_REQUEST, permissions);
            return false;
        } else {
            return true;
        }
    }
}
