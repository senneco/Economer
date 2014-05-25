package net.senneco.economer.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.*;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.googlecode.tesseract.android.TessBaseAPI;
import net.senneco.economer.R;
import net.senneco.economer.data.Price;
import net.senneco.economer.ui.adapters.ItemsAdapter;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private static final Map<Price.Level, Integer> LEVEL_COLORS;

    private Camera mCamera;
    private ItemsAdapter mItemsAdapter;
    private ViewPager mItemsPager;
    private SparseArray<Double> mEconomies;
    private SparseArray<Price.Level> mLevels;
    private View mEconomyBackgroundView;
    private TextView mEconomyText;

    static {
        LEVEL_COLORS = new HashMap<Price.Level, Integer>(5);
        LEVEL_COLORS.put(Price.Level.VERY_GOOD, R.color.very_good);
        LEVEL_COLORS.put(Price.Level.GOOD, R.color.good);
        LEVEL_COLORS.put(Price.Level.NORMAL, R.color.normal);
        LEVEL_COLORS.put(Price.Level.BAD, R.color.bad);
        LEVEL_COLORS.put(Price.Level.VERY_BAD, R.color.very_bad);
    }

    private SurfaceView mCameraSurface;
    private SurfaceHolder mCameraHolder;
    private TextView mPriceText;
    private ImageButton mAcceptButton;
    private ProgressBar mPriceRecognizeProgress;

    boolean mNeedPhoto = false;

    public static final String PACKAGE_NAME = "net.senneco.economer.ui.activities";
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory() + "/SimpleAndroidOCR/";

    // You should have the trained data file in assets folder
    // You can get them at:
    // http://code.google.com/p/tesseract-ocr/downloads/list
    public static final String lang = "eng";

    private static final String TAG = "SimpleAndroidOCR.java";

    protected static final String PHOTO_TAKEN = "photo_taken";

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] paths = new String[]{DATA_PATH, DATA_PATH + "tessdata/"};

        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }

        }

        if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
            try {

                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("tesseract-ocr/tessdata/" + lang + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/" + lang + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                Log.v(TAG, "Copied " + lang + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        }

        mEconomies = new SparseArray<Double>();
        mLevels = new SparseArray<Price.Level>();

        mPriceText = (TextView) findViewById(R.id.text_price);
        mPriceRecognizeProgress = (ProgressBar) findViewById(R.id.progress_price_recognize);

        mAcceptButton = (ImageButton) findViewById(R.id.butt_accept);
        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemsAdapter.getItem(mItemsPager.getCurrentItem()).setPrice(mPriceText.getText().toString());
            }
        });

        findViewById(R.id.butt_shot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPriceRecognizeProgress.setVisibility(View.VISIBLE);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mNeedPhoto = true;
                    }
                }, 400);

                if (mPriceText.getVisibility() == View.VISIBLE) {
                    mPriceText.setVisibility(View.INVISIBLE);
                    mAcceptButton.setVisibility(View.INVISIBLE);
                }
            }
        });
        findViewById(R.id.butt_complete).setOnClickListener(this);

        mCameraSurface = (SurfaceView) findViewById(R.id.surface_camera);

        mCameraHolder = mCameraSurface.getHolder();
        mCameraHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (mCamera == null) {
                        return;
                    }

                    mCamera.setDisplayOrientation(90);
                    mCamera.setPreviewDisplay(holder);
                    mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                        @Override
                        public void onPreviewFrame(byte[] data, Camera camera) {
                            if (mNeedPhoto) {
                                mNeedPhoto = false;

                                new TextRecognizeTask().execute(new BytesWrapper(data));
                            }
                        }
                    });

                    Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
                    float aspect = (float) previewSize.width / previewSize.height;

                    int previewSurfaceWidth = mCameraSurface.getWidth();

                    ViewGroup.LayoutParams lp = mCameraSurface.getLayoutParams();

                    lp.width = previewSurfaceWidth;
                    lp.height = (int) (previewSurfaceWidth / aspect);

                    mCameraSurface.setLayoutParams(lp);

                    mCamera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
        mCameraHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mEconomyBackgroundView = findViewById(R.id.view_economy_background);
        mEconomyText = (TextView) findViewById(R.id.text_economy);

        mItemsAdapter = new ItemsAdapter(getSupportFragmentManager());

        mItemsPager = (ViewPager) findViewById(R.id.pager_items);
        mItemsPager.setAdapter(mItemsAdapter);
        mItemsPager.setPageMargin(10);

        new TessBaseAPI().end();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCamera = Camera.open();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void choosePrice(Price price) {
        mEconomies.put(mItemsPager.getCurrentItem(), price.getEconomy());
        mLevels.put(mItemsPager.getCurrentItem(), price.getLevel());

        double totalEconomy = 0;
        HashMap<Price.Level, Integer> levelsCounter = new HashMap<Price.Level, Integer>();
        for (Price.Level level : Price.Level.values()) {
            levelsCounter.put(level, 0);
        }

        for (int i = 0; i < mItemsAdapter.getCount(); i++) {
            totalEconomy += mEconomies.get(i, 0d);
            Price.Level level = mLevels.get(i);

            if (levelsCounter.get(level) != null) {
                levelsCounter.put(level, levelsCounter.get(level) + 1);
            }
        }

        Map.Entry<Price.Level, Integer> popularLevel = null;
        for (Map.Entry<Price.Level, Integer> levelCounter : levelsCounter.entrySet()) {
            if (popularLevel == null || levelCounter.getValue() >= popularLevel.getValue()) {
                popularLevel = levelCounter;
            }
        }

        mEconomyBackgroundView.setBackgroundColor(getResources().getColor(LEVEL_COLORS.get(popularLevel.getKey())));
        mEconomyText.setText(String.format("%.2f", totalEconomy));

        showNextItem();
    }

    @Override
    public void onBackPressed() {
        int currentItem = mItemsPager.getCurrentItem();
        if (currentItem > 0) {
            mItemsPager.setCurrentItem(currentItem - 1);
        } else {
            super.onBackPressed();
        }
    }

    public void showNextItem() {
        if (mItemsPager.getCurrentItem() == mItemsAdapter.getCount() - 1) {
            mItemsAdapter.addPage();
        }
        mItemsPager.setCurrentItem(mItemsPager.getCurrentItem() + 1);
    }

    private int mCompleteDialogChoose = 0;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.butt_complete:
                new AlertDialog.Builder(this)
                        .setSingleChoiceItems(R.array.complete_dialog_items, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCompleteDialogChoose = which;
                            }
                        })
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (mCompleteDialogChoose) {
                                    case 0:
                                        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                                        intent.setType("text/plain");
                                        intent.putExtra(Intent.EXTRA_TITLE, R.string.app_name);
                                        intent.putExtra(Intent.EXTRA_TEXT, "Хэхэй! Я сэкономил " + mEconomyText.getText().toString() + " рублей!\nEconomer в Google Play! http://google.com");

                                        startActivity(Intent.createChooser(intent, "Расшарить через"));
                                    case 1:
                                        mItemsAdapter = new ItemsAdapter(getSupportFragmentManager());
                                        mItemsPager.setAdapter(mItemsAdapter);
                                        mEconomyText.setText("0.0");
                                        mEconomyBackgroundView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                                        break;
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                break;
        }
    }

    private static class BytesWrapper {
        private byte[] mBytes;

        public BytesWrapper(byte[] bytes) {
            mBytes = bytes;
        }

        public byte[] getBytes() {
            return mBytes;
        }
    }

    private class TextRecognizeTask extends AsyncTask<BytesWrapper, Void, String> {

        @Override
        protected String doInBackground(BytesWrapper... params) {
            Camera.Parameters parameters = mCamera.getParameters();
            int width = parameters.getPreviewSize().width;
            int height = parameters.getPreviewSize().height;

            YuvImage yuv = new YuvImage(params[0].getBytes(), parameters.getPreviewFormat(), width, height, null);

            int l = (int) getResources().getDimension(R.dimen.camera_sight_left);
            int t = (int) getResources().getDimension(R.dimen.camera_sight_top);
            int r = (int) getResources().getDimension(R.dimen.camera_sight_right);
            int b = (int) getResources().getDimension(R.dimen.camera_sight_bottom);

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            yuv.compressToJpeg(new Rect(t, height - r, b, height - l), 100, out);

            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            byte[] bytes = out.toByteArray();

            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            TessBaseAPI baseApi = new TessBaseAPI();
            baseApi.setDebug(true);
            baseApi.init(DATA_PATH, lang);
            baseApi.setImage(bitmap);

            String recognizedText = baseApi.getUTF8Text();

            baseApi.end();

            bitmap.recycle();

            // You now have the text in recognizedText var, you can do anything with it.
            // We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
            // so that garbage doesn't make it to the display.

            Log.v(TAG, "OCRED TEXT: " + recognizedText);

            recognizedText = recognizedText.replaceAll("[^0-9.,]+", "");

            return recognizedText;
        }

        @Override
        protected void onPostExecute(String recognizedText) {
            super.onPostExecute(recognizedText);

            mPriceRecognizeProgress.setVisibility(View.GONE);

            double price;
            try {
                price = Double.valueOf(recognizedText);
            } catch (NumberFormatException e) {
                if (mPriceText.getVisibility() == View.INVISIBLE) {
                    mPriceText.setVisibility(View.VISIBLE);
                    mAcceptButton.setVisibility(View.VISIBLE);
                }
                Toast.makeText(MainActivity.this, "Не могу распознать цену", Toast.LENGTH_SHORT).show();
                return;
            }

            mPriceText.setText(String.valueOf(price));
            mPriceText.setVisibility(View.VISIBLE);
            mAcceptButton.setVisibility(View.VISIBLE);
        }
    }
}
