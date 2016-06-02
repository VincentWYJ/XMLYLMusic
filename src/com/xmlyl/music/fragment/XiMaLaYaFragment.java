package com.xmlyl.music.fragment;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.xmlyl.music.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.advertis.Advertis;
import com.ximalaya.ting.android.opensdk.model.advertis.AdvertisList;
import com.ximalaya.ting.android.opensdk.model.live.radio.Radio;
import com.ximalaya.ting.android.opensdk.model.live.schedule.Schedule;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.advertis.IXmAdsStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;
import com.xmlyl.music.constants.Constants;
import com.xmlyl.music.util.ToolUtil;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import net.tsz.afinal.FinalBitmap;
import net.tsz.afinal.FinalHttp;

@SuppressLint("ViewHolder")
public class XiMaLaYaFragment extends Fragment implements OnClickListener
{
	private static final String TAG = "MusicFragment";
	
	private String mAppSecret = "4d8e605fa7ed546c4bcb33dee1381179";
	
	private static final String[] Titles = new String[] {
			"总榜", "资讯", "音乐", "有声书", "娱乐", "外语"
			, "儿童","健康养生", "商业财经", "历史人文", "情感生活", "相声评书"
			, "教育培训","百家讲坛", "广播剧", "戏曲", "电台", "IT科技"
			, "校园","汽车", "旅游", "电影", "动漫游戏", "时尚生活"};

	private TextView mTextView;
	private ImageView mSoundCover;
	private SeekBar mSeekBar;
	private ProgressBar mProgress;
	private ImageButton mBtnPreSound;
	private ImageButton mBtnPlay;
	private ImageButton mBtnNextSound;
	private TabLayout mTabLayout;
	private Button mBtnMoreClass;
	private ViewPager mViewPager;
	
	private PagerAdapter mViewPagerAdapter;
	
	private Dialog dialogPopup;
	private GridView mGridView;
	private Button mBtnCancel;
	private SimpleAdapter mSimpleAdapter;
	
	private NotificationManager mNotificationManager;
	private int mNotificationId;
	private RemoteViews mRemoteView;
	private Notification mNotification;
	
	private Context mContext;

	private FinalBitmap mFinalBitmap;
	private XmPlayerManager mPlayerManager;
	private CommonRequest mXimalaya;

	private boolean mUpdateProgress = true;

	private List<BaseFragment> fragmentList;
	
	public static int indexFragment = 0; 
	
