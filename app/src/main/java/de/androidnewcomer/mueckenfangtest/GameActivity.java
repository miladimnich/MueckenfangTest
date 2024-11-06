package de.androidnewcomer.mueckenfangtest;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Date;
import java.util.Random;

public class GameActivity extends AppCompatActivity implements View.OnClickListener, Runnable {
    private boolean spilLaeuft;
    private int runde;
    private int punkte;
    private int muecken;
    private int gefangenMueken;
    private int zeit;
    private float massstab;
    private Random zufallsgenerator = new Random();

    private ViewGroup spielbereich;  // Class-level variable to hold the reference to spielbereich
    private static final long HOECHSTALTER_MS = 2000;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.game);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                }
        );
        spielbereich = (ViewGroup) findViewById(R.id.spielbereich);


        spielStarten();
    }

    private void spielStarten() {
        spilLaeuft = true;
        runde = 0;
        punkte = 0;
        starteRunde();
    }

    private void starteRunde() {
        runde += 1; // Increment round
        muecken = runde * 10; // Mücken (mosquitoes) increase with each round
        gefangenMueken = 0; // Reset caught mosquitoes count
        zeit = 60; // Reset timer (assuming 60 seconds per round)
        bildschrimAktualisieren();
        handler.postDelayed(this, 1000);


    }

    private void bildschrimAktualisieren() {

        TextView tvPunkte = (TextView) findViewById(R.id.points);
        tvPunkte.setText(Integer.toString(punkte));
        TextView tvRunde = (TextView) findViewById(R.id.round);
        tvRunde.setText(Integer.toString(runde));
        TextView tvTreffer = findViewById(R.id.hits);
        tvTreffer.setText(Integer.toString(gefangenMueken));
        TextView tvZeit = findViewById(R.id.time);
        tvZeit.setText(Integer.toString(zeit));
        FrameLayout flTreffer = findViewById(R.id.bar_hits);
        FrameLayout flZeit = findViewById(R.id.bar_time);
        massstab = getResources().getDisplayMetrics().density;
        ViewGroup.LayoutParams lpTreffer = flTreffer.getLayoutParams();
        lpTreffer.width = Math.round(massstab * 300 * Math.min(gefangenMueken, muecken) / muecken);
        ViewGroup.LayoutParams lpZeit = flZeit.getLayoutParams();
        lpZeit.width = Math.round(massstab * zeit * 300 / 60);
    }

    private void zeitHerunterzaehlen() {

        zeit = zeit - 1;
        float zufallszahl = zufallsgenerator.nextFloat();
        double wahrscheinlichkeit = muecken * 1.5;
        if (wahrscheinlichkeit > 1) {
            eineMueckeAnzeigen();
            if (zufallszahl < wahrscheinlichkeit - 1) {
                eineMueckeAnzeigen();
            }
        } else {
            if (zufallszahl < wahrscheinlichkeit) {
                eineMueckeAnzeigen();
            }
        }
        mueckenVerschwinden();
        bildschrimAktualisieren();
        if (!pruefeSpielende()) {
            if (!pruefeRundenende()) {
                handler.postDelayed(this, 1000);

            }

        }

    }

    private boolean pruefeRundenende() {
        if (gefangenMueken >= muecken) {
            starteRunde();
            return true;
        }
        return false;
    }

    private boolean pruefeSpielende() {
        if (zeit == 0 && gefangenMueken < muecken) {
            gameOver();
            return true;
        }
        return false;
    }


    private void gameOver() {

        Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.gameover);

        dialog.show();
        spilLaeuft = false;

    }

    private void mueckenVerschwinden() {
        int nummer = 0;

        while (nummer < spielbereich.getChildCount()) {
            ImageView muecke = (ImageView) spielbereich.getChildAt(nummer);
            Date geburtsdatum = (Date) muecke.getTag(R.id.geburtsdatum);
            long alter = (new Date().getTime() - geburtsdatum.getTime());
            if (alter > HOECHSTALTER_MS) {
                spielbereich.removeView(muecke);
            } else {
                nummer++;
            }
        }

    }

    private void eineMueckeAnzeigen() {
        int breite = spielbereich.getWidth();
        int hoehe = spielbereich.getHeight();


        int muecke_breite = Math.round(massstab * 50); // Assuming 50 is the width of your mosquito image
        int muecke_hoehe = Math.round(massstab * 42); // Assuming 42 is the height of your mosquito image


        int links = zufallsgenerator.nextInt(breite - muecke_breite);
        int oben = zufallsgenerator.nextInt(hoehe - muecke_hoehe);

        ImageView muecke = new ImageView(this);
        muecke.setImageResource(R.drawable.muecken); // Set your mosquito image

        // Set the OnClickListener for the mosquito
        muecke.setOnClickListener(this);

        // Set the layout parameters
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(muecke_breite, muecke_hoehe);
        params.leftMargin = links;
        params.topMargin = oben;
        params.gravity = Gravity.TOP + Gravity.LEFT;

        // Add the mosquito to the spielbereich
        spielbereich.addView(muecke, params);

        // Set a tag with the current date as birthdate
        muecke.setTag(R.id.geburtsdatum, new Date());

    }

    @Override
    public void onClick(View view) {
        gefangenMueken++;
        punkte += 100;
        bildschrimAktualisieren();
        spielbereich.removeView(view);


    }

    @Override
    public void run() {
        zeitHerunterzaehlen();

    }

}
