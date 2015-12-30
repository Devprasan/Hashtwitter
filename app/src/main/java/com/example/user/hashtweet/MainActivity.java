package com.example.user.hashtweet;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String PREF_NAME = "sample_twitter_pref";
    private static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    private static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    private static final String PREF_KEY_TWITTER_LOGIN = "is_twitter_loggedin";
    private static final String PREF_USER_NAME = "twitter_user_name";
    public static final int WEBVIEW_REQUEST_CODE = 100;
    private ProgressDialog pDialog;
    private static Twitter twitter;
    private static RequestToken requestToken;
    private static SharedPreferences mSharedPreferences;
    private EditText mShareEditText;
    private TextView userName;
    private View loginLayout;
    private View shareLayout;
    private String consumerKey = null;
    private String consumerSecret = null;
    private String callbackUrl = null;
    private String oAuthVerifier = null;
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTwitterConfigs();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);
        loginLayout = (RelativeLayout) findViewById(R.id.login_layout);
        shareLayout = (LinearLayout) findViewById(R.id.share_layout);
        mShareEditText = (EditText) findViewById(R.id.share_text);
        userName = (TextView) findViewById(R.id.user_name);
        findViewById(R.id.btn_login).setOnClickListener(this);
        findViewById(R.id.btn_share).setOnClickListener(this);
        if (TextUtils.isEmpty(consumerKey) || TextUtils.isEmpty(consumerSecret)) {
            Toast.makeText(this, "Twitter key and secret not configured",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mSharedPreferences = getSharedPreferences(PREF_NAME, 0);

        boolean isLoggedIn = mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
        if (isLoggedIn) {
            loginLayout.setVisibility(View.GONE);
            shareLayout.setVisibility(View.VISIBLE);
            String username = mSharedPreferences.getString(PREF_USER_NAME, "");
            userName.setText(getResources ().getString(R.string.hello)
                    + username);

        } else {
            loginLayout.setVisibility(View.VISIBLE);
            shareLayout.setVisibility(View.GONE);
            Uri uri = getIntent().getData();
            if (uri != null && uri.toString().startsWith(callbackUrl)) {
                String verifier = uri.getQueryParameter(oAuthVerifier);
                try {
                    AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
                    long userID = accessToken.getUserId();
                    final User user = twitter.showUser(userID);
                    final String username = user.getName();
                    saveTwitterInfo(accessToken);
                    loginLayout.setVisibility(View.GONE);
                    shareLayout.setVisibility(View.VISIBLE);
                    userName.setText(getString(R.string.hello) + username);

                } catch (Exception e) {
                    Log.e("Failed to login Twitter!!", e.getMessage());
                }
            }

        }
    }
    private void saveTwitterInfo(AccessToken accessToken) {
        long userID = accessToken.getUserId();
        User user;
        try {
            user = twitter.showUser(userID);
            String username = user.getName();
            SharedPreferences.Editor e = mSharedPreferences.edit();
            e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
            e.putString(PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
            e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
            e.putString(PREF_USER_NAME, username);
            e.commit();
        } catch (TwitterException e1) {
            e1.printStackTrace();
        }
    }
    private void initTwitterConfigs() {
        consumerKey = getString(R.string.twitter_consumer_key);
        consumerSecret = getString(R.string.twitter_consumer_secret);
        callbackUrl = getString(R.string.twitter_callback);
        oAuthVerifier = getString(R.string.twitter_oauth_verifier);
    }
    private void loginToTwitter() {
        boolean isLoggedIn = mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
        if (!isLoggedIn) {
            final ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(consumerKey);
            builder.setOAuthConsumerSecret(consumerSecret);
            final Configuration configuration = builder.build();
            final TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();
            try {
                requestToken = twitter.getOAuthRequestToken(callbackUrl);
                final Intent intent = new Intent(this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.EXTRA_URL, requestToken.getAuthenticationURL());
                startActivityForResult(intent, WEBVIEW_REQUEST_CODE);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        } else {

            loginLayout.setVisibility(View.GONE);
            shareLayout.setVisibility(View.VISIBLE);

            }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String verifier = data.getExtras().getString(oAuthVerifier);
            try {
                AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
                long userID = accessToken.getUserId();
                final User user = twitter.showUser(userID);
                String username = user.getName();
                saveTwitterInfo(accessToken);
                loginLayout.setVisibility(View.GONE);
                shareLayout.setVisibility(View.VISIBLE);
                userName.setText(MainActivity.this.getResources().getString(
                        R.string.hello) + username);
            } catch (Exception e) {
                Log.e("Twitter Login Failed", e.getMessage());
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                loginToTwitter();
                break;
            case R.id.btn_share:
                final String search = mShareEditText.getText().toString();
                    if(search!=null) {

                        new updateTwitterStatus().execute(search);
                    }

                break;
        }
    }

    class updateTwitterStatus extends AsyncTask<String, String, List<Tweet_model>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Retriving...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected List<Tweet_model> doInBackground(String... args) {

            String search;
            if (args[0].startsWith("#")){
                search=args[0];
            }else {
                search="#"+args[0];
            }

            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(consumerKey);
            builder.setOAuthConsumerSecret(consumerSecret);
            String access_token = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
            String access_token_secret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");
            AccessToken accessToken = new AccessToken(access_token, access_token_secret);
            Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
            try {
                Query query = new Query(search);
                query.setCount(20);

                QueryResult result;
                List<String> stringtweets=new ArrayList<String>();

                    result = twitter.search(query);
                    List<twitter4j.Status> tweets = result.getTweets();
                    List<Tweet_model> tweet_models=new ArrayList<>();
                    for (twitter4j.Status tweet : tweets) {
                        Tweet_model tweet_model=new Tweet_model();
                        tweet_model.setUsername(tweet.getUser().getScreenName());
                        tweet_model.setTweets(tweet.getText());

                        // System.out.println("@" + tweet.getUser().getScreenName() + " - " + tweet.getText());
                        Log.e("TweetSearch", tweet.getUser().getScreenName() + " - " + tweet.getText());
                        tweet.getUser();
                        tweet_models.add(tweet_model);
                        stringtweets.add(tweet.getText());

                    }
                return tweet_models;

            } catch (TwitterException te) {
                te.printStackTrace();
                // System.out.println("Failed to search tweets: " + te.getMessage());
                Log.e("TweetSearch", te.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Tweet_model> result) {
            pDialog.dismiss();
            mShareEditText.setText("");
            ListView listView= (ListView) findViewById(R.id.listview);
           // ArrayAdapter arrayAdapter=new ArrayAdapter(MainActivity.this,android.R.layout.simple_list_item_1,result);
            searchAdapter arrayAdapter=new searchAdapter(MainActivity.this,R.layout.listrow,result);
            listView.setAdapter(arrayAdapter);
        }


    }}
