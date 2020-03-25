package online.menso.high_loweyeplayed;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import online.menso.high_loweyeplayed.cardsgame.CardGame;
import online.menso.high_loweyeplayed.cardsgame.CardImage;
import online.menso.high_loweyeplayed.mlkitsamples.common.CameraSource;
import online.menso.high_loweyeplayed.mlkitsamples.common.CameraSourcePreview;
import online.menso.high_loweyeplayed.mlkitsamples.common.GraphicOverlay;
import online.menso.high_loweyeplayed.mlkitsamples.facedetection.FaceDetectionProcessor;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, Handler.Callback {



    /**
     * Handler message codes
     */
    public static final int FACE_FEATURES_STATUS = 1;
    public static final int LOWER_CARD_MESSAGE = 2;
    public static final int HIGHER_CARD_MESSAGE = 3;
    public static final int CARD_GUESS_RIGHT = 4;
    public static final int CARD_GUESS_WRONG = 5;
    public static final int CARD_GUESS_EQUAL = 6;
    public static final int HEAD_ORIENTATION_X =7;
    public static final int HEAD_ORIENTATION_Y= 8;

    private static final String TAG = "HighLowMainActivity";

    /**
     * ML Kit sample variables
     */
    private static final int PERMISSION_REQUESTS = 1;
    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;

    /**
     * Timer for gestures
     */

    long _start_time;
    boolean timerStarted = false;

    /**
     * CardGame vars
      */

    CardGame cardGame;
    TextView streak_number, long_streak ,game_result,cards_left,higher_choice, lower_choice, left_eye_status, right_eye_status, mouth_status, head_orientation1, head_orientation2;
    ImageView card_imageview;


    LottieAnimationView main_animation;
    boolean gameActive = false;


    public static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        handler = new Handler(this);
        initViews();
        try {
            getSupportActionBar().hide();
        }catch (Exception e){
            e.printStackTrace();
        }
        preview = findViewById(R.id.firePreview);
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }

        graphicOverlay = findViewById(R.id.fireFaceOverlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }


        if (allPermissionsGranted()) {
            createCameraSource();
        } else {
            getRuntimePermissions();
        }

        game_result.setText("SMILE TO START GAME!");
    }


    private void initViews(){

        // Initialize all views from main layout (activity_main)

        streak_number =  findViewById(R.id.streak_number);
        long_streak = findViewById(R.id.long_streak);
        game_result = findViewById(R.id.game_result);
        cards_left = findViewById(R.id.cards_left);
        higher_choice = findViewById(R.id.higher_choice);
        lower_choice = findViewById(R.id.lower_choice);
        card_imageview = findViewById(R.id.card_imageview);
        main_animation = findViewById(R.id.main_animation);
        left_eye_status = findViewById(R.id.left_eye_status);
        right_eye_status = findViewById(R.id.right_eye_status);
        mouth_status = findViewById(R.id.mouth_status);
        head_orientation1 = findViewById(R.id.head_orientation1);
        head_orientation2 = findViewById(R.id.head_orientation2);
    }


    private boolean isTimeRightSmile(){

        long elapsedMilliSeconds = System.currentTimeMillis() - _start_time;
        if(elapsedMilliSeconds > 1500){
            timerStarted = false;
            return true;
        } else
            return false;
    }

    private void checkRightEyeStatus(double data){

        if(data > 0.20)
            left_eye_status.setText("Left Eye: OPEN");
        else{
            playLower();
            left_eye_status.setText("Left Eye: CLOSED");
        }

    }

    private void checkLeftEyeStatus (double data){
        if(data > 0.20)
            right_eye_status.setText("Right Eye: OPEN");
        else {
            playHigher();
            right_eye_status.setText("Right Eye: CLOSED");
        }
    }

    private void checkMouthStatus(double data){
        if(data > 0.50) {
            mouth_status.setText("Mouth: SMILE");

            if(cardGame == null){
                if(!timerStarted){
                    _start_time =  System.currentTimeMillis();
                    timerStarted = true;
                }
                if(isTimeRightSmile()){
                    cardGame = new CardGame(1);
                    game_result.setText("Game starting...");
                    timerStarted = false;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startGame();
                            game_result.setText("");

                        }
                    },800);

                }

            }else if(!cardGame.getGameIsActive()){
                cardGame = new CardGame(1);
                game_result.setText("Game starting...");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startGame();
                        game_result.setText("");
                    }
                },1000);
            }
        } else
            timerStarted = false;
            mouth_status.setText("Mouth: SERIOUS");

}

    @Override
    public boolean handleMessage(Message message) {

        switch (message.what){
            case FACE_FEATURES_STATUS:
                try {

                    checkLeftEyeStatus(((double[]) message.obj)[0]);
                    checkRightEyeStatus(((double[]) message.obj)[1]);
                    checkMouthStatus(((double[]) message.obj)[2]);

                }catch (Exception e){
                    e.printStackTrace();
                }

                break;
            case HEAD_ORIENTATION_X:

                if(((float) message.obj) > 20)
                    head_orientation1.setText("F.rotation: CW");
                else if(((float) message.obj) < -20){
                    head_orientation1.setText("F.rotation: CCW");
                }else {
                    head_orientation1.setText("F.rotation: Leveled");
                }
                break;

            case HEAD_ORIENTATION_Y:

                if(((float) message.obj) > 29) {
                    head_orientation2.setText("F.direction: LEFT");
                    playLower();
                }else if(((float) message.obj) < -29){
                    head_orientation2.setText("F.direction: RIGHT");
                    playHigher();
                }else{
                    head_orientation2.setText("F.direction: CENTER");
                }
                break;

                default:
                    break;
        }
        return false;
    }

    private void playLower(){
        if(!gameActive && cardGame != null){
            gameActive = true;

            dealCard();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    evaluateGuess(LOWER_CARD_MESSAGE);
                }
            },1000);
            lower_choice.setBackground(getResources().getDrawable(R.drawable.button_win));
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    lower_choice.setBackground(getResources().getDrawable(R.drawable.button_normal));
                }
            },2000);
        }
    }

    private void playHigher(){
        if(!gameActive && cardGame != null){
            gameActive = true;

            dealCard();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    evaluateGuess(HIGHER_CARD_MESSAGE);

                }
            },1000);
            higher_choice.setBackground(getResources().getDrawable(R.drawable.button_win));
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    higher_choice.setBackground(getResources().getDrawable(R.drawable.button_normal));
                }
            },2000);
        }
    }

    private void evaluateGuess(int user_guess){
        if (cardGame.getGameIsActive()){
            int res = cardGame.checkUserGuess(user_guess);
            if(res == CARD_GUESS_RIGHT){
                rightAnim();
            }else if(res == CARD_GUESS_WRONG){
                wrongAnim();
            }else{
                game_result.setText("Same Card, try again!");
                gameActive = false;
            }
        } else {
            endGame();
        }
    }


    private void dealCard(){

        card_imageview.setImageResource(R.drawable.green_back);
        final ObjectAnimator oa1 = ObjectAnimator.ofFloat(card_imageview, "scaleX", 1f, 0f);
        final ObjectAnimator oa2 = ObjectAnimator.ofFloat(card_imageview, "scaleX", 0f, 1f);
        oa1.setInterpolator(new DecelerateInterpolator());
        oa2.setInterpolator(new AccelerateDecelerateInterpolator());
        oa1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                cardGame.dealCard();
                Glide.with(MainActivity.this)
                        .load(Uri.parse("file:///android_asset/"+ CardImage.getCardName(cardGame.topCard().get_suit(), cardGame.topCard().get_value())))
                        .into(card_imageview);
                oa2.start();

            }
        });
        oa1.start();

    }

    private void updateStats(){
        game_result.setText("");
        cards_left.setText("Cards left: "+Integer.toString(cardGame.getCardsLeft()));
        streak_number.setText("Line streak: "+Integer.toString(cardGame.getCurrentStreak()));
        long_streak.setText("Total streak: "+Integer.toString(cardGame.getTotalStreak()));
    }

    private void endGame(){
        cards_left.setText("0");
        game_result.setText("Game Over");
        Glide.with(this)
                .load(Uri.parse("file:///android_asset/back.png"))
                .into(card_imageview);
    }

    private void startGame() {


        Glide.with(this)
                .load(Uri.parse("file:///android_asset/"+ CardImage.getCardName(cardGame.topCard().get_suit(), cardGame.topCard().get_value())))
                .into(card_imageview);
        updateStats();

    }

    private void rightAnim(){
        main_animation.setVisibility(View.VISIBLE);
        main_animation.setAnimation(R.raw.right);
        main_animation.playAnimation();
        main_animation.setRepeatMode(LottieDrawable.RESTART);

        main_animation.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        main_animation.setVisibility(View.INVISIBLE);
                        updateStats();
                        gameActive = false;
                    }
                },300);

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private void wrongAnim(){
        main_animation.setVisibility(View.VISIBLE);
        main_animation.setAnimation(R.raw.wrong);
        main_animation.playAnimation();
        main_animation.setRepeatMode(LottieDrawable.RESTART);

        main_animation.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        main_animation.setVisibility(View.INVISIBLE);
                        updateStats();
                        gameActive = false;
                    }
                },300);

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }


    @Override
    protected void onStop(){
        super.onStop();
    }


    private void createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }

        cameraSource.setMachineLearningFrameProcessor(new FaceDetectionProcessor());
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        startCameraSource();
    }

    /** Stops the camera. */
    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {
            createCameraSource();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }
}
