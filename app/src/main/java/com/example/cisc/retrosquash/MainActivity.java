package com.example.cisc.retrosquash;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.Random;

public class MainActivity extends Activity {

    Canvas canvas;
    SquashCourtView squashCourtView;

    private SoundPool soundPool;
    int sound1 = -1;
    int sound2 = -1;
    int sound3 = -1;
    int sound4 = -1;

    Display display;
    Point size;
    int screenWidth;
    int screenHeight;

    int racketWidth;
    int racketHeight;
    Point racketPosition;
    Point ballPosition;
    int ballWidth;

    //Ball movement
    boolean ballIsMovingLeft;
    boolean ballIsMovingRight;
    boolean ballIsMovingUp;
    boolean ballIsMovingDown;

    boolean racketIsMovingLeft;
    boolean racketIsMovingRight;

    // stats
    long lastFrameTime;
    int fps;
    int score;
    int lives;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        squashCourtView = new SquashCourtView(this);
        setContentView(squashCourtView);

        //load sounds
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        try {
            AssetManager assetManager = getAssets();
            AssetFileDescriptor descriptor;

            descriptor = assetManager.openFd("blip1.wav");
            sound1 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("blip2.wav");
            sound2 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("blip3.wav");
            sound3 = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("blip4.wav");
            sound4 = soundPool.load(descriptor, 0);
        } catch (IOException e) {
            // Maybe put toast to let user know something is not right
        }
        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        screenWidth  = size.x;
        screenHeight = size.y;

        //Game objects
        racketPosition = new Point();
        racketPosition.x = screenWidth/2;
        racketPosition.y = screenHeight - 20;
        racketWidth = screenWidth / 8;
        racketHeight = 10;

        ballWidth = screenWidth / 35;
        ballPosition = new Point();
        ballPosition.x = screenWidth/2;
        ballPosition.y = 1+ballWidth;

        lives = 3;

    } // End onCreate
class SquashCourtView extends SurfaceView implements Runnable {

    Thread ourThread = null;
    SurfaceHolder ourHolder;
    volatile boolean playingSquash;
    Paint paint;

    public SquashCourtView(Context context) {
        super(context);
        ourHolder = getHolder();
        paint = new Paint();
        ballIsMovingDown = true;

        //Send ball in random direction
        Random randomNumber = new Random();
        int ballDirection = randomNumber.nextInt(3);
        switch( ballDirection) {
            case 0:
                ballIsMovingLeft = true;
                ballIsMovingRight = false;
                break;
            case 1:
                ballIsMovingLeft = false;
                ballIsMovingRight = true;
                break;
            case 2:
                ballIsMovingLeft = false;
                ballIsMovingRight = false;
                break;
        } // End switch
    } // End public SquashCourtView

    @Override
        public void run() {
            while ( playingSquash) {
                updateCourt();
                drawCourt();
                controlFPS();
            }
    } // End public void run

    public void updateCourt() {
        if( racketIsMovingRight) {
            racketPosition.x = racketPosition.x + 10;
        }
        if( racketIsMovingLeft) {
            racketPosition.x = racketPosition.x - 10;
        }

        // Detect collisions

        //hit right of screen
        if( ballPosition.x + ballWidth > screenWidth) {
            ballIsMovingLeft = true;
            ballIsMovingRight = false;
            soundPool.play(sound1, 1,1,0,0,1);
        }
        // hit left of screen
        if( ballPosition.x < 0) {
            ballIsMovingLeft = false;
            ballIsMovingRight = true;
            soundPool.play(sound1, 1,1,0,0,1);
        }
        // hit bottom of screen
        if( ballPosition.y > screenHeight - ballWidth) {
            lives = lives -1;
            if( lives == 0)  {
                lives = 3;
                score = 0;
                soundPool.play(sound4,1,1,0,0,1);
            }
            ballPosition.y = 1 + ballWidth; // back to top of screen

            // choose new ball direction
            Random randomNumber = new Random();
            int startX = randomNumber.nextInt(screenWidth - ballWidth) + 1;
            ballPosition.x = startX + ballWidth;

            int ballDirection = randomNumber.nextInt(3);
            switch( ballDirection) {
                case 0: // move left
                    ballIsMovingLeft = true;
                    ballIsMovingRight = false;
                    break;
                case 1: // move right
                    ballIsMovingLeft = false;
                    ballIsMovingRight = true;
                    break;
                case 2: // not left or right
                    ballIsMovingLeft = false;
                    ballIsMovingRight = false;
                    break;

            } // End switch

        } // End hit bottom of screen
        // hit top of screen
        if( ballPosition.y <= 0) {
            ballIsMovingDown = true;
            ballIsMovingUp = false;
            ballPosition.y = 1;
            soundPool.play(sound2,1,1,0,0,1);
        }

        if( ballIsMovingDown) {
            ballPosition.y += 6;
        }
        if( ballIsMovingUp) {
            ballPosition.y -= 10;
        }
        if( ballIsMovingLeft) {
            ballPosition.x -= 12;
        }
        if( ballIsMovingRight) {
            ballPosition.x += 12;
        }

        // hit racket
        if( ballPosition.y + ballWidth >= (racketPosition.y - racketHeight/2)) {
            int halfRacket = racketWidth / 2;
            if( ballPosition.x + ballWidth > (racketPosition.x - halfRacket)
             && ballPosition.x - ballWidth < (racketPosition.x + halfRacket)) {
             // rebound the ball vertically
                soundPool.play(sound3,1,1,0,0,1);
                score++;
                ballIsMovingUp = true;
                ballIsMovingDown = false;
                //now decide how to rebound the ball horizontally
                if( ballPosition.x > racketPosition.x) {
                    ballIsMovingRight = true;
                    ballIsMovingLeft = false;
                }
                else {
                    ballIsMovingRight = false;
                    ballIsMovingLeft = true;
                }
            }
        }
    } // End public void updateCourt

    public void drawCourt() {
        if( ourHolder.getSurface().isValid()) {
            canvas = ourHolder.lockCanvas();
            //Paint paint = new Paint();
            canvas.drawColor(Color.BLACK); // the background
            paint.setColor(Color.argb(255,255,255,255));
            paint.setTextSize(45);
            canvas.drawText("Score: "+score+" Lives: "+lives + " fps: "+fps,20,40,paint);

            //Drew teh squash racket
            canvas.drawRect(racket)
        }
    }
} // End class SquashCourtView

} // End MainActivity
