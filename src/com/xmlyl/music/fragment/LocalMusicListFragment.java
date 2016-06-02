/*
 * Copyright (C) 2012 Andrew Neal Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.xmlyl.music.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.xmlyl.music.R;
import com.xmlyl.music.info.GetMusicInfo;
import com.xmlyl.music.info.MusicInfo;
import com.xmlyl.music.util.Utils;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class LocalMusicListFragment extends Fragment {
	private static Context mContext;
	private View rootView = null;
	private static ListView musicInfoListView = null;
	
	private static List<MusicInfo> musicInfo = null;
	private static List<Map<String, Object>> musicInfoList = null;
	public static SimpleAdapter musicInfoListAdapter = null;

//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        
//        mContext = getContext();
//    }

    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.musicinfo_list_fragment, container, false);
        
        musicInfoListView = (ListView) rootView.findViewById(R.id.music_info_list);
        
        mContext = getContext();
        initListView();
        
        return rootView;
    }
    
    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        //mContext = getContext();
    }
    
    public static void initListView(){
    	Utils.isInMusicList = true;
    	Utils.isInArtistMusicList = false;
    	Utils.isInAlbumMusicList = false;
    	
    	//Log.i("MusicFragment", "initListView: "+Utils.isInMusicList);
    	
    	musicInfo = GetMusicInfo.getMusicInfo(mContext , null, null, Utils.musicSortOrder);
      	if(LocalMusicFragment.musicInfo == null){
      		LocalMusicFragment.musicInfo = new ArrayList<MusicInfo>(musicInfo);
      	}
        getMusicInfoList();
        getMusicInfoListAdapter();
        musicInfoListView.setAdapter(musicInfoListAdapter);
        musicInfoListView.setOnItemClickListener(new OnItemClickListener() {
        	
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(LocalMusicFragment.musicInfo != null){
					LocalMusicFragment.musicInfo.clear();
					LocalMusicFragment.musicInfo = null;
				}
				LocalMusicFragment.musicInfo = new ArrayList<MusicInfo>(musicInfo);
				LocalMusicFragment.MusicPlay(position);
				
				LocalMusicFragment.positionPlay = position;
				musicInfoListAdapter.notifyDataSetChanged();
				
				Utils.isPlayingInMusicList = true;
				Utils.isPlayingInAlbumMusicList = false;
				Utils.isPlayingInArtistMusicList = false;
				
				//parent.getChildAt(LocalMusicFragment.positionPlay).setBackgroundResource(R.color.white_color);
//				((Activity) mContext ).runOnUiThread(new Runnable() {
//					
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						try {
//							Thread.sleep(1000);
//							musicInfoListAdapter.notifyDataSetChanged();
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//				});
				//musicInfoListAdapter.notifyDataSetChanged();
			}
		});
        musicInfoListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
				File file = new File(musicInfo.get(position).getPath());
				if(file.exists()){
					LocalMusicFragment.isMusicPlaying = false;
					file.delete();
					Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			    	scanIntent.setData(Uri.fromFile(file));
			    	mContext .sendBroadcast(scanIntent);
				}
				((Activity) mContext ).runOnUiThread(new Runnable(){
					
					@Override
					public void run(){
		            	try{
							Thread.sleep(1000);
							if(Utils.isPlayingInMusicList){
								LocalMusicFragment.UpdateMusicInfo(position);
							}else{
								musicInfo = GetMusicInfo.getMusicInfo(mContext , null, null, Utils.musicSortOrder);
						        getMusicInfoList();
								musicInfoListAdapter.notifyDataSetChanged();
							}
						} catch (InterruptedException e){
							e.printStackTrace();
						}
					}
				});
				
				return true;
			}
		});
    }
    
    public static void getMusicInfoList(){
    	if(musicInfoList == null){
    		musicInfoList = new ArrayList<Map<String, Object>>();
    	}
    	musicInfoList.clear();
        for(int i=0; i<musicInfo.size(); ++i){
        	Map<String, Object> map = new HashMap<String, Object>();
        	map.put("title", musicInfo.get(i).getTitle());
        	map.put("artist", musicInfo.get(i).getArtist());
        	float duration = (float) (musicInfo.get(i).getDuration()/60.0/1000.0);
        	int pre = (int)duration;
        	float suf = (duration-pre)*60;
        	map.put("duration",String.valueOf(pre)+":"+Utils.decimalFormat.format(suf));
        	musicInfoList.add(map);
        }
	}

    public static void getMusicInfoListAdapter(){
    	musicInfoListAdapter = new SimpleAdapter(mContext , musicInfoList, R.layout.musicinfo_item_layout,
                new String[]{"title", "artist", "duration"},
                new int[]{R.id.left_top, R.id.left_bottom, R.id.right})
    	{
        	@Override
        	public View getView(int position, View convertView, ViewGroup parent) {
        		View view;
        		if(convertView == null){
            		LayoutInflater mInflater = (LayoutInflater) mContext .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            		view = mInflater.inflate(R.layout.musicinfo_item_layout, parent, false);
        		}else{
        			view = convertView;
        		}
        		((TextView) view.findViewById(R.id.left_top)).setText((CharSequence) musicInfoList.get(position).get("title"));
        		((TextView) view.findViewById(R.id.left_bottom)).setText((CharSequence) musicInfoList.get(position).get("artist"));
        		((TextView) view.findViewById(R.id.right)).setText((CharSequence) musicInfoList.get(position).get("duration"));
        	    if(Utils.isPlayingInMusicList && LocalMusicFragment.mediaPlayer!=null && musicInfoList.get(position).get("title").equals(Utils.playingMusicName)){
        	    	view.setBackgroundResource(R.color.list_item_bg_normal_color);
        	    }else{
        	    	view.setBackgroundResource(R.color.white_color);
        	    }
        	    return view;
        	}
    	}
    	;
    }
    
    public static void updateMusicInfoListAdapter(int position){
    	musicInfo = GetMusicInfo.getMusicInfo(mContext , null, null, Utils.musicSortOrder);
        getMusicInfoList();
		musicInfoListAdapter.notifyDataSetChanged();
		if(LocalMusicFragment.musicInfo != null){
			LocalMusicFragment.musicInfo.clear();
			LocalMusicFragment.musicInfo = null;
		}
		LocalMusicFragment.musicInfo = new ArrayList<MusicInfo>(musicInfo);
    }
    
    @Override
	public void onDestroyView(){
    	
    	if(musicInfo != null){
	    	musicInfo.clear();
	    	musicInfo = null;
    	}
    	if(musicInfoList != null){
	    	musicInfoList.clear();
	        musicInfoList = null;
    	}
        musicInfoListAdapter = null;
        
    	super.onDestroyView();
    }
}
