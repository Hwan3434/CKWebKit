package ck.lib.module.webkit.cklistener;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.URISyntaxException;

import ck.lib.module.webkit.ckwebview.CKWebView;

public class CKWebViewClient extends WebViewClient {


    private static final String INTENT_PROTOCOL_START = "intent:";
    private static final String INTENT_PROTOCOL_INTENT = "#Intent;";
    //    public static final String INTENT_PROTOCOL_PAKEGE = "package=";
//    public static final String INTENT_PROTOCOL_END = ";end;";
    private static final String GOOGLE_PLAY_STORE_PREFIX = "market://details?id=";

    private Activity mActivity;
    public CKWebViewClient(Activity aActivity){
        mActivity = aActivity;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        if(url.equals("https://rsmpay.kcp.co.kr/pay/mobileGW.kcp")){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {

        if(view.getUrl() != null) {
            ((CKWebView) view).addHttpHeader("referer", view.getUrl());
        }

        if(view.getUrl().equals("https://rsmpay.kcp.co.kr/pay/mobileGW.kcp")){
            view.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        }else {
            view.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        }

        if(url.startsWith("tel:")){
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            mActivity.startActivity(intent);
            return true;
        }

        if(url.startsWith("sms:")){
            Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
            mActivity.startActivity(i);
            return true;
        }

        if (url.startsWith("mailto:")){
            Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
            mActivity.startActivity(i);
            return true;
        }

        if(url.startsWith(GOOGLE_PLAY_STORE_PREFIX)){


            if(mActivity != null) {
                mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            }

        }else if (url.startsWith(INTENT_PROTOCOL_START)) {
            int customUrlEndIndex = url.indexOf(INTENT_PROTOCOL_INTENT);

            if (customUrlEndIndex < 0) {

                return false;

            } else {

                try {
                    Intent p = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    if(mActivity != null) {
                        mActivity.startActivity(p);
                    }
                    return true;

                } catch (ActivityNotFoundException e) {

                    int packageNameStart = url.indexOf("package");
                    int packageNameEnd   = url.substring(packageNameStart, url.length()).indexOf(";");
                    String packageName = url.substring(packageNameStart+8, packageNameStart+packageNameEnd);

                    String[] queryString = url.split("\\?");
                    if(queryString.length>1){
                        String[] params = queryString[1].split(";");
                        for (String param : params) {
                            String name = param.split("=")[0];
                            if(name.equals("package")){
                                packageName = param.split("=")[1];
                            }
                        }
                    }

                    if(mActivity != null) {
                        try {
                            mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_PLAY_STORE_PREFIX + packageName)));
                            return true;
                        }catch (Exception e2){
                            e2.printStackTrace();
                        }

                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                return true;
            }
        } else if(!url.startsWith("http")){

            if (mActivity != null) {
                try {
                    mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }catch (ActivityNotFoundException e){
                    e.printStackTrace();
                }

                return true;
            }

        }

        return false;

    }


}