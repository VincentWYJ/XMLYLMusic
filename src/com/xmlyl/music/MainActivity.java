package com.xmlyl.music;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.xmlyl.music.R;
import com.xmlyl.music.adapter.DrawerListAdapter;
import com.xmlyl.music.fragment.LocalMusicFragment;
import com.xmlyl.music.fragment.XiMaLaYaFragment;
import com.xmlyl.music.info.DrawerListBean;

public class MainActivity extends BaseActivity {
	private static final String TAG = "MusicFragment";
	
	private Context mContext;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar toolbar;
    private ListView drawerListView;
    private LinearLayout drawerLayout;

    private XiMaLaYaFragment fragmentXiMaLaYa;
	private LocalMusicFragment fragmentLocalMusic;

    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mContext = this;
        
        initToolbar();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.ting_drawer);  //主布局
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mDrawerToggle = new ActionBarDrawerToggle((Activity) mContext, mDrawerLayout, toolbar,
                R.string.open, R.string.close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        fragmentXiMaLaYa = new XiMaLaYaFragment();
        fragmentLocalMusic = new LocalMusicFragment();
        
        getSupportFragmentManager().beginTransaction().add(R.id.container, fragmentLocalMusic).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.container, fragmentXiMaLaYa).commit();

        drawerLayout = (LinearLayout) findViewById(R.id.drawer_layout);  //侧滑布局
        drawerListView = (ListView) findViewById(R.id.ting_drawer_list);  //侧滑布局中的list菜单组件
        final DrawerListAdapter adapter = new DrawerListAdapter(this, R.layout.item_navigation_list, initDrawerList());
        drawerListView.setAdapter(adapter);
        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                    	getSupportFragmentManager().beginTransaction().hide(fragmentLocalMusic).show(fragmentXiMaLaYa).commit();
                        mDrawerLayout.closeDrawer(drawerLayout);
                        break;
                    case 1:
						getSupportFragmentManager().beginTransaction().hide(fragmentXiMaLaYa).show(fragmentLocalMusic).commit();
                        mDrawerLayout.closeDrawer(drawerLayout);
                        break;
                    case 2:
                    	//getSupportFragmentManager().beginTransaction().replace(R.id.container, new Fragment()).commit();
                        mDrawerLayout.closeDrawer(drawerLayout);
                        break;
                    case 3:
                        mDrawerLayout.closeDrawer(drawerLayout);
                        break;
                }
                adapter.setSelectedPosition(position);
                adapter.notifyDataSetChanged();
            }
        });
    }

    public List<DrawerListBean> initDrawerList(){
        List<DrawerListBean> drawerList = new ArrayList<DrawerListBean>();
        
        DrawerListBean drawerListBean = new DrawerListBean(getResources().getString(R.string.title_ximalaya), R.drawable.ting_bg_selector);
        drawerList.add(drawerListBean);
        
        DrawerListBean drawerListBean1 = new DrawerListBean(getResources().getString(R.string.title_localmusic), R.drawable.music_bg_selector);
        drawerList.add(drawerListBean1);
        
        DrawerListBean drawerListBean2 = new DrawerListBean(getResources().getString(R.string.title_myshare), R.drawable.share_bg_selector);
        drawerList.add(drawerListBean2);
        
        DrawerListBean drawerListBean3 = new DrawerListBean(getResources().getString(R.string.title_myfavorie), R.drawable.like_bg_selector);
        drawerList.add(drawerListBean3);
        
        return drawerList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	
    	Log.i(TAG, "MainActivity onDestroy");
    }
}
