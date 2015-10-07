package org.accessibility;

import java.util.ArrayList;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.accessibility.AccessibilityEvent;

public class BenetechAccessibilityService extends AccessibilityService
{
	private static TextToSpeech tts;
	private static boolean isInitialized = false;
	private static ArrayList<String> hold = new ArrayList<String>();
	private static String information;
		
	public static void setActivity(Activity owner) 
	{
		tts = new TextToSpeech(owner, new OnInitListener()
		{
			public void onInit(int status)  
			{
				System.out.println("initialize speech");
				if (status == TextToSpeech.SUCCESS) 
				{
					isInitialized = true;
					noConversionSpeak("Ready");
					checkHeld();
				}
			}
		});
		}
	
	private static void checkHeld()
	{
		if (hold.isEmpty()) return;
			for (String speak:hold)
				process(speak);
			hold.clear();
}
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent e) 
	{
		
	}

	public static void onReceiveAccessibilityEvent(Object message)
	{
		process(message);
}
	
	private static void process(Object message)
	{
		if (message.getClass().equals(String.class)) information = (String)message;
		else if (message.getClass().equals(char[].class)) information = new String((char[])message);
		else information = message.toString();
		if (isReady()) speak();
	}
	
	private static boolean isReady()
	{
			if ((tts == null) || (!isInitialized))
			{
				if (!hold.contains(new String(information.toCharArray()))) 
hold.add(new String(information.toCharArray()));
				return false;
			}
			return true;
		}
	
	@Override
	public void onInterrupt()
	{
		
	}

	@Override
	protected void onServiceConnected() 
	{
		super.onServiceConnected();
	}
	
	private static void noConversionSpeak(String speak)
	{
		tts.speak(speak, TextToSpeech.QUEUE_ADD, null);
	}

public static void speak()
{
	noConversionSpeak(information);
}

}