	@Override
	public void onActivityCreated(Bundle arg0)
	{
		super.onActivityCreated(arg0);
		
		Log.i(TAG, "XiMaLaYaFragment onActivityCreated");
		
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		//setContentView(R.layout.activity_ximalaya_layout);
		
		//ToolUtil.initStatusBarColor(getActivity());

		mContext = getContext();
		
		new FinalHttp();
		
		mXimalaya = CommonRequest.getInstanse();
		mXimalaya.init(mContext, mAppSecret);
		mXimalaya.setDefaultPagesize(50);  //设定一次加载的音乐条目数, 通过调试总条目数好像为1000, 即总页数为1000/50=20
		
		fragmentList = new ArrayList<BaseFragment>();
		for(int i=0; i<24; ++i){
			int position = 0;
			if(i<11){
				position = i;
			}else if(i<18){
				position = i+1;
			}else if(i<23){
				position = i+2;
			}else if(i<24){
				position = i+8;
			}
			//fragmentList.add(new Fragment_BangDan(mContext, position));
			fragmentList.add(Fragment_BangDan.newInstance(position));
		}
		
		mPlayerManager = XmPlayerManager.getInstance(mContext);
		mPlayerManager.init(mNotificationId, null);
		mPlayerManager.addPlayerStatusListener(mPlayerStatusListener);
		mPlayerManager.addAdsStatusListener(mAdsListener);
		mPlayerManager.getPlayerStatus();		

		mFinalBitmap = FinalBitmap.create(mContext.getApplicationContext());
		mFinalBitmap.configLoadingImage(R.drawable.ic_launcher);
		mFinalBitmap.configLoadfailImage(R.drawable.ic_launcher);		
		
		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotification = createNotification();
		mNotificationId = (int)System.currentTimeMillis();
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		Log.i(TAG, "XiMaLaYaFragment onCreateView");
		
		View view = inflater.inflate(R.layout.fragment_ximalaya_layout, container, false);
		mTextView = (TextView) view.findViewById(R.id.message);
		mSoundCover = (ImageView) view.findViewById(R.id.sound_cover);
		mSeekBar = (SeekBar) view.findViewById(R.id.seek_bar);  //继承自ProgressBar, 默认max value为100
		mProgress = (ProgressBar) view.findViewById(R.id.buffering_progress);
		mBtnPreSound = (ImageButton) view.findViewById(R.id.pre_sound);
		mBtnPlay = (ImageButton) view.findViewById(R.id.play_or_pause);
		mBtnNextSound = (ImageButton) view.findViewById(R.id.next_sound);
		mTabLayout = (TabLayout) view.findViewById(R.id.table_layout);  //和其他app相比, 默认高度对于小字体来说有点偏高, 如目前调整为40dp效果较好
		mBtnMoreClass = (Button) view.findViewById(R.id.more_class);
		mViewPager = (ViewPager) view.findViewById(R.id.pager);
		
		mViewPagerAdapter = new SlidingPagerAdapter(getChildFragmentManager());
		
		mViewPager.setOffscreenPageLimit(24); //参数默认为1,即当不指定或者指定值小于1时系统会当做1处理,一般同时缓存的有三页; 如置为2则一般同时缓存的页面数为5
		mViewPager.setAdapter(mViewPagerAdapter);

		mTabLayout.setupWithViewPager(mViewPager);
		
		mSoundCover.setOnClickListener(this);
		mBtnPlay.setOnClickListener(this);
		mBtnNextSound.setOnClickListener(this);
		mBtnPreSound.setOnClickListener(this);
		mBtnMoreClass.setOnClickListener(this);
		
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{

			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				mPlayerManager.seekToByPercent(seekBar.getProgress() / (float) seekBar.getMax());
				mUpdateProgress = true;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
				mUpdateProgress = false;
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
			}
		});
		
		return view;
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
		
		Log.i(TAG, "XiMaLaYaFragment onPause");
	}
	
	@Override
	public void onStop(){
		super.onStop();
		
		Log.i(TAG, "XiMaLaYaFragment onStop");
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		Log.i(TAG, "XiMaLaYaFragment onDestroy");
	}
	
	@Override
	public void onDestroyView(){
		Log.i(TAG, "XiMaLaYaFragment onDestroyView");
		if (mPlayerManager != null)
		{
			mPlayerManager.stop();
			mPlayerManager.removePlayerStatusListener(mPlayerStatusListener);
			mPlayerManager.removeAdsStatusListener(mAdsListener);
			mPlayerManager.release();
		}
		if(mNotificationManager != null){
			mNotificationManager.cancelAll();
		}
		
		super.onDestroyView();
	}

	public void Refresh(View view){
		Log.i(TAG, "refresh data");
		for(int i=0;i<24;++i){
			fragmentList.get(i).refresh();
		}
	}
	
	@SuppressWarnings("unused")
	private String generateFilePath(String baseDir, String url)
	{
		if (TextUtils.isEmpty(baseDir))
		{
			baseDir = Environment.getExternalStorageDirectory() + "/img_chache/";
		}
		File dir = new File(baseDir);
		if (dir.isFile())
		{
			dir.delete();
		}
		if (!dir.exists())
		{
			dir.mkdirs();
		}
		return baseDir + System.currentTimeMillis() + getSubfixByUrl(url);
	}
	
	private String getSubfixByUrl(String url)
	{
		if (TextUtils.isEmpty(url))
		{
			return "";
		}
		if (url.contains("."))
		{
			return url.substring(url.lastIndexOf("."));
		}
		return ".jpg";
	}
	
	class SlidingPagerAdapter extends FragmentPagerAdapter
	{
		public SlidingPagerAdapter(FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public Fragment getItem(int position)
		{
			return fragmentList.get(position);
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			return Titles[position % Titles.length];
		}

		@Override
		public int getCount()
		{
			return Titles.length;
		}
	}
	
	private IXmAdsStatusListener mAdsListener = new IXmAdsStatusListener()
	{
		
		@Override
		public void onStartPlayAds(Advertis ad, int position)
		{
			Log.i(TAG, "onStartPlayAds, Ad:" + ad.getName() + ", pos:" + position);
			if (ad != null)
			{
				//mFinalBitmap.display(mSoundCover, ad.getImageUrl());
				Glide.with(mContext).load(ad.getImageUrl()).into(mSoundCover); 
				/*
				File file = null;
				try {
					file = Glide.with(mContext)
					        .load(ad.getImageUrl())
					        .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
					        .get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(file != null){
					Log.i(TAG, file.getAbsolutePath());
					//Glide.with(mContext).load(file).into(mSoundCover); 
				}
				*/
			}
		}
		
		@Override
		public void onStartGetAdsInfo()
		{
			Log.i(TAG, "onStartGetAdsInfo");
			mBtnPlay.setEnabled(false);
			mSeekBar.setEnabled(false);
		}
		
		@Override
		public void onGetAdsInfo(AdvertisList ads)
		{
			Log.i(TAG, "onGetAdsInfo " + (ads != null));
		}
		
		@Override
		public void onError(int what, int extra)
		{
			Log.i(TAG, "onError what:" + what + ", extra:" + extra);
		}
		
		@Override
		public void onCompletePlayAds()
		{
			Log.i(TAG, "onCompletePlayAds");
			mBtnPlay.setEnabled(true);
			mSeekBar.setEnabled(true);
			PlayableModel model = mPlayerManager.getCurrSound();
			if (model != null && model instanceof Track)
			{
				//mFinalBitmap.display(mSoundCover, ((Track) model).getCoverUrlLarge());
				//Glide.with(mContext).load(((Track) model).getCoverUrlLarge()).into(mSoundCover);
			}
		}
		
		@Override
		public void onAdsStopBuffering()
		{
			Log.i(TAG, "onAdsStopBuffering");
		}
		
		@Override
		public void onAdsStartBuffering()
		{
			Log.i(TAG, "onAdsStartBuffering");
		}
	};
	
	/**
	 * Created by csonezp on 16-1-12.
	 */
	public class SaveImageTask extends AsyncTask<String, Void, File> {
	    private
	    final Context context;

	    public SaveImageTask(Context context) {
	        this.context = context;
	    }

	    @Override
	    protected File doInBackground(String... params) {
	        String url = params[0]; // should be easy to extend to share multiple images at once
	        try {
	            return Glide
	                    .with(context)
	                    .load(url)
	                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
	                    .get();
	        } catch (Exception ex) {
	            return null;
	        }
	    }

	    @Override
	    protected void onPostExecute(File result) {
	        if (result == null) {
	            return;
	        }
	        Log.i(TAG, result.getAbsolutePath());
			Glide.with(mContext).load(result).into(mSoundCover); 
	    }
	}

	private IXmPlayerStatusListener mPlayerStatusListener = new IXmPlayerStatusListener()
	{

		@Override
		public void onSoundPrepared()
		{
			Log.i(TAG, "onSoundPrepared");
			mSeekBar.setEnabled(true);
			mProgress.setVisibility(View.GONE);
		}

		@Override
		public void onSoundSwitch(PlayableModel laModel, PlayableModel curModel)
		{
			Log.i(TAG, "onSoundSwitch index:");
			PlayableModel model = mPlayerManager.getCurrSound();
			if (model != null)
			{
				//only used to test--stop local music playing if it was *******************************
				if(LocalMusicFragment.mediaPlayer!=null && LocalMusicFragment.mediaPlayer.isPlaying()){
					LocalMusicFragment.actionPauseOrPlay();
				}
				
				String title = null;
				String msg = null;
				String coverSmall = null;
				if (model instanceof Track)
				{
					Track info = (Track) model;
					title = info.getTrackTitle();
					msg = info.getAnnouncer() == null ? "" : info.getAnnouncer().getNickname();
					info.getCoverUrlLarge();
					coverSmall = info.getCoverUrlLarge();
				}
				mTextView.setText(title);  //设置播放栏位信息
				
				//new SaveImageTask(mContext).execute(coverUrl);
				//mFinalBitmap.display(mSoundCover, coverUrl);
				Glide.with(mContext).load(coverSmall).into(mSoundCover);
				if (!TextUtils.isEmpty(coverSmall))
				{
					updateRemoteViewIcon(coverSmall);
				}
				else
				{
					Log.i(TAG, "download img null");
				}
				updateNotification(title, msg, true, true);  //设置notify栏位信息
			}
			updateButtonStatus();
		}

		private void updateNotification(String title, String msg, boolean isPlaying,
				boolean hasNext)
		{
			if (!TextUtils.isEmpty(title))
			{
				mRemoteView.setTextViewText(R.id.txt_notifyMusicName, title);
			}
			if (!TextUtils.isEmpty(msg))
			{
				mRemoteView.setTextViewText(R.id.txt_notifyNickName, msg);
			}
			if (isPlaying)
			{
				mRemoteView.setImageViewResource(R.id.img_notifyPlayOrPause, R.drawable.ic_pause);
			}
			else
			{
				mRemoteView.setImageViewResource(R.id.img_notifyPlayOrPause, R.drawable.ic_play);
			}
			//mNotificationManager.notify(mNotificationId, mNotification);
		}

		private void updateRemoteViewIcon(final String coverUrl)
		{
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					 try {
						Bitmap bt = Glide.with(mContext)  
						    .load(coverUrl)  
						    .asBitmap()
						    .centerCrop()  
						    .into(500, 500)  
						    .get();
						mRemoteView.setImageViewBitmap(R.id.img_notifyIcon, bt);
						//mNotificationManager.notify(mNotificationId, mNotification);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
		}
		
		private void updateButtonStatus()
		{
			if (mPlayerManager.hasPreSound())
			{
				mBtnPreSound.setEnabled(true);
			}
			else
			{
				mBtnPreSound.setEnabled(false);
			}
			if (mPlayerManager.hasNextSound())
			{
				mBtnNextSound.setEnabled(true);
			}
			else
			{
				mBtnNextSound.setEnabled(false);
			}
		}

		@Override
		public void onPlayStop()
		{
			Log.i(TAG, "onPlayStop");
			mBtnPlay.setBackgroundResource(R.drawable.music_pause_drawable);
			updateNotification(null, null, false, true);
		}

		@Override
		public void onPlayStart()
		{
			Log.i(TAG, "onPlayStart");
			mBtnPlay.setBackgroundResource(R.drawable.music_start_drawable);
			updateNotification(null, null, false, true);
		}

		@Override
		public void onPlayProgress(int currPos, int duration)
		{
			String title = "";
			PlayableModel info = mPlayerManager.getCurrSound();
			if (info != null)
			{
				if (info instanceof Track)
				{
					title = ((Track) info).getTrackTitle();
				}
				else if (info instanceof Schedule)
				{
					title = ((Schedule) info).getRelatedProgram().getProgramName();
				}
				else if (info instanceof Radio)
				{
					title = ((Radio) info).getRadioName();
				}
			}
			mTextView.setText(title + "[" + ToolUtil.formatTime(currPos) + "/" + ToolUtil.formatTime(duration) + "]");
			if (mUpdateProgress && duration != 0)
			{
				mSeekBar.setProgress((int) (100 * currPos / (float) duration));
			}
		}

		@Override
		public void onPlayPause()
		{
			Log.i(TAG, "onPlayPause");
			mBtnPlay.setBackgroundResource(R.drawable.music_pause_drawable);
			updateNotification(null, null, true, true);
		}

		@Override
		public void onSoundPlayComplete()
		{
			Log.i(TAG, "onSoundPlayComplete");
			mBtnPlay.setBackgroundResource(R.drawable.music_pause_drawable);
		}

		@Override
		public boolean onError(XmPlayerException exception)
		{
			Log.i(TAG, "onError " + exception.getMessage());
			mBtnPlay.setBackgroundResource(R.drawable.music_pause_drawable);
			return false;
		}

		@Override
		public void onBufferProgress(int position)
		{
			mSeekBar.setSecondaryProgress(position);
		}

		public void onBufferingStart()
		{
			mSeekBar.setEnabled(false);
			mProgress.setVisibility(View.GONE);//mProgress.setVisibility(View.VISIBLE);
		}

		public void onBufferingStop()
		{
			mSeekBar.setEnabled(true);
			mProgress.setVisibility(View.GONE);
		}

	};
	
	private Notification createNotification()
	{
		NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

		Intent main = new Intent(mContext, XiMaLaYaFragment.class);
		PendingIntent mainPi = PendingIntent.getActivity(mContext, 0, main, 0);
		
		Intent play = new Intent(Constants.ACTION_CONTROL_PLAY_PAUSE);
		PendingIntent playPi = PendingIntent.getBroadcast(mContext, 0, play, 0);
		
		Intent pre = new Intent(Constants.ACTION_CONTROL_PLAY_PRE);
		PendingIntent prePi = PendingIntent.getBroadcast(mContext, 0, pre, 0);
		
		Intent next = new Intent(Constants.ACTION_CONTROL_PLAY_NEXT);
		PendingIntent nextPi = PendingIntent.getBroadcast(mContext, 0, next, 0);
		
		mRemoteView = new RemoteViews(mContext.getPackageName(), R.layout.music_notify_layout);
		mRemoteView.setOnClickPendingIntent(R.id.img_notifyIcon, mainPi);
		mRemoteView.setOnClickPendingIntent(R.id.img_notifyPlayOrPause, playPi);
		mRemoteView.setOnClickPendingIntent(R.id.img_notifyNext, nextPi);
		mRemoteView.setOnClickPendingIntent(R.id.img_notifyPre, prePi);
		
		builder.setContent(mRemoteView)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("名称")
				.setContentText("信息")
				.setContentIntent(mainPi);
		return builder.build();
	}
	
	 private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for(int i=0; i<Titles.length; ++i){
	        Map<String, Object> map = new HashMap<String, Object>();
	        map.put("title", Titles[i]);
	        list.add(map);
        }
        return list;
	 }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.sound_cover:
			Log.i(TAG, "refresh data");
			for(int i=0;i<24;++i){
				fragmentList.get(i).refresh();
			}
			break;
		case R.id.pre_sound:
			mPlayerManager.playPre();
			break;
		case R.id.play_or_pause:
			if (mPlayerManager.isPlaying())
			{
				mPlayerManager.pause();
			}
			else
			{
				mPlayerManager.play();
				//only used to test--stop local music playing if it was *******************************
				if(LocalMusicFragment.mediaPlayer!=null && LocalMusicFragment.mediaPlayer.isPlaying()){
					LocalMusicFragment.actionPauseOrPlay();
				}
			}
			break;
		case R.id.next_sound:
			mPlayerManager.playNext();
			break;
		case R.id.more_class:
			dialogPopup = new Dialog(mContext);
			dialogPopup.setContentView(R.layout.class_dialog_layout);
			dialogPopup.setCanceledOnTouchOutside(true);
            mGridView = (GridView) dialogPopup.findViewById(R.id.class_gridview);
            mBtnCancel  =(Button) dialogPopup.findViewById(R.id.cancel_button);
            mSimpleAdapter = new SimpleAdapter(mContext, getData(), R.layout.class_item_layout, 
        		new String[]{"title"}, new int[]{R.id.class_item_textview}){
            	@Override
            	public View getView(int position, View convertView, ViewGroup parent) {
            		View view;
            		if(convertView == null){
                		LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                		view = mInflater.inflate(R.layout.class_item_layout, parent, false);
            		}else{
            			view = convertView;
            		}
            	    TextView mTextView = (TextView) view.findViewById(R.id.class_item_textview);
            	    mTextView.setText(Titles[position]);
            	    if(position == mViewPager.getCurrentItem()){
            	    	mTextView.setBackgroundResource(R.color.class_item_pressed_color);
            	    }else{
            	    	mTextView.setBackgroundResource(R.color.tablayout_bg_color);
            	    }
            	    return view;
            	}
            };
            mGridView.setAdapter(mSimpleAdapter);
            dialogPopup.show();
            
            mBtnCancel.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					dialogPopup.cancel();
				}
			});
            
            mGridView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					// TODO Auto-generated method stub
					if(mViewPager.getCurrentItem() != position){
						mViewPager.setCurrentItem(position);
						mSimpleAdapter.notifyDataSetChanged();
						new Thread(new Runnable(){

						     @Override
						     public void run() {
						          // TODO Auto-generated method stub
						          try {
						               Thread.sleep(10);  //这里延迟时间设为0.01秒, 数据量大的刷新可能不够
						          } catch (InterruptedException e) {
						               // TODO Auto-generated catch block
						               e.printStackTrace();
						          }
						          dialogPopup.cancel();
						     }				
						}).start();
					}else{
						dialogPopup.cancel();
					}
				}
			});
			break;
		default:
			break;
		}
	}
}
