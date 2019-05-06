package br.com.bossini.usjt_ads3anmca_weatherforecast;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView weatherRecyclerView;
    private WeatherAdapter adapter;
    private List<Weather> previsoes;
    private EditText locationEditText;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestQueue = Volley.newRequestQueue(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        locationEditText = findViewById(R.id.locationEditText);

        weatherRecyclerView = findViewById(R.id.weatherRecyclerView);
        previsoes = new ArrayList<>();
        adapter = new WeatherAdapter(previsoes, this);

        weatherRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        weatherRecyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cidade = locationEditText.getEditableText().toString();
                String endereco = getString(
                        R.string.web_service_url,
                        cidade,
                        getString(R.string.api_key)
                );

                obtemPrevisoesV5(cidade);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void obtemPrevisoesV1(String endereco) {
        try {
            URL url = new URL(endereco);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String linha = null;
            StringBuilder resultado = new StringBuilder("");
            while ((linha = reader.readLine()) != null) {
                resultado.append(linha);
            }
            String json = resultado.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void obtemPrevisoesV2 (String endereco){
        new Thread ( ()->{
            try {
                URL url = new URL(endereco);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String linha = null;
                StringBuilder resultado = new StringBuilder("");
                while ((linha = reader.readLine()) != null) {
                    resultado.append(linha);
                }
                String json = resultado.toString();
                Toast.makeText(this, resultado.toString(), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void obtemPrevisoesV3 (String endereco){
        new Thread ( ()->{
            try {
                URL url = new URL(endereco);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String linha = null;
                StringBuilder resultado = new StringBuilder("");
                while ((linha = reader.readLine()) != null) {
                    resultado.append(linha);
                }
                String json = resultado.toString();
                runOnUiThread(() -> {
                    Toast.makeText(this, resultado.toString(), Toast.LENGTH_SHORT).show();
                    lidaComJSON(resultado.toString());
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    class ObtemPrevisoes extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... enderecos) {
            try {
                String endereco = getString(
                        R.string.web_service_url,
                        enderecos[0],
                        getString(R.string.api_key)
                );
                URL url = new URL(endereco);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder resultado = new StringBuilder("");
                String aux = null;
                while ((aux = reader.readLine()) != null)
                    resultado.append(aux);
                return resultado.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String resultado) {
            lidaComJSON(resultado);
        }
    }

    public void obtemPrevisoesV4(String endereco) {
        new ObtemPrevisoes().execute(endereco);
    }

    public void obtemPrevisoesV5 (String cidade){
        String url = getString(
                R.string.web_service_url,
                cidade,
                getString(R.string.api_key)
        );
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                (response)->{
                    //aqui vamos tratar o json, cujo nome é response nesse caso
                    previsoes.clear();
                    try{
                        JSONArray list = response.getJSONArray("list");
                        for (int i = 0; i < list.length(); i++){
                            JSONObject day = list.getJSONObject(i);
                            JSONObject main = day.getJSONObject("main");
                            JSONObject weather = day.getJSONArray("weather").getJSONObject(0);
                            previsoes.add (new Weather(day.getLong("dt"), main.getDouble("temp_min"),
                                    main.getDouble("temp_max"), main.getDouble ("humidity"),
                                    weather.getString("description"),weather.getString("icon")));
                        }
                        adapter.notifyDataSetChanged();
                    }
                    catch (JSONException e){
                        e.printStackTrace();
                    }
                },
                (error)->{
                    Toast.makeText(
                            MainActivity.this,
                            getString(R.string.connect_error) + ": " + error.getLocalizedMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );
        requestQueue.add(req);
    }

    private void dismissKeyboard (View view){
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),0);
    }

    public void lidaComJSON(String resultado) {
        previsoes.clear();
        try {
            JSONObject json = new JSONObject(resultado);
            JSONArray list = json.getJSONArray("list");
            for (int i = 0; i < list.length(); i++){
                JSONObject previsaoDaVez = list.getJSONObject(i);
                long dt = previsaoDaVez.getLong("dt");
                JSONObject main = previsaoDaVez.getJSONObject("main");
                double temp_min = main.getDouble("temp_min");
                double temp_max = main.getDouble("temp_max");
                double humidity = main.getDouble("humidity");
                JSONArray weather = previsaoDaVez.getJSONArray("weather");
                JSONObject unico = weather.getJSONObject(0);
                String description = unico.getString("description");
                String icon = unico.getString("icon");
                Weather w = new Weather(
                        dt,
                        temp_min,
                        temp_max,
                        humidity,
                        description,
                        icon
                );
                previsoes.add(w);
            }
            adapter.notifyDataSetChanged();
            dismissKeyboard(weatherRecyclerView);
        } catch (JSONException e) {
            Toast.makeText(this, getString(R.string.read_error), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
