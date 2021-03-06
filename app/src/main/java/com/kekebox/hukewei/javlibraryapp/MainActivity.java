package com.kekebox.hukewei.javlibraryapp;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.gc.materialdesign.views.CheckBox;
import com.gc.materialdesign.widgets.Dialog;
import com.kekebox.hukewei.javlibraryapp.jav.JavLibApplication;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.umeng.update.UmengUpdateAgent;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.Iterator;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {



    public static final int CONNECTION_TIMEOUT = 10000;
    public static final boolean ENABLE_THUMBS = true;
    private static final String TAG = "MainActivity";
    private Menu menu;
    String videoId;
    Dialog dialog;


    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private boolean doubleBackToExitPressedOnce =false;
    private UserLoginBackgroundTask mAuthTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI(findViewById(R.id.drawer_layout));

        Intent intent = getIntent();
        handleIntent(intent);


        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        dialog = new Dialog(this,getString(R.string.warning_title), getString(R.string.warning_text));
        // Set accept click listenner
        dialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        // Set cancel click listenner
        dialog.setOnCancelButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // Acces to accept button

        //dialog.show();
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String email = sharedPref.getString(getString(R.string.saved_email), null);
        String pw = sharedPref.getString(getString(R.string.saved_pw), null);
        if( !JavUser.getCurrentUser().isLogin() && email != null && pw != null) {
            mAuthTask = new UserLoginBackgroundTask(email, pw);
            mAuthTask.execute((Void) null);
        }

        UmengUpdateAgent.update(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    public void handleIntent(Intent intent) {
        videoId = intent.getStringExtra("VideoID");
        if(videoId != null) {
                Log.d(TAG, "video id = "+ videoId);
                Bundle myBundle = new Bundle();
                myBundle.putString("VideoID", videoId);
                Intent detail_intent = new Intent(this, VideoDetailActivity.class);
                detail_intent.putExtras(myBundle);
                startActivity(detail_intent);
        }
    }



    public void setupUI(View view) {

        //Set up touch listener for non-title_text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    hideKeyboard();
                    return false;
                }

            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUI(innerView);
            }
        }
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = getCurrentFocus();
        if ( view != null) {
            InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }




    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment next_fragment = null;
        if (position == 0) {
            next_fragment = new CategoryFragment();

        } else if(position == 2){
            if(JavUser.getCurrentUser().isLogin()) {
                updateMenuTitles();
                next_fragment = new UserInfoFragment();
            } else {
                next_fragment = new LoginFragment();
            }
        } else if (position == 3){

            next_fragment = new AboutFragment();
        } else {
            next_fragment = PlaceholderFragment.newInstance(position + 1);
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, next_fragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 0:
                mTitle = getString(R.string.title_section1);
                break;
            case 1:
                mTitle = getString(R.string.title_section2);
                break;
            case 2:
                mTitle = getString(R.string.title_section3);
                break;
            case 3:
                mTitle = getString(R.string.title_section4);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        //actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            this.menu = menu;
            updateMenuTitles();
            // Associate searchable configuration with the SearchView
            SearchManager searchManager =
                    (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView =
                    (SearchView) menu.findItem(R.id.search).getActionView();
            searchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getComponentName()));
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.goto_web) {
            Uri uri = Uri.parse("http://www.javlibrary.com/");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_signInUp) {
            if(JavUser.getCurrentUser().isLogin()) {
                JavUser.getCurrentUser().Logout(this);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new LoginFragment())
                    .commit();
        }
        updateMenuTitles();

        return super.onOptionsItemSelected(item);
    }

    public void updateMenuTitles() {
        MenuItem bedMenuItem = menu.findItem(R.id.action_signInUp);
        if(bedMenuItem == null) {
            return;
        }
        if (JavUser.getCurrentUser().isLogin()) {
            bedMenuItem.setTitle("注销");
        } else {
            bedMenuItem.setTitle("登陆");
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            //setHasOptionsMenu(true);
            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//            inflater.inflate(R.menu.main, menu);
//            MenuItem searchMenuItem = menu.findItem(R.id.search);
//            SearchView searchView = (SearchView) searchMenuItem.getActionView();
//            searchView.setIconified(false);
            //searchMenuItem.expandActionView();
            super.onCreateOptionsMenu(menu, inflater);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.double_click_exit), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 1200);
    }


    public class UserLoginBackgroundTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        JavUser mCurrentUser;

        UserLoginBackgroundTask(String email, String password) {
            mEmail = email;
            mPassword = password;
            mCurrentUser = JavUser.getCurrentUser();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpClient client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(), MainActivity.CONNECTION_TIMEOUT); //Timeout Limit
            HttpResponse response;
            JSONObject json = new JSONObject();

            try {
                HttpPost post = new HttpPost(getString(R.string.authentication_url));
                json.put("email", mEmail);
                json.put("password", mPassword);
                StringEntity se = new StringEntity( json.toString());
                Log.i(TAG, json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                post.setEntity(se);
                response = client.execute(post);

                /*Checking response */
                if(response!=null && response.getStatusLine().getStatusCode() == 200){
                    String json_string = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObj = new JSONObject(json_string);
                    Iterator<String> keys= jsonObj.keys();
                    while (keys.hasNext())
                    {


                        String keyValue = (String)keys.next();
                        String valueString = jsonObj.getString(keyValue);
                        Log.i(TAG, keyValue);
                        Log.i(TAG, valueString);
                        if(keyValue.contains("id")) {
                            if(valueString.equals("ERROR")) {
                                //login failed
                                Log.d(TAG, "Login fail");
                                mCurrentUser.setLogin(false);
                                return false;
                            } else {
                                mCurrentUser.setLogin(true);
                                mCurrentUser.setUserId(valueString);
                                Log.d(TAG, "Login success");
                                return true;
                            }
                        }
                    }
                    Log.i(TAG, json_string);
                } else {
                    mCurrentUser.setLogin(false);
                    return false;
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            if (success) {

                updateMenuTitles();
                Toast.makeText(MainActivity.this, "自动登陆成功", Toast.LENGTH_SHORT).show();
                Fragment next_fragment = new UserInfoFragment();
            } else {
                Toast.makeText(MainActivity.this, "自动登陆失败", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }


}
