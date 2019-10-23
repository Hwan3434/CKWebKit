package ck.lib.module.webkit.cklistener;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;


public class CKWebChromeClient extends WebChromeClient {

    private CKWebClientListener mJListener;

    public CKWebChromeClient(Activity activity) {
        this.mActivity = activity;
    }

    public CKWebChromeClient(Activity activity, CKWebClientListener aJListener){
        this.mActivity = activity;
        mJListener = aJListener;
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {

        if (consoleMessage.message().contains("Uncaught ReferenceError: oLibMobileApp is not defined")) {
            // 웹뷰에서 자바스크립트 호출시 해당 라이브러리가 웹에 없을떄 동작
            if(mJListener != null) {
                mJListener.onNotDefinedLib();
            }
        }

        return super.onConsoleMessage(consoleMessage);
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);

        if (mJListener != null) {
            mJListener.onPregress(newProgress);
        }
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);

        if (mJListener != null) {
            mJListener.onTitle(title);
        }

    }

    private Activity mActivity = null;

    private View mCustomView;
    private CustomViewCallback mCustomViewCallback;
    private int mOriginalOrientation;
    private FrameLayout mFullscreenContainer;
    private static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {

        if (mCustomView != null) {
            callback.onCustomViewHidden();
            return;
        }

        mOriginalOrientation = mActivity.getRequestedOrientation();
        FrameLayout decor = (FrameLayout) mActivity.getWindow().getDecorView();
        mFullscreenContainer = new FullscreenHolder(mActivity);
        mFullscreenContainer.addView(view, COVER_SCREEN_PARAMS);
        decor.addView(mFullscreenContainer, COVER_SCREEN_PARAMS);
        mCustomView = view;
        setFullscreen(true);
        mCustomViewCallback = callback;
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        super.onShowCustomView(view, callback);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
        this.onShowCustomView(view, callback);
    }

    @Override
    public void onHideCustomView() {
        if (mCustomView == null) {
            return;
        }

        setFullscreen(false);
        FrameLayout decor = (FrameLayout) mActivity.getWindow().getDecorView();
        decor.removeView(mFullscreenContainer);
        mFullscreenContainer = null;
        mCustomView = null;
        mCustomViewCallback.onCustomViewHidden();
        mActivity.setRequestedOrientation(mOriginalOrientation);

    }

    private void setFullscreen(boolean enabled) {

        Window win = mActivity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        if (enabled) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
            if (mCustomView != null) {
                mCustomView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        }
        win.setAttributes(winParams);
    }

    private static class FullscreenHolder extends FrameLayout {
        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ContextCompat.getColor(ctx, android.R.color.black));
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }
    }

}
