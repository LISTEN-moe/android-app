package jcotter.listenmoe;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Menu extends AppCompatActivity {

    // [GLOBAL VARIABLES] //
    // UI VARIABLES //
    LinearLayout root;
    TabHost tabHost;
    //Request Tab //
    TextView req_loginRequired;
    TextView req_searchText;
    EditText req_search;
    Button req_searchButton;
    ListView req_list;
    TextView req_remaining;
    // Favorites Tab //
    TextView fav_loginRequired;
    ListView fav_list;
    // Login Tab //
    EditText username;
    EditText password;
    Button login;
    Button logout;
    TextView status;
    ImageButton github;
    // NON-UI GLOBAL VARIABLES //
    List<Integer> songIds, favorite;
    List<Boolean> enabled;
    ArrayAdapter<String> adapter;
    // [METHODS] //
    // SYSTEM METHODS //
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        root = (LinearLayout) findViewById(R.id.root);
        tabHost = (TabHost) findViewById(R.id.tabAPI);
        req_loginRequired = (TextView) findViewById(R.id.req_loginRequired);
        req_searchText = (TextView) findViewById(R.id.req_searchText);
        req_search = (EditText) findViewById(R.id.req_search);
        req_searchButton = (Button) findViewById(R.id.req_searchButton);
        req_list = (ListView) findViewById(R.id.req_list);
        req_remaining = (TextView) findViewById(R.id.req_remaining);
        fav_loginRequired = (TextView) findViewById(R.id.fav_loginRequired);
        fav_list = (ListView) findViewById(R.id.fav_list);

        username = (EditText) findViewById(R.id.login_username);
        password = (EditText) findViewById(R.id.login_password);
        login = (Button) findViewById(R.id.login_button);
        logout = (Button) findViewById(R.id.login_logout);
        status = (TextView) findViewById(R.id.loginStatus);
        github = (ImageButton) findViewById(R.id.github);

        // SETUP METHODS //
        tabHostSetup();
        tabChangeListener();
        uiClickListeners();
        onWindowFocusChanged(true);
    }
    @Override public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
            if(imm.isActive() && getWindow().getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        // Removes cursor from the edit text fields
        req_search.clearFocus();
        username.clearFocus();
        password.clearFocus();
    }
    // UI METHODS //
    private void tabHostSetup(){
        // PURPOSE: SETS UP THE TAB HOST //
        tabHost.setup();
        // Tab 1 //
        TabHost.TabSpec spec = tabHost.newTabSpec(getString(R.string.tabReq));
        spec.setContent(R.id.Requests);
        spec.setIndicator(getString(R.string.tabReq));
        tabHost.addTab(spec);
        // Tab 2 //
        spec = tabHost.newTabSpec(getString(R.string.tabFav));
        spec.setContent(R.id.Favorites);
        spec.setIndicator(getString(R.string.tabFav));
        tabHost.addTab(spec);
        // Tab 3 //
        spec = tabHost.newTabSpec(getString(R.string.tabLogin));
        spec.setContent(R.id.Login);
        spec.setIndicator(getString(R.string.tabLogin));
        tabHost.addTab(spec);
        // Opens Tab specified in intent | Defaults to Request Tab //
        tabHost.setCurrentTab(this.getIntent().getIntExtra("index", 0));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(tabHost.getCurrentTab() == 0)
            requestTab(sharedPreferences.getString("userToken", "NULL"));
    }
    private void tabChangeListener(){
        // PURPOSE: LISTENER FOR TAB HOST SELECTION //
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
                // Reset Global Variables //
                adapter = null;
                enabled = null;
                favorite = null;
                songIds = null;
                // Reset UI Components //
                req_search.setText("");
                username.setText("");
                password.setText("");
                if(req_list != null) req_list.setAdapter(null);
                if(fav_list != null) fav_list.setAdapter(null);
                // Changes Tab content if a valid token is available //
                // Not required for Login Tab as same UI Components always shown //
                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                int currentTab = tabHost.getCurrentTab();
                if(currentTab == 0)
                    requestTab(sharedPreferences.getString("userToken", "NULL"));
                else if (currentTab == 1)
                    favoriteTab(sharedPreferences.getString("userToken", "NULL"));
                else if (currentTab == 2){
                    if(!sharedPreferences.getString("userToken", "NULL").equals("NULL"))
                    status.setVisibility(View.VISIBLE);
                    if(sharedPreferences.getLong("lastAuth", 0) != 0)
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getBaseContext(), "Current Token Age: " + (Math.round((System.currentTimeMillis() / 1000 - sharedPreferences.getLong("lastAuth", 0)) / 86400.0)) + " Days", Toast.LENGTH_LONG).show();
                            }
                        });
                }
            }
        });
    }
    private void requestTab(String userToken){
        // PURPOSE: CONTROLS CONTENT DISPLAYED IN REQUEST TAB //
        if (userToken.equals("NULL")){
            req_loginRequired.setVisibility(View.VISIBLE);
            req_searchText.setVisibility(View.GONE);
            req_search.setVisibility(View.GONE);
            req_searchButton.setVisibility(View.GONE);
            req_list.setVisibility(View.GONE);
            req_remaining.setVisibility(View.GONE);
            return;
        }
        req_loginRequired.setVisibility(View.GONE);
        req_searchText.setVisibility(View.VISIBLE);
        req_searchButton.setVisibility(View.VISIBLE);
        req_search.setVisibility(View.VISIBLE);
    }
    private void favoriteTab(String userToken){
        // PURPOSE: CONTROLS CONTENT DISPLAYED IN FAVORITE TAB //
        if (userToken.equals("NULL")){
            fav_loginRequired.setVisibility(View.VISIBLE);
            fav_list.setVisibility(View.GONE);
            return;
        }
        fav_loginRequired.setVisibility(View.GONE);
        fav_list.setVisibility(View.VISIBLE);
        // Retrieves Favorites //
        APIActions apiActions = new APIActions(new APIActions.APIListener() {
            @Override
            public void favoriteListCallback(String jsonResult) {
                listViewDisplay(jsonResult, 1);
            }
            @Override public void authenticateCallback(String token) {}
            @Override public void requestCallback(String jsonResult) {}
            @Override public void favoriteCallback(String jsonResult) {}
            @Override public void searchCallback(String jsonResult) {}
        });
        apiActions.favoriteList(getApplicationContext());
    }
    private void listViewDisplay(final String displayData, final int tab){
        // PURPOSE: PROCESSES AND DISPLAYS THE RELEVANT LIST VIEW DATA //
        final int currentTab = tabHost.getCurrentTab();
        List<String> displayList = new ArrayList<>();
        try {
            // Get songs object from JSON data //
            JSONObject json = new JSONObject(displayData);
            JSONArray songsObject = json.getJSONArray("songs");
            // Nullify Lists & Re-init them //
            songIds = null;
            enabled = null;
            favorite = null;
            adapter = null;
            songIds = new ArrayList<>();
            favorite = new ArrayList<>();
            enabled = new ArrayList<>();
            // Loop through each song setting whether it is a favorite, enabled, both or neither & Sets song string format //
            JSONObject song;
            for(int i = 0; i < songsObject.length(); i++){
                song =  songsObject.getJSONObject(i);
                if(!song.getString("anime").equals(""))
                    displayList.add(song.getString("artist") + " - " + song.getString("title") + " [" + song.getString("anime") + "]");
                else
                    displayList.add(song.getString("artist") + " - " + song.getString("title"));
                songIds.add(i, song.getInt("id"));
                if(!song.has("enabled"))
                    enabled.add(i, true);
                else
                    enabled.add(i, false);
                if(currentTab == 0){
                    if(song.has("favorite"))
                        favorite.add(i, song.getInt("favorite"));
                    else
                        favorite.add(i, 0);
                } else
                    favorite.add(i,1);
            }
        }catch (JSONException ex){ex.printStackTrace();}
        if(displayList.size() != 0){
            // Creates a new Adapter using displayList //
            adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, displayList){
                @SuppressWarnings("deprecation")
                @NonNull
                @Override
                public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    TextView text = (TextView) super.getView(position, convertView, parent);
                    // Reset view so that it does not keep properties of a recycled view //
                    text.setBackgroundColor(0);
                    text.setTextColor(Color.WHITE);
                    // Sets Text Grey if song is disabled //
                    if(!enabled.get(position)){
                        if(Build.VERSION.SDK_INT >= 23){
                            text.setTextColor(getColor(R.color.greyText));
                        } else {
                            text.setTextColor(getResources().getColor(R.color.greyText));
                        }
                    }
                    // If current tab is Request Tab sets Pink text for favorites //
                    if(currentTab == 0){
                        if(favorite.get(position) == 1){
                            if(Build.VERSION.SDK_INT >= 23){
                                text.setBackgroundColor(getColor(R.color.flavoredText));
                            } else {
                                text.setBackgroundColor(getResources().getColor(R.color.flavoredText));
                            }
                        }
                    }
                    return text;
                }
            };
        }else {
            // Sets Adapter to empty to display nothing //
            if (adapter != null)
                adapter = null;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(adapter != null) {
                    if(tab == 0){
                        req_list.setAdapter(adapter);
                        req_list.setVisibility(View.VISIBLE);
                    } else if(tab == 1){
                        fav_list.setAdapter(adapter);
                        fav_list.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }
    private void uiClickListeners(){
        // PURPOSE: LISTENERS FOR ALL CLICKABLE UI COMPONENTS //
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWindowFocusChanged(true);
            }
        });
        req_searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWindowFocusChanged(true);
                if(req_search.getText().toString().trim().length() == 0) return;
                search();
            }
        });
        req_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                confirmationDialog(i);
            }
        });
        fav_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                confirmationDialog(i);
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWindowFocusChanged(true);
                if(username.getText().length() == 0)
                    Toast.makeText(getBaseContext(), getString(R.string.errorName), Toast.LENGTH_LONG).show();
                else if (password.getText().length() == 0)
                    Toast.makeText(getBaseContext(), getString(R.string.errorPass), Toast.LENGTH_LONG).show();
                login();
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWindowFocusChanged(true);
                logout();
            }
        });
        github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri link = Uri.parse(getString(R.string.github));
                Intent intent = new Intent(Intent.ACTION_VIEW, link);
                startActivity(intent);
            }
        });
    }
    private void confirmationDialog(final int songIndex){
        // PURPOSE: DISPLAYS A DIALOG CONTAINING SONG ACTIONS //
        AlertDialog.Builder builder = new AlertDialog.Builder(Menu.this);
        // Cancel button //
        builder.setMessage(R.string.req_dialog_message);
        builder.setPositiveButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        if(favorite.get(songIndex) == 1){
            // Create button "Unfavorite"
            builder.setNegativeButton("Un" + getString(R.string.tabFav).substring(0, getString(R.string.tabFav).length() - 1), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int in) {
                    favorite(songIndex);
                }
            });
        } else {
            // Create button "Favorite" //
            builder.setNegativeButton(getString(R.string.tabFav).substring(0 ,getString(R.string.tabFav).length() - 1), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int in) {
                    favorite(songIndex);
                }
            });
        }
        if(enabled.get(songIndex)){
            // Create button Request //
            builder.setNeutralButton(getString(R.string.tabReq).substring(0, getString(R.string.tabReq).length() - 1), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int im) {
                   request(songIndex);
                }
            });
        }
        builder.create().show();
    }
    // LOGIC METHODS //
    private void favorite(final int songIndex){
        // PURPOSE: CHANGES THE FAVORITE STATUS OF A SONG //
        final int songID = songIds.get(songIndex);
        APIActions apiActions = new APIActions(new APIActions.APIListener() {
            @Override public void favoriteCallback(final String jsonResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(jsonResult.contains("success\":true")){
                            Toast.makeText(getBaseContext(), R.string.success, Toast.LENGTH_SHORT).show();
                            if(jsonResult.contains("favorite\":true"))
                                favorite.set(songIndex, 1);
                            else {
                                favorite.set(songIndex, 0);
                                // Removes Song from Favorite List //
                                if(tabHost.getCurrentTab() == 1){
                                    songIds.remove(songIndex);
                                    favorite.remove(songIndex);
                                    enabled.remove(songIndex);
                                    adapter.remove(adapter.getItem(songIndex));
                                }
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getBaseContext(), R.string.req_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override public void searchCallback(String jsonResult) {}
            @Override public void favoriteListCallback(String jsonResult) {}
            @Override public void authenticateCallback(String token) {}
            @Override public void requestCallback(String jsonResult) {}
        });
        apiActions.favorite(songID, getApplicationContext());
    }
    private void request(final int songIndex){
        // PURPOSE: REQUESTS A SONG //
        final int songID = songIds.get(songIndex);
        APIActions apiActions = new APIActions(new APIActions.APIListener() {
            @Override public void requestCallback(final String jsonResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(jsonResult.contains("success\":true")){
                            Toast.makeText(getBaseContext(), R.string.success, Toast.LENGTH_LONG).show();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    enabled.set(songIndex, false);
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        } else {
                            if(jsonResult.contains("user-is-not-supporter")){
                                Toast.makeText(getBaseContext(), R.string.supporter, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getBaseContext(), R.string.req_error, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }
            @Override public void favoriteListCallback(String jsonResult) {}
            @Override public void authenticateCallback(String token) {}
            @Override public void favoriteCallback(String jsonResult) {}
            @Override public void searchCallback(String jsonResult) {}
        });
        apiActions.request(songID, getApplicationContext());
    }
    private void search(){
        APIActions apiActions = new APIActions(new APIActions.APIListener() {
            @Override public void searchCallback(final String jsonResult) {
                listViewDisplay(jsonResult, 0);
                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            JSONObject jsonObject = new JSONObject(jsonResult);
                            JSONObject extras = jsonObject.getJSONObject("extra");
                            String search = getString(R.string.search) + "     [" + extras.get("requests").toString() + " Requests Remaining]";
                            req_searchText.setText(search);
                        }catch(JSONException ex){ex.printStackTrace();}
                    }
                });*/
            }
            @Override public void favoriteListCallback(String jsonResult) {}
            @Override public void authenticateCallback(String token) {}
            @Override public void requestCallback(String jsonResult) {}
            @Override public void favoriteCallback(String jsonResult) {}
        });
        apiActions.search(req_search.getText().toString().trim(), getApplicationContext());
    }
    private void login(){
        APIActions apiActions = new APIActions(new APIActions.APIListener() {
            @Override public void authenticateCallback(final String token) {
                runOnUiThread(new Runnable() {
                    @SuppressLint("CommitPrefEdits")
                    @Override
                    public void run() {
                        if(token.contains(getString(R.string.invalid_user))){
                            Toast.makeText(getBaseContext(), getString(R.string.errorName), Toast.LENGTH_LONG).show();
                            return;
                        } else if (token.contains(getString(R.string.invalid_pass))){
                            Toast.makeText(getBaseContext(), getString(R.string.errorPass), Toast.LENGTH_LONG).show();
                            return;
                        } else if (token.contains("error-general")){

                            Toast.makeText(getBaseContext(), getString(R.string.errorGeneral), Toast.LENGTH_LONG).show();
                            return;
                        }
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = sharedPreferences.edit()
                                .putString("userToken", token)
                                .putLong("lastAuth", System.currentTimeMillis() / 1000);
                        editor.commit();
                        status.setVisibility(View.VISIBLE);
                        Toast.makeText(getBaseContext(), getString(R.string.success), Toast.LENGTH_LONG).show();
                    }
                });
            }
            @Override public void favoriteListCallback(String jsonResult) {}
            @Override public void requestCallback(String jsonResult) {}
            @Override public void favoriteCallback(String jsonResult) {}
            @Override public void searchCallback(String jsonResult) {}
        });
        apiActions.authenticate(getApplicationContext(), username.getText().toString().trim(), password.getText().toString().trim());
    }
    @SuppressLint("CommitPrefEdits")
    private void logout(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(!sharedPreferences.getString("userToken", "NULL").equals("NULL")){
            SharedPreferences.Editor editor = sharedPreferences.edit()
                    .putString("userToken", "NULL")
                    .putLong("lastAuth", 0);
            editor.commit();
            username.setText("");
            password.setText("");
            status.setVisibility(View.INVISIBLE);
            Toast.makeText(getBaseContext(), getString(R.string.success), Toast.LENGTH_LONG).show();
        }
    }
}
