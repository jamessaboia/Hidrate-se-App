package com.jamessaboia.hidrate_se;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button btnNotify;
    private EditText editMinutes;
    private TimePicker timePicker;

    private int hour;
    private int minute;
    private int interval;
    private String sInterval;

    private boolean activated = false;

    // criando variavel do tipo "SharedPreferences" pra guardar dados pequenos
    private SharedPreferences storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnNotify = findViewById(R.id.btn_notify);
        editMinutes = findViewById(R.id.edit_txt_number_interval);
        timePicker = findViewById(R.id.time_picker);

        // definindo o formato padrão do timePicker para 24hrs
        timePicker.setIs24HourView(true);

        //o modo privado não permite que nenhum outro app consiga acessar os dados que serão armazenados
        storage = getSharedPreferences("db", Context.MODE_PRIVATE);

        // chamando a chave responsavel por trazer as informações no banco, a "activated".
        // atribuímos o valor FALSE, pois ainda não temos informações salvas.
        activated =  storage.getBoolean("activated", false);

        if (activated) {
            btnNotify.setText(R.string.pause);
            int color = ContextCompat.getColor(this, android.R.color.black);
            btnNotify.setBackgroundColor(color);

            int interval = storage.getInt("interval", 0);
            int hour = storage.getInt("hour", timePicker.getCurrentHour());
            int minute = storage.getInt("minute", timePicker.getCurrentMinute());

            editMinutes.setText(String.valueOf(interval));
            timePicker.setCurrentHour(hour);
            timePicker.setCurrentMinute(minute);
        }

    }

    public void notifyClick(View view) {

        sInterval = editMinutes.getText().toString();

        // validador de formulário
        if (sInterval.isEmpty()) {
            Toast.makeText(this, R.string.error_msg, Toast.LENGTH_LONG).show();
            return;
        }

        hour = timePicker.getCurrentHour();
        minute = timePicker.getCurrentMinute();
        // passando o conteudo da varivel sInterval (String) para int, e amazenando-a na variavel interval
        interval = Integer.parseInt(sInterval);

        if (!activated) {
            btnNotify.setText(R.string.pause);
            int color = ContextCompat.getColor(this, android.R.color.black);
            btnNotify.setBackgroundColor(color);

            // usando a classe "SharedPreferences" p/ salvar e armazenar os dados...
            // abaixo. O "edtior" permite que seja escrito qualquer coisa no banco de dados
            SharedPreferences.Editor editor =  storage.edit();
            editor.putBoolean("activated", true);
            editor.putInt("interval", interval);
            editor.putInt("hour", hour);
            editor.putInt("minute", minute);
            editor.apply();

            Intent notificationIntent = new Intent(MainActivity.this, NotificationPublisher.class);
            notificationIntent.putExtra(NotificationPublisher.KEY_NOTIFICATION_ID, 1);
            notificationIntent.putExtra(NotificationPublisher.KEY_NOTIFICATION, "Hora de beber água");

            PendingIntent broadcast = PendingIntent.getBroadcast(MainActivity.this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            long futureInMillis = SystemClock.elapsedRealtime() + (interval * 1000);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, broadcast);

            activated = true;
        }   else {
            // quando o usuario clicar em pausar o texto e a cor irão se manter/retornar no padrão de antes
                btnNotify.setText(R.string.notify);
                int color = ContextCompat.getColor(this, R.color.colorAccent);
                btnNotify.setBackgroundColor(color);

            // fazendo a logica inversa, pois vamos apagar o banco de dados...
            SharedPreferences.Editor editor =  storage.edit();
            editor.putBoolean("activated", false);
            editor.remove("interval");
            editor.remove("hour");
            editor.remove("minute");
            editor.apply();

            activated = false;
            }

        Log.d("Teste", "hora: " + hour + ";" + " minuto: " + minute + ";" + " intervalo: " + interval);

    }

}