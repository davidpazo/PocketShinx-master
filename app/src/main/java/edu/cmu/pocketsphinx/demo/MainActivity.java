package edu.cmu.pocketsphinx.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);

        textToSpeech = new TextToSpeech(this, this);
        textToSpeech.setLanguage( new Locale( "spa", "ESP" ) );

        micro();

    }

    /**
     * Metodo usado en el boton del microfono
     * Este metodo usa la api de google para el reconocimiento de voz. Una vez con el intent
     * empieza la startActivityForRest donde se trata el text y se determina que accion quiere
     * realizar el usuario
     *
     *
     */
    public void micro() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // Indicamos el modelo de lenguaje para el intent
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Definimos el mensaje que aparecerá
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "¿Qué es lo que quieres hacer?");
        // Lanzamos la actividad esperando resultados
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    /**
     * Metodo onActivityResult() que trata el texto reonocido del usuario y define la accion
     * que quiere realizar el usuario y llama al metodo correspondiente
     *
     * @param requestCode codigo de la peticion
     * @param resultCode codigo de resultado
     * @param dat Intent con los datos del micro
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dat) {

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {

            ArrayList<String> matches = dat.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            String[] palabras = matches.get(0).split(" ");

            if (palabras[0].equals("llamar")) {

                action_llamar(palabras);

            } else if ( (palabras[0].equals("contar") && palabras[1].equals("chiste")) ||
                    ( palabras[0].equals("cuéntame") && palabras[2].equals("chiste")) ) {

                textToSpeech.speak(Methods.tell_a_joke(), 1, null, null);

            } else if ( palabras[1].equalsIgnoreCase("hora") ){

                textToSpeech.speak(Methods.time(), 1, null, null);

            } else {

                String frase = "";
                for (int i=0; i<palabras.length; i++)
                    frase += palabras[i] + " ";

                Toast.makeText(this, frase, Toast.LENGTH_LONG).show();

                textToSpeech.speak("Lo siento, no tengo esa orden registrada", 1, null, null);

            }

        }
    }

    /**
     * Metodo action_llamar() que realiza todas las operaciones necesarias para realizar
     * una llamada cuando esta es requerida por el usuario
     *
     * @param palabras arraylist con todas las palabras reconocidas por la api de google
     */
    private void action_llamar(String[] palabras) {

        if(palabras.length > 3) {
            for(int i = 3; i<palabras.length; i++){
                palabras[2] += " " + palabras[i];
            }
        }

        String number = Methods.findNumber(this.getApplicationContext(), palabras[2]);

        if (!number.equals("error")) {

            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + number));

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {

                // se deberia introducir un mecanismo de control
                //  dejo la estructura hecha
                if ( "" == "" ) {
                    startActivity(callIntent);
                } else {

                }

            } else {

                textToSpeech.speak("No he podido llamar a: " + palabras[2] +
                        " porque no me has dado los permisos para poder usar el telefono",
                        1, null, null);

            }
        } else {

            textToSpeech.speak("No encontré a: " + palabras[2] + " entre tus contactos",
                    1, null, null);

        }
    }

    @Override
    public void onInit( int status ) {

        if ( status == TextToSpeech.LANG_MISSING_DATA | status == TextToSpeech.LANG_NOT_SUPPORTED ){

            Toast.makeText(this, "ERROR LANG_MISSING_DATA | LANG_NOT_SUPPORTED", Toast.LENGTH_SHORT)
                    .show();

        }

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

}
