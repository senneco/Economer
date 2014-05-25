package net.senneco.economer.ui.activities;

import android.content.res.AssetManager;
import android.graphics.*;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.googlecode.tesseract.android.TessBaseAPI;
import net.senneco.economer.R;
import net.senneco.economer.data.Price;
import net.senneco.economer.ui.adapters.ItemsAdapter;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends ActionBarActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

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
                mNeedPhoto = true;
            }
        });

        mCameraSurface = (SurfaceView) findViewById(R.id.surface_camera);

        mCameraHolder = mCameraSurface.getHolder();
        mCameraHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    mCamera.setDisplayOrientation(90);
                    mCamera.setPreviewDisplay(holder);
                    mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                        @Override
                        public void onPreviewFrame(byte[] data, Camera camera) {
                            if (mNeedPhoto) {
                                mNeedPhoto = false;

                                Camera.Parameters parameters = camera.getParameters();
                                int width = parameters.getPreviewSize().width;
                                int height = parameters.getPreviewSize().height;

                                YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);

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
                                bitmap = Bitmap.createBitmap(bitmap, 0 , 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

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

                                double price;
                                try {
                                    price = Double.valueOf(recognizedText);
                                } catch (NumberFormatException e) {
                                    Toast.makeText(MainActivity.this, "Не могу распознать цену", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                mPriceText.setText(String.valueOf(price));
                                mPriceText.setVisibility(View.VISIBLE);
                                mAcceptButton.setVisibility(View.VISIBLE);
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
}
