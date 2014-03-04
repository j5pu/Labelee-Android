package com.example.myqrscanner;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Esta clase representa una fuente de texto
 *
 */
public abstract class Fuente {

	
	/**
	 * Cambia la fuente de una vista a cartogothic.ttf
	 * @param group Vista 
	 */
	public static void cambiaFuente(ViewGroup group) {
		Typeface typeFace=Typeface.createFromAsset(group.getContext().getAssets(), "fonts/labelee.ttf");
	    int count = group.getChildCount();
	    View v;
	    for(int i = 0; i < count; i++) {
	        v = group.getChildAt(i);
	        if(v instanceof TextView || v instanceof Button /*etc.*/)
	            ((TextView)v).setTypeface(typeFace);
	        else if(v instanceof ViewGroup)
	            cambiaFuente((ViewGroup)v);
	    }
	}
}
