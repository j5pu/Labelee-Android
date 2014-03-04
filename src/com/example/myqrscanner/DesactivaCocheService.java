package com.example.myqrscanner;


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class DesactivaCocheService extends IntentService {

	static AlarmManager alarmaCoche;
	SharedPreferences prefs;


	public DesactivaCocheService() {
		super(DesactivaCocheService.class.getSimpleName());
	}

	@Override
	protected void onHandleIntent(Intent arg0) {

		// Desactivamos la alarma
		alarmaCoche = (AlarmManager) this
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(this, DesactivaCocheService.class);
		PendingIntent pi = PendingIntent.getService(this, 354, i,
				PendingIntent.FLAG_NO_CREATE);

		if (pi != null) { // nos aseguramos de que esta activa la alarma: comprobamos si existe el pending intent
			alarmaCoche.cancel(pi);
		}
		
		// Lo quitamos de las preferencias
		prefs = this.getSharedPreferences("com.mnopi.labelee", Context.MODE_PRIVATE);
		prefs.edit().putBoolean("coche_ubicado", false).commit();
	}

}
