package harsh.demoglynk;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.parse.Parse;

public class Facebook extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("6md5fSIFNrcpXc0ZStnJos7SW4Rnrok0Hyu7Uoes")
                .clientKey("JkJobVzYq3d6qHSZlVD6I1Whgo0rirOS8lEcZyba")
                .server("https://parseapi.back4app.com/")
                .enableLocalDataStore()
                .build());
    }
}
