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
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;

public class MainActivity extends Activity {

    Canvas canvas;
    SquashCourtView squashCourtView;

    private SoundPool soundPool;
    int soundNewBall    = -1;
    int soundMissedBall = -1;
    int soundBounceWall = -1;
    int soundpaddle = -1;


    Display display;
    Point size;
    int screenWidth;
    int screenHeight;

    int racket1Width, racket1Height;
    int racket2Width, racket2Height;
    Point racket1Position,racket2Position;
    Point ballPosition;
    int ballWidth;

    //Ball movement
    boolean ballIsMovingLeft;
    boolean ballIsMovingRight;
    boolean ballIsMovingUp;
    boolean ballIsMovingDown;

    boolean racket1IsMovingLeft, racket1IsMovingRight;
    boolean racket2IsMovingLeft, racket2IsMovingRight;

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

            descriptor = assetManager.openFd("newball.wav");
            soundNewBall = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("missedball.wav");
            soundMissedBall = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("bouncewall.wav");
            soundBounceWall = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("paddle.wav");
            soundpaddle = soundPool.load(descriptor, 0);
        } catch (IOException e) {
            // Maybe put toast to let user know something is not right
            Toast.makeText(getApplicationContext(),"Trouble loading sounds.",Toast.LENGTH_LONG).show();
        }
        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        screenWidth  = size.x;
        screenHeight = size.y;

        //Game objects
        racket1Position = new Point();
        racket1Position.x = screenWidth/2;
        racket1Position.y = screenHeight - 20;
        racket1Width = screenWidth / 8;
        racket1Height = 10;

        racket2Position = new Point();
        racket2Position.x = screenWidth/2;
        racket2Position.y = 40;
        racket2Width = screenWidth / 8;
        racket2Height = 10;


        ballWidth = screenWidth / 35;
        ballPosition = new Point();
        ballPosition.x = screenWidth/2;
        ballPosition.y = screenHeight/2;

        soundPool.play(soundNewBall,1,1,0,0,1);

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

        // Update both racket positions
        if( racket1IsMovingRight) {
            racket1Position.x = racket1Position.x + 10;
        }
        if( racket1IsMovingLeft) {
            racket1Position.x = racket1Position.x - 10;
        }
        if( racket2IsMovingRight) {
            racket2Position.x = racket2Position.x + 10;
        }
        if( racket2IsMovingLeft) {
            racket2Position.x = racket2Position.x - 10;
        }

        // Detect collisions

        //hit right of screen
        if( ballPosition.x + ballWidth > screenWidth) {
            ballIsMovingLeft = true;
            ballIsMovingRight = false;
            soundPool.play(soundBounceWall, 1,1,0,0,1);
        }
        // hit left of screen
        if( ballPosition.x < 0) {
            ballIsMovingLeft = false;
            ballIsMovingRight = true;
            soundPool.play(soundBounceWall, 1,1,0,0,1);
        }
        // hit bottom or top of screen
        if(( ballPosition.y > screenHeight - ballWidth) ||
           ( ballPosition.y < ballWidth) ) {
            lives = lives -1;
            soundPool.play(soundMissedBall,1,1,0,0,1);
/*
            try {
                ourThread.wait(250L);
            }catch (InterruptedException e){
                //Nothing here
            }
*/
            soundPool.play(soundNewBall,1,1,0,0,1);
            if( lives == 0)  {
                lives = 3;
                score = 0;
            }
            ballPosition.x = screenWidth/2;
            ballPosition.y = screenHeight/2;

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

        // Update ball position
        if( ballIsMovingDown) {
            ballPosition.y += 12;
        }
        if( ballIsMovingUp) {
            ballPosition.y -= 12;
        }
        if( ballIsMovingLeft) {
            ballPosition.x -= 12;
        }
        if( ballIsMovingRight) {
            ballPosition.x += 12;
        }

        // hit racket1 at bottom of screen
        if( ballPosition.y + ballWidth >= (racket1Position.y - racket1Height/2)) {
            int halfRacket1 = racket1Width / 2;
            if( ballPosition.x + ballWidth > (racket1Position.x - halfRacket1)
             && ballPosition.x - ballWidth < (racket1Position.x + halfRacket1)) {
             // rebound the ball vertically
                soundPool.play(soundpaddle,1,1,0,0,(float)0.5);
                score++;
                ballIsMovingUp = true;
                ballIsMovingDown = false;
                //now decide how to rebound the ball horizontally
                if( ballPosition.x > racket1Position.x) {
                    ballIsMovingRight = true;
                    ballIsMovingLeft = false;
                }
                else {
                    ballIsMovingRight = false;
                    ballIsMovingLeft = true;
                }
            }
        } // End hit racket1

        // hit racket2
        if( ballPosition.y - ballWidth <= (racket2Position.y + racket2Height/2)) {
            int halfRacket2 = racket2Width / 2;
            if( ballPosition.x + ballWidth > (racket2Position.x - halfRacket2)
                    && ballPosition.x - ballWidth < (racket2Position.x + halfRacket2)) {
                // rebound the ball vertically
                soundPool.play(soundpaddle,1,1,0,0,(float)0.5);
                score++;
                ballIsMovingUp = false;
                ballIsMovingDown = true;
                //now decide how to rebound the ball horizontally
                if( ballPosition.x > racket2Position.x) {
                    ballIsMovingRight = true;
                    ballIsMovingLeft = false;
                }
                else {
                    ballIsMovingRight = false;
                    ballIsMovingLeft = true;
                }
            }
        } // End hit racket2

    } // End public void updateCourt

    public void drawCourt() {
        if( ourHolder.getSurface().isValid()) {
            canvas = ourHolder.lockCanvas();
            //Paint paint = new Paint();
            canvas.drawColor(Color.BLACK); // the background
            paint.setColor(Color.argb(255, 255, 255, 255));
            paint.setTextSize(45);
            canvas.drawText("Score: " + score + " Lives: " + lives + " fps: " + fps, 20, 40, paint);

            //Draw the squash racket1
            paint.setColor(Color.argb(255, 25, 255, 255));
            canvas.drawRect(racket1Position.x - (racket1Width / 2),  // Left
                            racket1Position.y - (racket1Height / 2), // Top
                            racket1Position.x + (racket1Width / 2),  // Right
                            racket1Position.y +  racket1Height,      // Bottom
                            paint);                                  // Bitmap

            //Draw the squash racket2
            paint.setColor(Color.argb(255, 255, 25, 255));
            canvas.drawRect(racket2Position.x - (racket2Width / 2),
                            racket2Position.y - (racket2Height / 2),
                            racket2Position.x + (racket2Width / 2),
                            racket2Position.y + racket2Height,
                            paint);

            // draw the ball
            paint.setColor(Color.argb(255, 255, 255, 25));
            canvas.drawRect(ballPosition.x,
                            ballPosition.y,
                            ballPosition.x + ballWidth,
                            ballPosition.y + ballWidth,
                            paint);

            ourHolder.unlockCanvasAndPost(canvas);
        }
    } // End drawCourt()

    public void controlFPS() {
        long timeThisFrame = (System.currentTimeMillis() - lastFrameTime);
        long timeToSleep = 15 - timeThisFrame;
        if(timeThisFrame > 0 ) {
            fps = (int) (1000/timeThisFrame);
        }
        if( timeToSleep > 0 ) {
            try {
                ourThread.sleep(timeToSleep);

            }
            catch (InterruptedException e) {
                // Nothing here
            }
        }

        lastFrameTime = System.currentTimeMillis();
    } // End controlFPS

    public void pause() {
        playingSquash = false;
        try {
            ourThread.join();
        }
        catch (InterruptedException e) {
            // Nothing here
        }
    } // End pause()

    public void resume() {
        playingSquash = true;
        ourThread = new Thread(this);
        ourThread.start();
    } // End resume()

    //Fixed racket1 control relative to the racket NOT the center of the screen
    @Override
    public boolean onTouchEvent( MotionEvent motionEvent) {
        switch( motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN: // Start touching the screen
                if( motionEvent.getY() >= screenHeight/2) { // Check for action of racket1
                    if (motionEvent.getX() >= racket1Position.x + (racket1Width / 2)) {
                        racket1IsMovingRight = true;
                        racket1IsMovingLeft = false;
                    } else {
                        racket1IsMovingLeft = true;
                        racket1IsMovingRight = false;
                    }
                }
                else { // Check for action of racket2
                    if (motionEvent.getX() >= racket2Position.x + (racket2Width / 2)) {
                        racket2IsMovingRight = true;
                        racket2IsMovingLeft = false;
                    } else {
                        racket2IsMovingLeft = true;
                        racket2IsMovingRight = false;
                    }
                }
                break;

            case MotionEvent.ACTION_UP: // Stopped touching the screen
                racket1IsMovingRight = false;
                racket1IsMovingLeft = false;
                racket2IsMovingRight = false;
                racket2IsMovingLeft = false;
                break;
        } // End switch
        return true;
    } // End onTouchEvent

} // End class SquashCourtView

    @Override
    protected void onStop() {
        super.onStop();
        while(true) {
            squashCourtView.pause();
            break;
        }
        finish();
    } // end onStop()

    @Override
    protected void onPause() {
        super.onPause();
        squashCourtView.pause();
    } // End onPause

    @Override
    protected void onResume() {
        super.onResume();
        squashCourtView.resume();
    } // End onResume

    public boolean onKeyDown( int keyCode, KeyEvent event) {
        if( keyCode == KeyEvent.KEYCODE_BACK) {
            squashCourtView.pause();
            finish();
            return true;
        }
        return false;
    } // End onKeyDown

} // End MainActivity
