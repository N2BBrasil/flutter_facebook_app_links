package it.remedia.flutter_facebook_app_links;

import android.content.Context;
import android.os.Handler;
//import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.applinks.AppLinkData;
import com.facebook.FacebookSdk;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** FlutterFacebookAppLinksPlugin */
public class FlutterFacebookAppLinksPlugin implements FlutterPlugin {
  private Context mContext;
  private MethodChannel mChannel;

  private static final String CHANNEL = "plugins.remedia.it/flutter_facebook_app_links";

  private void initFBLinks(Result result) {
    //Log.d("FB_APP_LINKS", "Facebook App Links initialized");

    final Map<String, String> data = new HashMap<>();
    final Result resultDelegate = result;
    // Get a handler that can be used to post to the main thread
    final Handler mainHandler = new Handler(mContext.getMainLooper());

    // Get user consent
    FacebookSdk.setAutoInitEnabled(true);
    FacebookSdk.fullyInitialize();
    AppLinkData.fetchDeferredAppLinkData(mContext,
      new AppLinkData.CompletionHandler() {
        @Override
        public void onDeferredAppLinkDataFetched(AppLinkData appLinkData) {
          // Process app link data
          if(appLinkData!=null) {

            if(appLinkData.getTargetUri()!=null){
              //Log.d("FB_APP_LINKS", "Deferred Deeplink Received: " + appLinkData.getTargetUri().toString());
              data.put("deeplink", appLinkData.getTargetUri().toString());
            }

            //Log.d("FB_APP_LINKS", "Deferred Deeplink Received: " + appLinkData.getPromotionCode());
            if(appLinkData.getPromotionCode()!=null)
              data.put("promotionalCode", appLinkData.getPromotionCode());
            else
              data.put("promotionalCode", "");

            Runnable myRunnable = new Runnable() {
              @Override
              public void run() {
                if(resultDelegate!=null)
                  resultDelegate.success(data);
              }
            };

            mainHandler.post(myRunnable);

          }else{
            //Log.d("FB_APP_LINKS", "Deferred Deeplink Received: null link");

            Runnable myRunnable = new Runnable() {
              @Override
              public void run() {
                if(resultDelegate!=null)
                  resultDelegate.success(null);
              }
            };

            mainHandler.post(myRunnable);

          }

        }
      }
    );
  }


  @Override
  public void onAttachedToEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
    mContext = binding.getApplicationContext();
    mChannel = new MethodChannel(binding.getBinaryMessenger(), CHANNEL);
    mChannel.setMethodCallHandler(new MethodCallHandler() {
      @Override
      public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("getPlatformVersion")) {
          result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if(call.method.equals("initFBLinks")){

          initFBLinks(result);
        } else {
          result.notImplemented();
        }
      }
    });
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
    mChannel.setMethodCallHandler(null);
  }
}
