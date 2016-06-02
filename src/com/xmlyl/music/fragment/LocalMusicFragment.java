package com.xmlyl.music.fragment;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.xmlyl.music.info.MusicInfo;
import com.xmlyl.music.util.Utils;
import com.xmlyl.music.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint({ "ResourceAsColor", "InflateParams", "CutPasteId" })
public class LocalMusicFragment extends Fragment implements OnClickListener{
	private static final String TAG = "MusicFragment";
	
	public static Uri uri = null;
	public static String songPath = null;
	public static MediaPlayer mediaPlayer = null;
	
	public static List<MusicInfo> musicInfo = null;
	
	public static List<Fragment> fragmentList = null;
	public static Fragment localMusicListFragment = null;
	public static Fragment localAlbumListFragment = null;
	public static Fragment localArtistListFragment = null;
	
	public static boolean isMusicPlaying = false;
	public static int positionPlay = 0;
	public static int indexViewPager = 0;
	public static int preIndexViewPager = 0;
	
	public static int buttonPressColor = 0;
	public static int buttonNormalColor = 0;
	
	private static int offsetCursor = 0;
	private static int widthCursor = 0;
	
	private static Context mContext;
	
	private Button btnTitleMusic;
	private Button btnTitleAlbum;
	private Button btnTitleArtist;
	private ImageView musicPlayPre = null;
	public static ImageView musicPlayPause = null;
	private ImageView musicPlayNext = null;
	
	private static ImageView imageViewCursor = null;
	private static SeekBar musicPlaySeekBar;
	private static TextView musicPlayName = null;
	private static TextView musicTimePlay = null;
	private static TextView musicTimeEnd = null;
	private static ViewPager viewPager = null;
	private static FragmentAdapter fragmentAdapter = null;
	
	public static RemoteViews mRemoteView = null;
	private static NotificationManager mNotificationManager = null;
	private static Notification mNotification = null;
	private static int mNotificationId = 0;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        
        Log.i(TAG, "LocalMusicFragment onActivityCreated");
        
        //Utils.initStatusBarColor(this); 
        
        //setContentView(R.layout.fragment_localmusic_layout);
        
        //mContext  = getContext();
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		Utils.mContext  = getContext();
		mContext = getContext();
		
		View view = inflater.inflate(R.layout.fragment_localmusic_layout, container, false);
		isMusicPlaying = false;
    	positionPlay = 0;
    	indexViewPager = 0;
        
        fragmentList = new ArrayList<Fragment>();
        localMusicListFragment = new LocalMusicListFragment();
        localAlbumListFragment = new LocalAlbumListFragment();
        localArtistListFragment = new LocalArtistListFragment();
        
        viewPager = (ViewPager) view.findViewById(R.id.musicinfo_list_viewpager);
        
        musicPlaySeekBar = (SeekBar)view.findViewById(R.id.music_play_seekbar);
        musicTimePlay = (TextView)view.findViewById(R.id.music_time_play);
        musicPlayName = (TextView)view.findViewById(R.id.music_play_name);
        musicTimeEnd = (TextView)view.findViewById(R.id.music_time_end);
        musicPlayPause = (ImageView)view.findViewById(R.id.music_play_pause);
        musicPlayPre = (ImageView)view.findViewById(R.id.music_play_pre);
        musicPlayNext = (ImageView)view.findViewById(R.id.music_play_next);
        btnTitleMusic = (Button)view.findViewById(R.id.local_music_title);
        btnTitleAlbum = (Button)view.findViewById(R.id.local_album_title);
        btnTitleArtist = (Button)view.findViewById(R.id.local_artist_title);
        btnTitleMusic.setSelected(true);
        musicPlayPause.setOnClickListener(this);
        musicPlayPre.setOnClickListener(this);
        musicPlayNext.setOnClickListener(this);
        btnTitleMusic.setOnClickListener(this);
        btnTitleAlbum.setOnClickListener(this);
        btnTitleArtist.setOnClickListener(this);
       
        fragmentList.add(localMusicListFragment);
        fragmentList.add(localAlbumListFragment);
        fragmentList.add(localArtistListFragment);

