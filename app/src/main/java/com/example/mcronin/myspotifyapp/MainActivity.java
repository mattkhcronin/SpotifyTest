package com.example.mcronin.myspotifyapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity implements
        PlayerNotificationCallback, ConnectionStateCallback {

    private static final int REQUEST_CODE = 1337;
    private String _authToken = "";

    private Player mPlayer;
    private Boolean isPlaying = false;
    private Track currentTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(Global.CLIENT_ID, AuthenticationResponse.Type.TOKEN, Global.REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                _authToken = response.getAccessToken();
                Config playerConfig = new Config(this, response.getAccessToken(), Global.CLIENT_ID);

                Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                    @Override
                    public void onInitialized(Player player) {
                        mPlayer = player;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addPlayerNotificationCallback(MainActivity.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d("MainActivity", "Playback event received: " + eventType.name());
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String errorDetails) {
        Log.d("MainActivity", "Playback error received: " + errorType.name());
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    public void pauseResumeSong(View view){
        if (isPlaying){
            mPlayer.pause();
            isPlaying = false;
        } else {
            mPlayer.resume();
            isPlaying = true;
        }
    }

    public void playSong(View view){
        if (currentTrack != null) {
            mPlayer.play("spotify:track:" + currentTrack.getId());
        }
    }

    public void searchForTrack(View view) {
        CallAPI api = new CallAPI(Global.WEB_BASE_URL, _authToken, new CallAPI.PostExecutable() {
            @Override
            public void Execute(String results) {
                setCurrentTrack(results);
            }
        });
        EditText searchText = (EditText) findViewById(R.id.searchTextBox);
        String search = searchText.getText().toString();
        search = search.replace(" ", "+");
        api.execute(Global.SEARCH_TRACK_URL + search);
    }

    private void setCurrentTrack(String results){
        try {
            JSONObject jsonObject = new JSONObject(results);

            //for (int i=0; i<jsonArray.length(); i++){
            JSONObject jsonTrackObject = jsonObject.getJSONObject("tracks");
            JSONArray jsonItemArray = jsonTrackObject.getJSONArray("items");
            JSONObject jsonItemObject = jsonItemArray.getJSONObject(0);
            JSONArray jsonArtistArray = jsonItemObject.getJSONArray("artists");
            JSONObject jsonArtistObject = jsonArtistArray.getJSONObject(0);
            currentTrack = new Track(jsonItemObject.getString("id"), jsonItemObject.getString("name"), jsonArtistObject.getString("name"));
            //}
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
