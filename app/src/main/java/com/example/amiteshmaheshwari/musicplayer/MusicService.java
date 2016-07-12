package com.example.amiteshmaheshwari.musicplayer;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Binder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

import android.content.Context;
import java.util.Random;
import android.app.Notification;
import android.app.PendingIntent;
import java.util.ArrayList;

public class MusicService extends Service implements
        AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosn;
    private int trackPosn;
    private final IBinder musicBind = new MusicBinder();
    private String songTitle ="";
    private static final int NOTIFY_ID = 1;
    private boolean shuffle = false;
    private Random rand;
    private static final String TAG = "MusicServiceTest";
    private boolean audioFocusGranted = false;
    private boolean autoPaused = false;
    public MusicService() {

    }

    public void onCreate(){
        rand = new Random();
        super.onCreate(); //create the service
        songPosn = 0;

        player = new MediaPlayer();
        initMusicPlayer();


    }

    public void setShuffle(){
        if(shuffle) shuffle = false;
        else shuffle=true;
    }
    public void setList(ArrayList<Song> theSongs){
        songs = theSongs;
    }

    private void requestAudioFocus(){
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.e(TAG, ">>>>>>>>>>>>> FAILED TO REQUEST AUDIO FOCUS <<<<<<<<<<<<<<<<<<<<<<<<");
            audioFocusGranted = false;
        } else {
            audioFocusGranted = true;
        }
    }

    private void abandonAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.abandonAudioFocus(this);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            audioFocusGranted = false;
        } else {
            // FAILED
            audioFocusGranted = true;
            Log.e(TAG, ">>>>>>>>>>>>> FAILED TO ABANDON AUDIO FOCUS <<<<<<<<<<<<<<<<<<<<<<<<");
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        //TODO- there are other cases as well in switch case what about them?

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                //resume playback
                audioFocusGranted = true;
                Log.d(TAG, "Focus gain");
//                if (player == null) {
//                    player = new MediaPlayer();
//                    initMusicPlayer();
//                    if (!autoPaused){playSong();}
//                }
                if (autoPaused && !player.isPlaying()) {
                    player.start();
                    autoPaused = false;
                }
                player.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                audioFocusGranted = false;
                Log.d(TAG, "Focus lost");
                trackPosn = player.getCurrentPosition();
                if (player.isPlaying()) {
                    player.pause();
                    autoPaused = true;
                }

//                player.release();
//                player = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                Log.d(TAG, "Focus transient");
                if (player.isPlaying()) {
                    autoPaused = true;
                    player.pause();}
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                Log.d(TAG, "Focus can duck");
                if (player.isPlaying()) player.setVolume(0.1f, 0.1f);
                break;
            default:

        }
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }
    public void initMusicPlayer(){
        //set player properties
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public void onDestroy(){
        abandonAudioFocus();
        stopForeground(true);
    }

    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    public void setSong(int songIndex) {
        songPosn = songIndex;
        trackPosn = 0;
    }

    public void playSong(){

        player.reset();
        Song playSong = songs.get(songPosn);
        player.seekTo(trackPosn);
        songTitle = playSong.getTitle();
        long currSong = playSong.getId();

        //set uri

        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);
        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch (Exception e) {
            Log.e("Music Service", "Error setting data source", e);
        }
        player.prepareAsync();
    }

    public int getPosn(){
        if(player == null) {
          return trackPosn;
        } else
            return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        if (player != null) {
            return player.isPlaying();
        } else {
            return false;
        }
    }

    public void pausePlayer(){
        trackPosn = getPosn();
        player.pause();
    }

    public void seek(int posn) {
        player.seekTo(posn);
    }

    public void go(){
        if (!audioFocusGranted) {
            requestAudioFocus();
        }
        player.start();
    }

    public void playPrev(){
        songPosn--;
        trackPosn = 0;
        if(songPosn < 0 ) {
            songPosn = songs.size() - 1;
        }
        playSong();
    }

    public void playNext(){
        trackPosn = 0;
        if(shuffle) {
            int newSong = songPosn;
            while (newSong == songPosn){
                newSong = rand.nextInt(songs.size());
            }
            songPosn = newSong;
        }
        else {
            songPosn++;
            if (songPosn >= songs.size()) {
                songPosn = 0;
            }
        }
        playSong();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(player.getCurrentPosition() > 0){
            mediaPlayer.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        abandonAudioFocus();
        mediaPlayer.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (!audioFocusGranted) {
            requestAudioFocus();
        }
        mediaPlayer.start();
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification not = builder.build();
        startForeground(NOTIFY_ID, not);

    }
}
