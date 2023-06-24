package com.hari.musicplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Home extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private MediaPlayer mediaPlayer;
    int pos=0;


    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> mp3List = new ArrayList<>();

    ImageButton playpause,next,prev;
//    ImageView song=(ImageView) findViewById(R.id.home_iv1);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().hide();

        listView = findViewById(R.id.home_lv1);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mp3List);
        listView.setAdapter(adapter);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            getMP3Files();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String mp3Name = mp3List.get(position);
                pos=position;
                File externalStorage = Environment.getExternalStorageDirectory();
                File file = searchForFile(externalStorage, mp3Name);
                String filePath = "";
                if (file != null) {
                    filePath = file.getAbsolutePath();
                    // rest of your code
                } else {
                    Toast.makeText(Home.this, "Not found", Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG, "Clicked on item " + position + " with file path " + filePath);
                playAudio(filePath);
            }
        });



        prev=findViewById(R.id.home_ib1);
        playpause=findViewById(R.id.home_ib2);
        next=findViewById(R.id.home_ib3);

        //prev button
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pos=pos-1;
                String mp3Name = mp3List.get(pos);
                File externalStorage = Environment.getExternalStorageDirectory();
                File file = searchForFile(externalStorage, mp3Name);
                String filePath = file.getAbsolutePath();
                Log.d(TAG, "Clicked on item " + pos + " with file path " + filePath);
                playAudio(filePath);
            }
        });

        //next button
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pos=pos+1;
                String mp3Name = mp3List.get(pos);
                File externalStorage = Environment.getExternalStorageDirectory();
                File file = searchForFile(externalStorage, mp3Name);
                String filePath = file.getAbsolutePath();
                Log.d(TAG, "Clicked on item " + pos + " with file path " + filePath);
                playAudio(filePath);
            }
        });

        //playpause button
        playpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playpause.setImageResource(R.drawable.cplay);
                } else {
                    mediaPlayer.start();
                    playpause.setImageResource(R.drawable.cpause);
                }
            }
        });
    }

    private File searchForFile(File directory, String fileName) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    File result = searchForFile(file, fileName);
                    if (result != null) {
                        return result;
                    }
                } else if (file.getName().equals(fileName)) {
                    return file;
                }
            }
        }
        return null;
    }

    private void getMP3Files() {
        String[] projection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME};
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                mp3List.add(name);
            }
            cursor.close();
            adapter.notifyDataSetChanged();
        }
    }


    private void playAudio(String filePath) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        } else {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }

        try {
            File file = new File(filePath);
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            playpause.setImageResource(R.drawable.cpause);
            Log.d(TAG, "Started playing audio with file path " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to play audio with file path " + filePath, e);
        }

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);

        byte[] artwork = retriever.getEmbeddedPicture();
        if (artwork != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(artwork, 0, artwork.length);
//            song.setImageBitmap(bitmap);
        } else {
            // no artwork found
//            song.setImageResource(R.drawable.llogobg);
        }
    }
//    private void playAudio(String filePath) {
//        MediaPlayer mediaPlayer = MediaPlayer.create(this, Uri.parse(filePath));
//        mediaPlayer.start();
//    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getMP3Files();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}


