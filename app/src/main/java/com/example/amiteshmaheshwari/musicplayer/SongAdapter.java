package com.example.amiteshmaheshwari.musicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by amiteshmaheshwari on 28/06/16.
 */
public class SongAdapter extends BaseAdapter {

    private ArrayList<Song> songs;
    private LayoutInflater songInf;

    public SongAdapter(Context c, ArrayList<Song> theSongs){
        songs = theSongs;
        songInf = LayoutInflater.from(c);
    }

    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView= (LinearLayout)songInf.inflate(R.layout.song, parent, false);
        }
        TextView songView = (TextView)convertView.findViewById(R.id.song_title);
        TextView artistView = (TextView)convertView.findViewById(R.id.song_artist);
        Song currSong = songs.get(position);
        songView.setText(position + " - " + currSong.getTitle());
        artistView.setText(currSong.getArtist());

        convertView.setTag(position);
        return convertView;
    }
}
