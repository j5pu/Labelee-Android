package com.example.myqrscanner;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.mnopi.labelee.R;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

public class WebActivity extends SherlockActivity {

	private static final String URL_ORIGEN = "http://locmx.labelee.com/map/origin/32_89";

	private WebView wv_mapa;
	private SharedPreferences prefs;
	private ImageView mapa;

	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web);

		prefs = this.getSharedPreferences("com.mnopi.labelee",
				Context.MODE_PRIVATE);

		ActionBar actBar = getSupportActionBar();
		actBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		View view = getLayoutInflater().inflate(R.layout.action_bar, null);
		actBar.setCustomView(view);

		wv_mapa = (WebView) findViewById(R.id.wv_mapa);
		mapa = (ImageView) findViewById(R.id.img_mapa_coche);

		boolean coche_ubicado = prefs.getBoolean("coche_ubicado", false);
		
		// Tenemos el coche ubicado
		if (coche_ubicado) {
			mapa.setImageResource(R.drawable.coche);
		}
		// No tenemos el coche ubicado
		else {
			mapa.setImageResource(R.drawable.mapa);
		}

		if (isOnline(this)) {
			boolean qr_leido = getIntent().getBooleanExtra(
					MainActivity.QR_LEIDO, false);
			// Se ha leído un QR: mostramos la aplicación
			if (qr_leido) {
				String qr_code = getIntent().getStringExtra(
						MainActivity.QR_VALUE);
				WebSettings webSettings = wv_mapa.getSettings();
				webSettings.setJavaScriptEnabled(true);
				wv_mapa.loadUrl(qr_code);
			}
			// No se ha leído un QR: se ha pulsado el botón del mapa/coche
			else {
				coche_ubicado = prefs.getBoolean("coche_ubicado", false);
				// Tenemos el coche ubicado
				if (coche_ubicado) {
					mapa.setImageResource(R.drawable.coche);
					String qr_coche = prefs.getString("qr_coche", null);
					WebSettings webSettings = wv_mapa.getSettings();
					webSettings.setJavaScriptEnabled(true);
					wv_mapa.loadUrl(qr_coche);
				}
				// No tenemos el coche ubicado
				else {
					mapa.setImageResource(R.drawable.mapa);
					wv_mapa.loadUrl(URL_ORIGEN);
				}
			}

		} else {
			Toast.makeText(this, "No dispones de conexión a Internet",Toast.LENGTH_LONG).show();
		}

		mapa.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean coche_ubicado = prefs
						.getBoolean("coche_ubicado", false);
				if (coche_ubicado) {
					String qr_coche = prefs.getString("qr_coche", null);
					WebSettings webSettings = wv_mapa.getSettings();
					webSettings.setJavaScriptEnabled(true);
					wv_mapa.loadUrl(qr_coche);
				} else {
					wv_mapa.loadUrl(URL_ORIGEN);
				}
			}
		});
	}

	/**
	 * Comprueba si está conectado a internet
	 * 
	 * @param context
	 * @return
	 */
	private boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		wv_mapa.saveState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		wv_mapa.restoreState(savedInstanceState);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

}
