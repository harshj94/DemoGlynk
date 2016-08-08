package harsh.demoglynk;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class OtherActivity extends AppCompatActivity {
    AccessToken accessToken;
    String user_id;
    ArrayList<Item> itemArrayList;
    ListView listView;
    DatabaseHandler db;
    ItemsAdapter adapter;
    String accessToke;
    SharedPreferences sharedPreferences;
    LoginButton loginButton;
    AccessTokenTracker accessTokenTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        db = new DatabaseHandler(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        listView = (ListView) findViewById(R.id.list);
        itemArrayList = new ArrayList<>();
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        final ActionBar actionBar = getSupportActionBar();
        View viewActionBar = getLayoutInflater().inflate(R.layout.actionbar_titletext_layout, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        TextView textView = (TextView) viewActionBar.findViewById(R.id.actionbar_textview);
        textView.setText(R.string.follow);
        if (actionBar != null) {
            actionBar.setCustomView(viewActionBar, params);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        sharedPreferences = getApplicationContext().getSharedPreferences("GlynkPreferences", MODE_PRIVATE);
        accessToke = sharedPreferences.getString("AccessToken", "empty");
        accessToken = AccessToken.getCurrentAccessToken();
        itemArrayList = db.getAllItems();
        if (accessToke.equals(accessToken.getUserId())) {
            if (itemArrayList.size() == 0) {
                function();
            } else {
                adapter = new ItemsAdapter(getApplicationContext(), itemArrayList);
                listView.setAdapter(adapter);
            }
        } else {
            itemArrayList.clear();
            function();
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("AccessToken", accessToken.getUserId());
            editor.apply();
        }
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    finish();
                }
            }
        };

        accessTokenTracker.startTracking();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            loginButton.performClick();
        }
        return super.onOptionsItemSelected(item);
    }

    void function() {
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    user_id = object.getString("id");
                    new GetLikes().execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private class GetLikes extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            LoginManager.getInstance().logInWithReadPermissions(OtherActivity.this, Collections.singletonList("user_likes"));
            db.deleteTable();
            db.close();
            db = new DatabaseHandler(OtherActivity.this);
            final String[] afterString = {""};
            final Boolean[] noData = {false};
            do {
                Bundle params = new Bundle();
                params.putString("after", afterString[0]);
                new GraphRequest(accessToken, user_id + "/likes", params, HttpMethod.GET, new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse graphResponse) {
                        JSONObject jsonObject = graphResponse.getJSONObject();
                        try {
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                String name = jsonObject1.optString("name");
                                String id = jsonObject1.optString("id");
                                Item item = new Item();
                                item.settTitle(name);
                                item.settObjectId(id);
                                item.settURL("https://graph.facebook.com/" + id + "/picture?width=100");
                                itemArrayList.add(item);
                                db.addContact(item, i);
                            }
                            if (!jsonObject.isNull("paging")) {
                                JSONObject paging = jsonObject.getJSONObject("paging");
                                JSONObject cursors = paging.getJSONObject("cursors");
                                if (!cursors.isNull("after")) {
                                    afterString[0] = cursors.getString("after");
                                } else {
                                    noData[0] = true;
                                }
                            } else {
                                noData[0] = true;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).executeAndWait();
            } while (!noData[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter = new ItemsAdapter(getApplicationContext(), itemArrayList);
            listView.setAdapter(adapter);
        }
    }
}