        fragmentAdapter = new FragmentAdapter(getChildFragmentManager(), fragmentList);
        viewPager.setAdapter(fragmentAdapter);
        
        imageViewCursor= (ImageView) view.findViewById(R.id.cursor);
        
        widthCursor = imageViewCursor.getWidth();
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;
        offsetCursor = (screenW / fragmentList.size() - widthCursor) / 2;
        Matrix matrix = new Matrix();
        matrix.postTranslate(offsetCursor, 0);
        imageViewCursor.setImageMatrix(matrix);

        musicPlaySeekBar.setProgress(0);
        musicTimePlay.setText("0:00");
        musicTimeEnd.setText("0:00");
        
    	mNotificationManager = (NotificationManager)mContext .getSystemService(Context.NOTIFICATION_SERVICE);
    	mNotificationId = (int) System.currentTimeMillis();
    	mNotification = createNotification();

        setViewPagerChangeListener();

        setSeekBarOnClickListener();
		setSeekBarMoveListener();
		
		return view;
	}
    
    @SuppressWarnings("deprecation")
	public void setViewPagerChangeListener(){
    	final int baseOffset = offsetCursor * 2 + widthCursor;
    	
    	viewPager.setOnPageChangeListener(new OnPageChangeListener(){
    		
        	@Override
        	public void onPageScrollStateChanged(int arg0){
        	}

        	@Override
        	public void onPageScrolled(int arg0, float arg1, int arg2){
        	}

        	@Override
        	public void onPageSelected(int arg0){
        		//Log.i(TAG, " "+preIndexViewPager+" "+arg0);
        		Animation animation = new TranslateAnimation(baseOffset*preIndexViewPager, baseOffset*arg0, 0, 0);
                animation.setFillAfter(true);
                animation.setDuration(300);
                imageViewCursor.startAnimation(animation);
                
        		indexViewPager = arg0;
        		preIndexViewPager = indexViewPager;
        		
        		btnTitleMusic.setSelected(false);
				btnTitleAlbum.setSelected(false);
				btnTitleArtist.setSelected(false);

        		if(indexViewPager == 0){
        			btnTitleMusic.setSelected(true);
        			LocalMusicListFragment.initListView();
        		}else if(indexViewPager == 1){
        			btnTitleAlbum.setSelected(true);
        			LocalAlbumListFragment.initAlbumInfoListView();
        		}else if(indexViewPager == 2){
        			btnTitleArtist.setSelected(true);
        			LocalArtistListFragment.initArtistInfoListView();
        		}
        	}
        });
    }
    
    public void setSeekBarOnClickListener(){
    	musicPlaySeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			@Override
			public void onStopTrackingTouch(SeekBar seekBar){
				if(mediaPlayer != null){
					mediaPlayer.seekTo(seekBar.getProgress());
				}
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar){
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
				float duration = progress/60.0f/1000.0f;
	        	int pre = (int)duration;
	        	int suf = (int)((duration-pre)*60);
	        	musicTimePlay.setText(String.valueOf(pre)+":"+Utils.decimalFormat.format(suf));
			}
		});
    }
    
    public void setSeekBarMoveListener(){
    	new Thread(new Runnable(){
			
			@Override
			public void run(){
				while (true){
	            	try{
						Thread.sleep(500);
						if(isMusicPlaying){
							musicPlaySeekBar.setProgress(mediaPlayer.getCurrentPosition());
						}
					} catch (InterruptedException e){
						e.printStackTrace();
					}
	            }
			}
		}).start();
    }
	
    public static void actionPauseOrPlay(){
    	if(isMusicPlaying){
			isMusicPlaying = false;
			mediaPlayer.pause();
			musicPlayPause.setBackgroundResource(R.drawable.music_to_pause);
			songPath = musicInfo.get(positionPlay).getPath();
			File songFile = new File(songPath);
			if(!songFile.exists()){
				UpdateMusicInfo(positionPlay);
				updateNotification("Title", "Artist", true, true);
				return;
			}
			updateNotification(musicInfo.get(positionPlay).getTitle(), musicInfo.get(positionPlay).getArtist(), true, true);
		}else{
			if(mediaPlayer != null){
				songPath = musicInfo.get(positionPlay).getPath();
				File songFile = new File(songPath);
				if(!songFile.exists()){
					UpdateMusicInfo(positionPlay);
					return;
				}
				//only used to test--stop ximalaya music playing if it was *******************************
		    	XmPlayerManager mXmPlayerManager = XmPlayerManager.getInstance(mContext);
		    	if (mXmPlayerManager.isPlaying())
				{
		    		mXmPlayerManager.pause();
				}
				isMusicPlaying = true;
				mediaPlayer.start();
				musicPlayPause.setBackgroundResource(R.drawable.music_to_start);
				updateNotification(musicInfo.get(positionPlay).getTitle(), musicInfo.get(positionPlay).getArtist(), false, true);
			}else{
				//MusicPlay(positionPlay);
			}
		}
    }
    
    public static void MusicPlay(final int position){
    	//only used to test--stop ximalaya music playing if it was *******************************
    	XmPlayerManager mXmPlayerManager = XmPlayerManager.getInstance(mContext);
    	if (mXmPlayerManager.isPlaying())
		{
    		mXmPlayerManager.pause();
		}
    	
    	isMusicPlaying = false;
    	if(!Utils.isMusicInfoEmpty()){
	    	int totalTime = musicInfo.get(position).getDuration();
			musicPlaySeekBar.setMax(totalTime);
			songPath = musicInfo.get(position).getPath();
			File songFile = new File(songPath);
			if(!songFile.exists()){
				UpdateMusicInfo(position);
				return;
			}
			uri = Uri.fromFile(songFile);
			try{
				if(mediaPlayer != null){
					if(mediaPlayer.isPlaying()){
						mediaPlayer.pause();
						mediaPlayer.stop();
					}
					mediaPlayer.reset();
					mediaPlayer = null;
				}
				mediaPlayer = new MediaPlayer();
				mediaPlayer.setDataSource(mContext , uri);
				mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
					
					@Override
					public void onPrepared(MediaPlayer mp) {
						//Log.i(TAG, "start play music **********************");
						mediaPlayer.start();
						isMusicPlaying = true;
						positionPlay = position;
						setMusicViewInfos();
						updateNotification(musicInfo.get(positionPlay).getTitle(), musicInfo.get(positionPlay).getArtist(), false, true);
					}
				});
				mediaPlayer.prepareAsync();
			} catch (IllegalStateException e){
				e.printStackTrace();
			} catch (IOException e){
				e.printStackTrace();
			}
    	}
    }
    
    public static void UpdateMusicInfo(int position){
    	
    	Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    	scanIntent.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
    	mContext .sendBroadcast(scanIntent);
    
    	Toast.makeText(mContext , "The music file doesn't exists, already updated music list.", Toast.LENGTH_SHORT).show();
		if(mediaPlayer != null && mediaPlayer.isPlaying() && positionPlay != position){
			isMusicPlaying = true;
			if(positionPlay > position){
				positionPlay -= 1;
			}
		}else{
			if(mediaPlayer != null){
				if(mediaPlayer.isPlaying()){
					isMusicPlaying = false;
					mediaPlayer.pause();
					mediaPlayer.stop();
				}
				mediaPlayer.reset();
				mediaPlayer = null;
			}

			musicPlayPause.setBackgroundResource(R.drawable.music_to_pause);
			updateNotification("Title", "Artist", true, true);
			musicPlaySeekBar.setProgress(0);
	        musicTimePlay.setText("0:00");
	        musicTimeEnd.setText("0:00");
	        musicPlayName.setText("");
			if(positionPlay == musicInfo.size()-1){
				positionPlay = 0;
			}
		}
		if(Utils.isPlayingInMusicList){
			LocalMusicListFragment.updateMusicInfoListAdapter(position);
		}else if(Utils.isPlayingInAlbumMusicList){
			LocalAlbumListFragment.updateAlbumInfoListAdapter(position);
		}else if(Utils.isPlayingInArtistMusicList){
			LocalArtistListFragment.updateArtistInfoListAdapter(position);
		}
    }
    
    public static void setMusicViewInfos(){
    	//Log.i("MusicFragment", "setMusicViewInfos: "+Utils.isInMusicList);
    	
    	Utils.playingMusicName = musicInfo.get(positionPlay).getTitle();
    	
    	//Log.i("MusicFragment", "setMusicViewInfos");
    	if(Utils.isPlayingInArtistMusicList && Utils.isInArtistMusicList){
			LocalArtistListFragment.artistInfoListAdapter.notifyDataSetChanged();
		}else if(Utils.isPlayingInAlbumMusicList && Utils.isInAlbumMusicList){
			LocalAlbumListFragment.albumInfoListAdapter.notifyDataSetChanged();
		}else if(Utils.isPlayingInMusicList && Utils.isInMusicList){
			LocalMusicListFragment.musicInfoListAdapter.notifyDataSetChanged();
		}
    	
    	musicPlayName.setText(musicInfo.get(positionPlay).getTitle());
    	float duration = (float) (musicInfo.get(positionPlay).getDuration()/60.0/1000.0);
    	int pre = (int)duration;
    	float suf = (duration-pre)*60;
        musicTimeEnd.setText(String.valueOf(pre)+":"+Utils.decimalFormat.format(suf));
		mediaPlayer.setOnCompletionListener(new OnCompletionListener(){
			
			@Override
			public void onCompletion(MediaPlayer mp){
				musicPlayPause.setBackgroundResource(R.drawable.music_to_pause);
				String str = musicTimeEnd.getText().toString();
				int result = Integer.parseInt(str.substring(str.indexOf(":")+1));
				float duration = (float)((mediaPlayer.getCurrentPosition())/60.0/1000.0);
	        	int pre = (int)duration;
	        	int suf = (int)((duration-pre)*60);
	        	if(suf != result){
	        		++suf;
	        		if(suf == 60){
	        			suf = 0;
	        			++pre;
	        		}
	        	}
	        	Log.i("AudioInfo", String.valueOf(suf));
	        	musicTimePlay.setText(String.valueOf(pre)+":"+Utils.decimalFormat.format(suf));
	        	if(!Utils.isMusicInfoEmpty()){
	        		MusicPlay((positionPlay+1)%musicInfo.size());
	        	}
			}
		});
		musicPlayPause.setBackgroundResource(R.drawable.music_to_start);
    }
    
    public class FragmentAdapter extends FragmentPagerAdapter{
        private List<Fragment> fragmentList;
        
        public FragmentAdapter(FragmentManager fragmentManager, List<Fragment> fragmentListArg){
            super(fragmentManager);
            fragmentList = fragmentListArg;
        }
        
        @Override
        public Fragment getItem(int arg0){
            return fragmentList.get(arg0);
        }
        
        @Override
        public int getCount(){
            return fragmentList.size();
        }
    } 
	
    public Notification createNotification(){
		NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext );

		Intent main = new Intent(mContext , LocalMusicFragment.class);
		PendingIntent mainPi = PendingIntent.getActivity(mContext , 0, main, 0);
		
		Intent play = new Intent(Utils.ACTION_CONTROL_PLAY_PAUSE);
		PendingIntent playPi = PendingIntent.getBroadcast(mContext , 0, play, 0);
		
		Intent next = new Intent(Utils.ACTION_CONTROL_PLAY_NEXT);
		PendingIntent nextPi = PendingIntent.getBroadcast(mContext , 0, next, 0);
		
		Intent pre = new Intent(Utils.ACTION_CONTROL_PLAY_PRE);
		PendingIntent prePi = PendingIntent.getBroadcast(mContext , 0, pre, 0);
		
		mRemoteView = new RemoteViews(mContext .getPackageName(), R.layout.notify_show_playmusic);
		mRemoteView.setOnClickPendingIntent(R.id.img_notifyIcon, mainPi);
		mRemoteView.setOnClickPendingIntent(R.id.img_notifyPlayOrPause, playPi);
		mRemoteView.setOnClickPendingIntent(R.id.img_notifyNext, nextPi);
		mRemoteView.setOnClickPendingIntent(R.id.img_notifyPre, prePi);
		
		builder.setContent(mRemoteView).setSmallIcon(R.drawable.launcher_icon)
				.setContentTitle("Title").setContentText("Artist")
				.setContentIntent(mainPi);
		
		return builder.build();
	}
    
	public static void updateNotification(String title, String artist, boolean isPlaying, boolean hasNext){
		if(!TextUtils.isEmpty(title)){
			mRemoteView.setTextViewText(R.id.txt_notifyMusicName, title);
		}
		if(!TextUtils.isEmpty(artist)){
			mRemoteView.setTextViewText(R.id.txt_notifyNickName, artist);
		}
		if(isPlaying){
			mRemoteView.setImageViewResource(R.id.img_notifyPlayOrPause, R.drawable.ic_pause);
		}else{
			mRemoteView.setImageViewResource(R.id.img_notifyPlayOrPause, R.drawable.ic_play);
		}
		//mNotificationManager.notify(mNotificationId, mNotification);
	}
	
	@Override
	  public void onDetach() {
	    super.onDetach();
	    try {
	    	Field childFragmentManager = Fragment.class
	          .getDeclaredField("mChildFragmentManager");
	    	childFragmentManager.setAccessible(true);
	    	childFragmentManager.set(this, null);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void onPause(){
		super.onPause();
		
		Log.i(TAG, "LocalMusicFragment onPause");
	}
	
	@Override
	public void onStop(){
		super.onStop();
		
		Log.i(TAG, "LocalMusicFragment onStop");
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		Log.i(TAG, "LocalMusicFragment onDestroy");
	}
    
    @Override
	public void onDestroyView(){
    	Log.i(TAG, "LocalMusicFragment onDestroyView");
    	
    	isMusicPlaying = false;
    	
    	if(mNotificationManager != null){
    		//Log.i("MusicFragment", "***************************");
    		mNotificationManager.cancelAll();
    	}
    	
    	if(musicInfo != null){
			musicInfo.clear();
			musicInfo = null;
		}
    	if(fragmentList != null){
	    	fragmentList.clear();
	    	fragmentList = null;
    	}
    	localMusicListFragment = null;
    	localAlbumListFragment = null;
    	localArtistListFragment = null;
    	
    	if(mediaPlayer != null){
    		if(mediaPlayer.isPlaying()){
	    		mediaPlayer.pause();
				mediaPlayer.stop();
    		}
			mediaPlayer.release();
			mediaPlayer = null;
		}
    	
    	Utils.isPlayingInMusicList = false;
    	
    	super.onDestroyView();
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		preIndexViewPager = indexViewPager;
		switch(v.getId()){
		case R.id.music_play_pre:
			if(!Utils.isMusicInfoEmpty() && mediaPlayer!=null){
				MusicPlay((musicInfo.size()+positionPlay-1)%musicInfo.size());
			}
			break;
		case R.id.music_play_next:
			if(!Utils.isMusicInfoEmpty() && mediaPlayer!=null){
				MusicPlay((positionPlay+1)%musicInfo.size());
			}
			break;
		case R.id.music_play_pause:
			if(!Utils.isMusicInfoEmpty()){
				actionPauseOrPlay();
			}
			break;
		case R.id.local_music_title:
			indexViewPager = 0;
			if(indexViewPager != preIndexViewPager){
				viewPager.setCurrentItem(indexViewPager);
			}
			if(Utils.isInMusicList){
				LocalMusicListFragment.initListView();
			}
			break;
		case R.id.local_album_title:
			indexViewPager = 1;
			if(indexViewPager != preIndexViewPager){
				viewPager.setCurrentItem(indexViewPager);
			}
			if(Utils.isInAlbumMusicList){
				LocalAlbumListFragment.initAlbumInfoListView();
			}
			break;
		case R.id.local_artist_title:
			indexViewPager = 2;
			if(indexViewPager != preIndexViewPager){
				viewPager.setCurrentItem(indexViewPager);
			}
			if(Utils.isInArtistMusicList){
				LocalArtistListFragment.initArtistInfoListView();
			}
			break;
		default:
			break;
		}
	}
}