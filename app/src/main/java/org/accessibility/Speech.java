package org.accessibility;

import java.util.ArrayList;

import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class Speech
{
	private TextToSpeech tts;
	private Activity owner;
	private ArrayList<String> hold;
	private boolean isInitialized = false;
	
	public Speech(Activity owner)
	{
		System.out.println("create");
		this.owner = owner;
		hold = new ArrayList<String>();
		tts = new TextToSpeech(owner, new OnInitListener()
		{
			public void onInit(int status) 
			{
				System.out.println("initialize speech");
				if (status == TextToSpeech.SUCCESS) 
				{
					setIsInitialized(true);
					noConversionSpeak("Ready");
					for (String str : hold)
						speak(str);
				}
	}
		});
	}
	
	private void setIsInitialized(boolean isInitialized)
	{
		this.isInitialized	= isInitialized;
	}
		
	private void noConversionSpeak(String speak)
		{
			if ((isInitialized)	&&  (speak != null)) tts.speak(speak, TextToSpeech.QUEUE_ADD, null);
			else if (speak != null)
				hold.add(speak);
		}

	public void speak(String speak)
	{
		System.out.println("not good "+speak);
		noConversionSpeak(speak);
	}
	
	public void processView(View v)
	{
		if (ViewGroup.class.isAssignableFrom(v.getClass()))
		{
			ViewGroup parent = (ViewGroup)v;
			View child = null;
			for (int i = 0, numChildren = parent.getChildCount();	i < numChildren; i++)
			{
				child = parent.getChildAt(i);
				System.out.println("child "+child.getClass());
				if (child.getClass().equals(TextView.class)) speak(((TextView)child).getText().toString());
				else 				if (child.getClass().equals(Button.class)) speak(((Button)child).getText().toString());
				else if (ViewGroup.class.isAssignableFrom(child.getClass())) processView(child);
			}
		}
	}

}