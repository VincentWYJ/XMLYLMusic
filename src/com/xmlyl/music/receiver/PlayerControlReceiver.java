package com.xmlyl.music.receiver;

import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.xmlyl.music.constants.Constants;
import com.xmlyl.music.fragment.LocalMusicFragment;
import com.xmlyl.music.util.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PlayerControlReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		XmPlayerManager manager = XmPlayerManager.getInstance(context);
		String action = intent.getAction();
		if (Constants.ACTION_CONTROL_PLAY_PAUSE.equals(action))
		{
			if (manager.isPlaying())
			{
				manager.pause();
			}
			else
			{
				manager.play();
			}
		}
		else if (Constants.ACTION_CONTROL_PLAY_NEXT.equals(action))
		{
			manager.playNext();
		}
		else if (Constants.ACTION_CONTROL_PLAY_PRE.equals(action))
		{
			manager.playPre();
		}
		else if(!Utils.isMusicInfoEmpty()){
			int size = LocalMusicFragment.musicInfo.size();
			if (Utils.ACTION_CONTROL_PLAY_PAUSE.equals(action)){
				LocalMusicFragment.actionPauseOrPlay();
			}else if (Utils.ACTION_CONTROL_PLAY_NEXT.equals(action)){
				LocalMusicFragment.MusicPlay((LocalMusicFragment.positionPlay+1)%size);
			}else if (Utils.ACTION_CONTROL_PLAY_PRE.equals(action)){
				LocalMusicFragment.MusicPlay((size+LocalMusicFragment.positionPlay-1)%size);
			}
		}
	}

}

