/*
 * This file is part of radio reddit for Android.
 *
 * radio reddit for Android is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * radio reddit for Android is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with radio reddit for Android.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.radioreddit.android.api;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.radioreddit.android.AllSongInfo;
import com.radioreddit.android.MainActivity;
import com.radioreddit.android.R;

public class GetStationStatus extends AsyncTask<String, Integer, StationStatus> {
    private MainActivity mMainActivity;
    private String mCookie;
    
    public GetStationStatus(MainActivity mainActivity, String cookie) {
        mMainActivity = mainActivity;
        mCookie = cookie;
    }
    
    @Override
    protected StationStatus doInBackground(String... params) {
        InputStream source = InternetCommunication.retrieveStream(params[0]);
        
        if (source == null) {
            // No Internet connection
            return null;
        }
        
        Reader reader = new InputStreamReader(source);
        Gson gson = new Gson();
        return gson.fromJson(reader, StationStatus.class);
    }
    
    @Override
    protected void onPostExecute(StationStatus result) {
        if (result == null) {
            // No Internet connection
            mMainActivity.displaySongInfo(null);
            return;
        }
        
        // XXX: Work-around for the talk stream. I guess it uses episodes instead?
        final StationStatus status = result;
        SongInfo currentSong;
        if (status.songs == null || status.songs.song == null || status.songs.song.isEmpty()) {
            final String filler = mMainActivity.getString(R.string.info_filler);
            currentSong = new SongInfo();
            currentSong.title = filler;
            currentSong.artist = filler;
            currentSong.genre = filler;
            currentSong.redditor = filler;
        } else {
            currentSong = status.songs.song.get(0);
        }
        
        // Copy the info we want to display to a new object 
        final AllSongInfo song = new AllSongInfo();
        song.title = currentSong.title;
        song.artist = currentSong.artist;
        song.genre = currentSong.genre;
        song.redditor = currentSong.redditor;
        song.playlist = status.playlist;
        song.reddit_url = currentSong.reddit_url;
        
        // Not all the information we need is available yet. We need to make
        // a request to reddit to get vote/save info for this song before
        // sending back to the main activity
        new GetSongInfo(mMainActivity, song).execute(mCookie);
    }
}
