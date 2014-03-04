package com.example.myqrscanner;

import java.util.Calendar;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.mnopi.labelee.R;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;
import android.widget.TextView;

public class MainActivity extends SherlockActivity {

	public static final String QR_VALUE = "Qr_value";
	public static final String QR_LEIDO = "Qr_leido";

	TextView scanText;
	Button scanButton;
	private ImageView mapa;

	private Camera mCamera;
	private CameraPreview mPreview;
	private Handler autoFocusHandler;
	private SharedPreferences prefs;
	ImageScanner scanner;
	private boolean barcodeScanned = false;
	private boolean previewing = true;
	private boolean coche_ubicado;

	static {
		System.loadLibrary("iconv");
	}

	/**
	 * onCreate
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// Cambiamos la fuente de la pantalla
		Fuente.cambiaFuente((ViewGroup) findViewById(R.id.ll_pantalla_qr));
		
		prefs = getBaseContext().getSharedPreferences("com.mnopi.labelee",
				Context.MODE_PRIVATE);

		ActionBar actBar = getSupportActionBar();
		actBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		View view = getLayoutInflater().inflate(R.layout.action_bar, null);
		actBar.setCustomView(view);

		mapa = (ImageView) findViewById(R.id.img_mapa_coche);
		
		coche_ubicado = prefs.getBoolean("coche_ubicado", false);
		
		// Tenemos el coche ubicado
		if (coche_ubicado) {
			mapa.setImageResource(R.drawable.coche);
		}
		// No tenemos el coche ubicado
		else {
			mapa.setImageResource(R.drawable.mapa);
		}
		

		mapa = (ImageView) findViewById(R.id.img_mapa_coche);
		mapa.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				barcodeScanned = true;
				// Mostramos la pantalla con el mapa
				Intent i = new Intent(MainActivity.this, WebActivity.class);
				i.putExtra(QR_LEIDO, false);
				startActivity(i);
			}
		});

		// Arrancamos el lector de QR
		if (isCameraAvailable()) {
			autoFocusHandler = new Handler();
			mCamera = getCameraInstance();

			// Instance barcode scanner
			scanner = new ImageScanner();
			scanner.setConfig(0, Config.X_DENSITY, 3);
			scanner.setConfig(0, Config.Y_DENSITY, 3);

			mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
			FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
			preview.addView(mPreview);

			scanText = (TextView) findViewById(R.id.scanText);
		} else {
			Toast.makeText(this, "Cámara no disponible", Toast.LENGTH_LONG).show();
		}

	}

	/**
	 * onResume
	 */
	public void onResume() {
		super.onResume();
		if (barcodeScanned) {
			barcodeScanned = false;
			// Arrancamos el lector de QR
			if (isCameraAvailable()) {
				autoFocusHandler = new Handler();
				mCamera = getCameraInstance();

				// Instance barcode scanner
				scanner = new ImageScanner();
				scanner.setConfig(0, Config.X_DENSITY, 3);
				scanner.setConfig(0, Config.Y_DENSITY, 3);

				mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
				FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
				preview.addView(mPreview);

				scanText = (TextView) findViewById(R.id.scanText);
			} else {
				Toast.makeText(this, "Cámara no disponible", Toast.LENGTH_LONG)
						.show();
			}
		}
		coche_ubicado = prefs.getBoolean("coche_ubicado", false);
		// Tenemos el coche ubicado
		if (coche_ubicado) {
			mapa.setImageResource(R.drawable.coche);
		}
		// No tenemos el coche ubicado
		else {
			mapa.setImageResource(R.drawable.mapa);
		}
	}

	/**
	 * onPause
	 */
	public void onPause() {
		super.onPause();
		barcodeScanned = true;
		releaseCamera();
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open();
		} catch (Exception e) {
		}
		return c;
	}

	/**
	 * MÉTODOS DE LA CÁMARA
	 */
	private void releaseCamera() {
		if (mCamera != null) {
			previewing = false;
			mPreview.getHolder().removeCallback(mPreview);
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}

	private Runnable doAutoFocus = new Runnable() {
		public void run() {
			if (previewing)
				mCamera.autoFocus(autoFocusCB);
		}
	};

	PreviewCallback previewCb = new PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			Camera.Parameters parameters = camera.getParameters();
			Size size = parameters.getPreviewSize();

			Image barcode = new Image(size.width, size.height, "Y800");
			barcode.setData(data);

			int result = scanner.scanImage(barcode);

			if (result != 0) {
				previewing = false;
				mCamera.setPreviewCallback(null);
				mCamera.stopPreview();

				SymbolSet syms = scanner.getResults();
				for (Symbol sym : syms) {
					// Comprobamos si es una plaza de parking
					String qr_leido = sym.getData();
					String ultima_letra = qr_leido
							.substring(qr_leido.length() - 1);
					// Parking
					if (ultima_letra.equals("P")) {
						// Guardamos el qr de donde está el coche
						prefs.edit().putString("qr_coche", qr_leido).commit();
						// Indicamos que tenemos el coche guardado
						prefs.edit().putBoolean("coche_ubicado", true).commit();
						// Activamos la alarma para que se desactive la plaza del coche a las 24 horas
						AlarmManager alarmaCoche = (AlarmManager)getSystemService(ALARM_SERVICE);
						Intent myIntent = new Intent(MainActivity.this,DesactivaCocheService.class);
						PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 354, myIntent,0);
						Calendar calendar = Calendar.getInstance();
						calendar.setTimeInMillis(System.currentTimeMillis());
						calendar.add(Calendar.SECOND, 24 * 60 * 60);
						alarmaCoche.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(), pendingIntent);
					}

					else {
						// Indicamos que no tenemos el coche guardado
						prefs.edit().putBoolean("coche_ubicado", false)
								.commit();
					}

					// Mostramos la pantalla con la aplicación
					Intent i = new Intent(MainActivity.this, WebActivity.class);
					i.putExtra(QR_LEIDO, true);
					i.putExtra(QR_VALUE, qr_leido);
					startActivity(i);
					barcodeScanned = true;
				}
			}
		}
	};

	// Mimic continuous auto-focusing
	AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};

	public boolean isCameraAvailable() {
		PackageManager pm = getPackageManager();
		return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}
}