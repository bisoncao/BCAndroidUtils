package com.bisoncao.bcgifutildemo;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bisoncao.bccommonutil.BCDebug;
import com.bisoncao.bccommonutil.BCNullUtil;
import com.bisoncao.bccommonutil.BCSingleToastUtil;
import com.bisoncao.bcgifutil.BCGifPlayCallback;
import com.bisoncao.bcgifutil.BCGifUtil;
import com.bisoncao.bcgifutil.GifAnimationDrawable;

public class BaseActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static int GIF_RES_ID;
    private static final int[] GIF_IDS = { R.raw.gdemo_cool,
            R.raw.gdemo_penguin };
    private static final int[] BTN_IDS = { R.id.btn_display_dialog_1,
            R.id.btn_display_dialog_2 };
    private static final int BTN_ID_LENGTH = BTN_IDS.length;

    protected Context mContext = this;
    private static final String TAG = "BaseActivity";
    /**
     * 单例加载对话框（loading dialog）
     */
    protected static Dialog progressDialog;
    private static GifAnimationDrawable gifAnimationDrawable;
    private static View gifCarrier;

    private BCSingleToastUtil singleToast;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        singleToast = new BCSingleToastUtil(mContext);

        for (int id : BTN_IDS) {
            findViewById(id).setOnClickListener(this);
        }

    }

    public void recursiveDo() {
        showProgressDialog(null, false);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                closeProgressDialog();
            }
        }, 3000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 得到自定义的progressDialog
     * @param context
     * @param loadingString
     *            load界面显示的文字
     * @param canCancel
     *            是否可以通过返回或者点击其他区域退出loading
     * @return
     */
    public static Dialog createLoadingDialogWithString(Context context,
            String loadingString, boolean canCancel) {
        if (context == null) {
            return null;
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.new_loading_dialog, null);// 得到加载view
        RelativeLayout layout = (RelativeLayout) v
                .findViewById(R.id.dialog_view);// 加载布局
        gifCarrier = v.findViewById(R.id.progressimg);
        TextView loadingText = (TextView) v.findViewById(R.id.loading_text);
        if (BCNullUtil.isNullString(loadingString)) {
            loadingText.setVisibility(View.INVISIBLE);
        } else {
            loadingText.setVisibility(View.VISIBLE);
            loadingText.setText(loadingString);
        }

        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog
        loadingDialog.setCanceledOnTouchOutside(canCancel);// 设置点击屏幕Dialog不消失
        loadingDialog.setCancelable(canCancel);// 是否可用“返回键”取消
        loadingDialog.setContentView(layout, new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));// 设置布局
        return loadingDialog;

    }

    public void showProgressDialog(String loadingString, final boolean canCancel) {
        if (isExited()) {
            return;
        }

        if (progressDialog == null) {
            progressDialog = createLoadingDialogWithString(mContext,
                    loadingString, canCancel);
        }

        if (progressDialog != null) {
            BCGifUtil.playGif(mContext, handler, gifCarrier, GIF_RES_ID,
                    new BCGifPlayCallback() {
                        @Override
                        public void onPlayedSuc(GifAnimationDrawable gif) {
                            if (isExited()) {
                                return;
                            }
                            progressDialog.show();
                            if (!canCancel) {
                                singleToast.showShortToast("播放完毕后将会自动关闭。");
                            }
                            gifAnimationDrawable = gif;
                            BCDebug.d(TAG, "onPlayedSuc");
                        }

                        @Override
                        public void onPalyedFail() {

                        }
                    });
        }
    }

    /**
     * 关闭加载对话框
     */
    public void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            if (gifAnimationDrawable != null) {
                try {
                    gifAnimationDrawable.stop();

                } catch (Exception e) {
                    BCDebug.e(BCDebug.BISON, "gif: error when stop");
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        int find = -1;
        for (int i = 0; i < BTN_ID_LENGTH; i++) {
            if (BTN_IDS[i] == id) {
                find = i;
                break;
            }
        }
        if (find != -1) {
            GIF_RES_ID = GIF_IDS[find];
            recursiveDo();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.isDestoryed = true;
        handler.removeCallbacksAndMessages(null);
        /**
         * Must reset to avoid next time crash: if not reset, calling its show()
         * at next launch will use the previous context (as it is a static
         * variable), which has already been destroyed.
         * Error log:
         * android.view.WindowManager$BadTokenException: Unable to add window --
         * token android.os.BinderProxy@4ef614c is not valid; is your activity
         * running?
         */
        progressDialog = null;
        Log.d(TAG, "onDestroy");
    }

    private boolean isDestoryed;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public boolean isDestroyedVersionSafe() {
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ? this
                .isDestroyed() : this.isDestoryed;
    }

    public boolean isExited() {
        Log.d(TAG, "isExited: isFinishing = " + this.isFinishing());
        Log.d(TAG, "isExited: isDestroyed = " + this.isDestroyedVersionSafe());
        return this.isFinishing() || isDestroyedVersionSafe();
    }
}
