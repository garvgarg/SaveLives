package com.parse.loginsample.withdispatchactivity;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.VideoView;
import android.widget.MediaController;

public class TrainingVideoPlayerActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.training_video);
	    VideoView videoView =(VideoView)findViewById(R.id.videoView);
	    MediaController mediaController= new MediaController(this);
	    mediaController.setAnchorView(videoView);        
	    Uri uri=Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.trauma);        
	    videoView.setMediaController(mediaController);
	    videoView.setVideoURI(uri);        
	    videoView.requestFocus();
	    videoView.start();
	}
}   
	/*
	@Override
	protected void onStart() {
		super.onStart();
		
		final VideoView videoView = 
	               (VideoView) findViewById(R.id.videoView1);
			
	           videoView.setVideoPath(
         "http://www.ebookfrenzy.com/android_book/movie.mp4");
			
	       MediaController mediaController = new 
	       			MediaController(this);
	       		mediaController.setAnchorView(videoView);
	       	videoView.setMediaController(mediaController);

		videoView.start();

	}
	*/