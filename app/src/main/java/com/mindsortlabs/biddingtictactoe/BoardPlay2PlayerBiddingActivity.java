package com.mindsortlabs.biddingtictactoe;

import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.mindsortlabs.biddingtictactoe.log.LogUtil;
import com.mindsortlabs.biddingtictactoe.preferences.MyPreferences;

public class BoardPlay2PlayerBiddingActivity extends AppCompatActivity {

    TextView tvBid1, tvBid2, tvBidTime, tvTotal1, tvTotal2, tvPlayerTitle;
    Button  btnSetBid;
    ImageView counter, winLine;
    GridLayout gridLayout;
    Toast mToast;
    ImageView imgGoldStack;

    RadioGroup radioGroupSymbol;
    RadioButton radioBtnCross, radioBtnCircle;
    LinearLayout layoutPlayer1, layoutPlayer2;
    char player1Symbol = 'X';
    int activePlayer = 0;  //First Player
    CountDownTimer moveTimer;
    boolean gameActive = false;
    boolean gameStarted = false;
    int[] gameState = {2, 2, 2, 2, 2, 2, 2, 2, 2};
    int line = 0;

    int bid1 = 1, bid2 = 1;
    int total1 = 100, total2 = 100;
    boolean updatedBid1 = false, updatedBid2 = false;   // put it to zero again when player moves.
    int[][] winningPositions = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6}};

    SoundPool turnSound, winSound, drawSound, loseSound;
    boolean turnSoundLoaded = false, winSoundLoaded = false, drawSoundLoaded = false, loseSoundLoaded = false;
    int turnSoundId, winSoundId, drawSoundId, loseSoundId;

    private InterstitialAd mInterstitialAd;

    ShowcaseView showcaseView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar();
        setContentView(R.layout.activity_board_play2_player_bidding);

        MobileAds.initialize(this,
                "ca-app-pub-3940256099942544~3347511713");

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());

            }
        });


//        Log.d("TAG123","onCreate: ");
        tvBid1 = findViewById(R.id.tv_bid1);
        tvBid2 = findViewById(R.id.tv_bid2);
        tvBidTime = findViewById(R.id.tv_bid_time);
        tvTotal1 = findViewById(R.id.tv_total1);
        tvTotal2 = findViewById(R.id.tv_total2);
        tvPlayerTitle = findViewById(R.id.tv_player_title);

        gridLayout = findViewById(R.id.gridLayout);
        winLine = findViewById(R.id.win_line);

        radioBtnCross = findViewById(R.id.radiobtn_cross);
        radioBtnCircle = findViewById(R.id.radiobtn_circle);

        layoutPlayer1 = findViewById(R.id.layout_player1);
        layoutPlayer2 = findViewById(R.id.layout_player2);

        layoutPlayer1.getBackground().setAlpha(65);
        layoutPlayer2.getBackground().setAlpha(65);

        radioBtnCross.setChecked(true);

        moveTimer = new CountDownTimer(3300, 1000) {
            @Override
            public void onTick(long l) {

                int time = (int) (l / 1000);
                if (LogUtil.islogOn()) {
                    Log.d("TAG123", (time) + "");
                }
                tvBidTime.setAlpha(1f);
                if (time > 1) {
                    tvBidTime.setText(String.valueOf(time));
                } else {
                    tvBidTime.setText("Play");
                }

                tvBidTime.animate().alpha(0).setDuration(900);
            }

            @Override
            public void onFinish() {
                gameActive = true;
                tvBid1.animate().alpha(0).setDuration(200);
                tvBid2.animate().alpha(0).setDuration(200);
                if (bid1 != bid2) {
                    tvTotal1.animate().alpha(0).setDuration(200);
                    tvTotal2.animate().alpha(0).setDuration(200);
                }
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tvBid1 = findViewById(R.id.tv_bid1);
                        tvBid2 = findViewById(R.id.tv_bid2);

                        tvBid1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 60);
                        tvBid2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 60);
                        tvBid1.setText(String.valueOf(bid1));
                        tvBid2.setText(String.valueOf(bid2));
                        tvBid1.animate().alpha(1f).setDuration(200);
                        tvBid2.animate().alpha(1f).setDuration(200);

                        cancelToast();
                        if (bid1 == bid2) {
                            gameActive = false;
                            mToast = Toast.makeText(BoardPlay2PlayerBiddingActivity.this, "What a coincidence! Please choose again",
                                    Toast.LENGTH_SHORT);
                            mToast.show();
                            updatedBid1 = false;
                            updatedBid2 = false;
                        } else if (bid1 > bid2) {
                            mToast = Toast.makeText(BoardPlay2PlayerBiddingActivity.this, "Player 1 turn", Toast.LENGTH_SHORT);
                            mToast.show();
                            total1 = total1 - bid1;
                            total2 = total2 + bid1;
                            tvTotal1.setText(String.valueOf(total1));
                            tvTotal2.setText(String.valueOf(total2));
                            activePlayer = 0;
                            tvTotal1.animate().alpha(1f).setDuration(200);
                            tvTotal2.animate().alpha(1f).setDuration(200);
                            tvBid1.setClickable(false);
                            tvBid2.setClickable(false);
                        } else {
                            mToast = Toast.makeText(BoardPlay2PlayerBiddingActivity.this, "Player 2 turn", Toast.LENGTH_SHORT);
                            mToast.show();
                            total1 = total1 + bid2;
                            total2 = total2 - bid2;
                            tvTotal1.setText(String.valueOf(total1));
                            tvTotal2.setText(String.valueOf(total2));
                            activePlayer = 1;
                            tvTotal1.animate().alpha(1f).setDuration(200);
                            tvTotal2.animate().alpha(1f).setDuration(200);
                            tvBid1.setClickable(false);
                            tvBid2.setClickable(false);
                        }
                    }
                }, 200);

                tvBidTime.setVisibility(View.GONE);
                //hide/display Options
            }
        };

        radioGroupSymbol = findViewById(R.id.radiogroup_symbol);
        radioGroupSymbol.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.radiobtn_cross) {
                    player1Symbol = 'X';
                    layoutPlayer1.animate().alpha(0).setDuration(200);
                    layoutPlayer2.animate().alpha(0).setDuration(200);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            layoutPlayer1.setBackgroundResource(R.drawable.lightcross);
                            layoutPlayer2.setBackgroundResource(R.drawable.lightcircle);
                            layoutPlayer1.animate().alpha(1f).setDuration(200);
                            layoutPlayer2.animate().alpha(1f).setDuration(200);
                        }
                    }, 200);
                }

                if (i == R.id.radiobtn_circle) {
                    player1Symbol = 'O';
                    layoutPlayer1.animate().alpha(0).setDuration(200);
                    layoutPlayer2.animate().alpha(0).setDuration(200);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            layoutPlayer1.setBackgroundResource(R.drawable.lightcircle);
                            layoutPlayer2.setBackgroundResource(R.drawable.lightcross);
                            layoutPlayer1.animate().alpha(1f).setDuration(200);
                            layoutPlayer2.animate().alpha(1f).setDuration(200);
                        }
                    }, 200);
                }
            }
        });

        tvBid1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveTimer.cancel();
                tvBidTime.setVisibility(View.GONE);
                setBid(1);
            }
        });

        tvBid2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveTimer.cancel();
                tvBidTime.setVisibility(View.GONE);
                setBid(2);
            }
        });


        loadSound();

        turnSound.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                turnSoundLoaded = true;
//                playSound(1);
            }
        });

        winSound.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                winSoundLoaded = true;
            }
        });

        drawSound.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                drawSoundLoaded = true;
//                playSound(1);
            }
        });

        loseSound.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                loseSoundLoaded = true;
//                playSound(1);
            }
        });

    }

    private void hideStatusBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    public void dropIn(View view) {  //set in XML

        counter = (ImageView) view;
        int tappedCounter = Integer.parseInt(counter.getTag().toString());

        if (gameActive) {

            if (!gameStarted) {
                gameStarted = true;
                radioGroupSymbol.setClickable(false);
                displayOptions(false);
            }

            if (gameState[tappedCounter] != 2) {
                cancelToast();
                mToast = Toast.makeText(this, "Play somewhere else", Toast.LENGTH_SHORT);
                mToast.show();
            } else {


                int symbol = R.drawable.cross;
                if (activePlayer == 0 && player1Symbol == 'O' || activePlayer == 1 && player1Symbol == 'X') {
                    symbol = R.drawable.circletwo;
                }

                counter.setImageResource(symbol);

                setInitialPositions(counter, tappedCounter);
                gameState[tappedCounter] = activePlayer;
                animateCounters(counter, tappedCounter);

                if (!checkWinner()) {
                    SoundActivity.playSound(this, turnSound, turnSoundLoaded, turnSoundId);
                    cancelToast();
                    mToast = Toast.makeText(this, "Time to Bid", Toast.LENGTH_SHORT);
                    mToast.show();
                }

                tvBid1.setClickable(true);
                tvBid2.setClickable(true);
                updatedBid1 = false;
                updatedBid2 = false;
                tvBid1.animate().alpha(0).setDuration(200);
                tvBid2.animate().alpha(0).setDuration(200);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tvBid1 = findViewById(R.id.tv_bid1);
                        tvBid2 = findViewById(R.id.tv_bid2);
                        tvBid1.setText("00");
                        tvBid1.animate().alpha(1f).setDuration(200);
                        tvBid2.setText("00");
                        tvBid2.animate().alpha(1f).setDuration(200);
                    }
                }, 200);
            }

            gameActive = false;
        } else {
            cancelToast();
            mToast = Toast.makeText(this, "Select Bid First", Toast.LENGTH_SHORT);
            mToast.show();
        }
    }


    @Override
    public void onBackPressed() {
        releaseSound();
        super.onBackPressed();

    }


    private void displayOptions(boolean display) {
        if (display) {
            radioGroupSymbol.animate().translationYBy(-1000f).setDuration(500);
            tvPlayerTitle.animate().translationYBy(-1000f).setDuration(500);
        } else {
            radioGroupSymbol.animate().translationY(1000f).setDuration(500);
            tvPlayerTitle.animate().translationYBy(1000f).setDuration(500);
        }


    }

    private boolean checkWinner() {
        int i = 0;
        for (int[] winningPosition : winningPositions) {

            if (gameState[winningPosition[0]] == gameState[winningPosition[1]] &&
                    gameState[winningPosition[1]] == gameState[winningPosition[2]] &&
                    gameState[winningPosition[0]] != 2) {

                // Someone has won!

                gameActive = false;

                String winner = "Player 2";

                if (gameState[winningPosition[0]] == 0) {

                    winner = "Player 1";

                }
                cancelToast();
                mToast = Toast.makeText(this, winner + " wins.", Toast.LENGTH_SHORT);
                mToast.show();


                final int winnerPlayer = gameState[winningPosition[0]];

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        gameOverMessage(winnerPlayer);
                    }
                }, 500);
                declareWinner(winningPosition, winner, i);
                return true;

            } else {

                i++;

                boolean gameIsOver = true;

                for (int counterState : gameState) {

                    if (counterState == 2) gameIsOver = false;

                }

                if (gameIsOver) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            gameOverMessage(2);
                        }
                    }, 500);

                }

            }

        }
        return false;
    }

    public void playAgain(View view) {
       /* releaseSound();
        Intent intent = new Intent(this, BoardPlay2PlayerBiddingActivity.class);
        startActivity(intent);
        if (LogUtil.islogOn()) {
            Log.d("finishCheck", "called: ");
        }
        finish();*/


        //releaseSound();
//        System.gc();
        gameActive = false;
        gameStarted = false;
        displayOptions(true);
        radioGroupSymbol.setClickable(false);
        bid1 = 1;
        bid2 = 1;
        total1 = 100;
        total2 = 100;
        updatedBid1 = false;
        updatedBid2 = false;
        tvBid1.setText("00");
        tvBid2.setText("00");
        line = 0;
        tvBid1.setClickable(true);
        tvTotal1.setText(String.valueOf(total1));
        tvTotal2.setText(String.valueOf(total2));
        radioBtnCross.setChecked(true);

        activePlayer = 0;
        initializeCounters();
        winLine.setTranslationX(0);
        winLine.setTranslationY(0);
        winLine.setRotation(0);
        winLine.setScaleX(1f);
        winLine.setVisibility(View.GONE);

        for(int i=0;i<=8;i++){
            gameState[i] = 2;
        }
        LinearLayout layout = findViewById(R.id.playAgainLayout);
        layout.setVisibility(View.GONE);

        MyPreferences myPreferences = new MyPreferences();
        total1 = myPreferences.getUserTotalCoins(this);
        total2 = myPreferences.getcpuTotalCoins(this);

    }

    private void initializeCounters() {
//        int pos = 0;
        for(int pos=0;pos<9;pos++){
            counter = findViewById(R.id.gridLayout).findViewWithTag(String.valueOf(pos));
            counter.setImageDrawable(null);
        }
    }

    private void releaseSound() {
        if (turnSound != null) {
            turnSound.setOnLoadCompleteListener(null);
            turnSound.release();
        }
        if (winSound != null) {
            winSound.setOnLoadCompleteListener(null);
            winSound.release();
        }
        if (drawSound != null) {
            drawSound.setOnLoadCompleteListener(null);
            drawSound.release();
        }
        if (loseSound != null) {
            loseSound.setOnLoadCompleteListener(null);
            loseSound.release();
        }

    }

    private void gameOverMessage(int i) {

        LinearLayout layout = findViewById(R.id.playAgainLayout);
        TextView winnerMessage = findViewById(R.id.winnerMessage);
        layout.setVisibility(View.VISIBLE);
        layout.setAlpha(0);

        if (i == 1) {
            SoundActivity.playSound(this, winSound, winSoundLoaded, winSoundId);
            winnerMessage.setText("Player 2 wins");
        } else if (i == 2) {
            winnerMessage.setText("It's a draw");
            SoundActivity.playSound(this, drawSound, drawSoundLoaded, drawSoundId);
        } else {
            winnerMessage.setText("Player 1 wins");
            SoundActivity.playSound(this, winSound, winSoundLoaded, winSoundId);
        }

        layout.animate().alpha(1).setDuration(300);


        MyPreferences myPreferences = new MyPreferences();
        int gamePlayed = myPreferences.getGamePlayed(this);
        gamePlayed +=1 ;

        if(gamePlayed>=MyPreferences.SHOW_ADS_AFTER_PLAYED_GAMES){
            //show AD
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
                gamePlayed = 0;
            } else {
                Log.d("TAG", "The interstitial wasn't loaded yet.");
            }

        }
        myPreferences.saveGamePlayed(this, gamePlayed);

    }

    private void declareWinner(int[] winningPosition, String winner, int i) {

        winLine.setVisibility(View.VISIBLE);
        winLine.setScaleX(0f);

        line = 0;

        if (i <= 2) {
            line = 1;
            if (winningPosition[0] == 0) {
                line = 0;
                winLine.setTranslationY(-(gridLayout.getWidth() / 3));
            } else if (winningPosition[0] == 6) {
                line = 2;
                winLine.setTranslationY((gridLayout.getWidth() / 3));
            }
            winLine.setScaleX(0f);
            winLine.animate().scaleX(1f).setDuration(200);
        } else if (i <= 5) {
            line = 4;
            winLine.setRotation(90);

            if (winningPosition[0] == 0) {
                line = 3;
                winLine.setTranslationX(-(gridLayout.getWidth() / 3));
            } else if (winningPosition[0] == 2) {
                line = 5;
                winLine.setTranslationX((gridLayout.getWidth() / 3));
            }
            winLine.setScaleX(0f);
            winLine.animate().scaleX(1f).setDuration(200);

        } else {

            if (winningPosition[0] == 0) {
                line = 6;
                winLine.setRotation(45);
            } else if (winningPosition[0] == 2) {
                line = 7;
                winLine.setRotation(-45);
            }

            winLine.animate().scaleX(1.2f).setDuration(200);
        }

//        try {
//
//            wait(200);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        mergeImages(line);
    }

    private void mergeImages(int line) {

    }

    private void animateCounters(ImageView counter, int tappedCounter) {

        switch (tappedCounter) {
            case 0:
                counter.animate().translationYBy(1000f).translationXBy(1000f).rotation(360).setDuration(300);
                break;
            case 1:
                counter.animate().translationYBy(1000f).rotation(360).setDuration(300);
                break;
            case 2:
                counter.animate().translationYBy(1000f).translationXBy(-1000f).rotation(360).setDuration(300);
                break;
            case 3:
                counter.animate().translationXBy(1000f).rotation(360).setDuration(300);
                break;
            case 4:
                counter.animate().scaleX(1f).scaleY(1f).rotation(360).setDuration(300);
                break;
            case 5:
                counter.animate().translationXBy(-1000f).rotation(360).setDuration(300);
                break;
            case 6:
                counter.animate().translationYBy(-1000f).translationXBy(1000f).rotation(360).setDuration(300);
                break;
            case 7:
                counter.animate().translationYBy(-1000f).rotation(360).setDuration(300);
                break;
            case 8:
                counter.animate().translationYBy(-1000f).translationXBy(-1000f).rotation(360).setDuration(300);
                break;
        }

    }

    private void setInitialPositions(ImageView counter, int tappedCounter) {

        if (tappedCounter == 0 || tappedCounter == 1 || tappedCounter == 2) {
            counter.setTranslationY(-1000f);
        }
        if (tappedCounter == 0 || tappedCounter == 3 || tappedCounter == 6) {
            counter.setTranslationX(-1000f);
        }
        if (tappedCounter == 2 || tappedCounter == 5 || tappedCounter == 8) {
            counter.setTranslationX(1000f);
        }
        if (tappedCounter == 6 || tappedCounter == 7 || tappedCounter == 8) {
            counter.setTranslationY(1000f);
        }
        if (tappedCounter == 4) {
            counter.setScaleX(0);
            counter.setScaleY(0);
        }

    }

    public void setBid(final int bidNumber) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View theView = inflater.inflate(R.layout.dialog_number_picker, null);

        final NumberPicker np = theView.findViewById(R.id.num_picker);
        imgGoldStack = theView.findViewById(R.id.goldStackImg);
        builder.setView(theView);

        builder.setView(theView)
                .setPositiveButton("Set",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        tvBid1 = findViewById(R.id.tv_bid1);
                        tvBid2 = findViewById(R.id.tv_bid2);

                        if (bidNumber == 1) {
                            tvBid1.setText("Hidden");
                            tvBid1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);
                            bid1 = np.getValue();
                            updatedBid1 = true;
                        } else {
                            tvBid2.setText("Hidden");
                            tvBid2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);
                            bid2 = np.getValue();
                            updatedBid2 = true;
                        }
                        callTimer(updatedBid1, updatedBid2);
//                Log.d("TAG124","bid1: "+ bid1 + "  bid2: "+ bid2);

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });


        np.setMaxValue(total1); // max value 100
        np.setMinValue(1);   // min value 0
        np.setValue(bid1);

        if (bidNumber == 2) {
            np.setMaxValue(total2);
            np.setValue(bid2);
        }
        np.setWrapSelectorWheel(true);

        np.setOnScrollListener(new NumberPicker.OnScrollListener() {
            @Override
            public void onScrollStateChange(NumberPicker view, int scrollState) {
                int bidSelected = view.getValue();
                setGoldStackImage(bidSelected);
            }
        });
//        final int[] bid = new int[1];
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                //numPickerBid1.setValue(i);
            }
        });

        builder.show();
    }

    private void setGoldStackImage(int bidSelected) {
        if(bidSelected<=10){
            imgGoldStack.setImageResource(R.drawable.goldcoin9);
        }else if(bidSelected<=20){
            imgGoldStack.setImageResource(R.drawable.goldcoin8);
        }else if(bidSelected<=30){
            imgGoldStack.setImageResource(R.drawable.goldcoin7);
        }else if(bidSelected<=40){
            imgGoldStack.setImageResource(R.drawable.goldcoin6);
        }else if(bidSelected<=50){
            imgGoldStack.setImageResource(R.drawable.goldcoin5);
        }else if(bidSelected<=60){
            imgGoldStack.setImageResource(R.drawable.goldcoin4);
        }else if(bidSelected<=70){
            imgGoldStack.setImageResource(R.drawable.goldcoin3);
        }else if(bidSelected<=80){
            imgGoldStack.setImageResource(R.drawable.goldcoin2);
        }else{
            imgGoldStack.setImageResource(R.drawable.goldcoin1);
        }
    }

    private void callTimer(boolean updatedBid1, boolean updatedBid2) {

        if (updatedBid1 && updatedBid2) {
            tvBidTime.setVisibility(View.VISIBLE);
            moveTimer.start();
        }
    }

    private void loadSound() {
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            turnSound = new SoundPool.Builder().build();
            winSound = new SoundPool.Builder().build();
            drawSound = new SoundPool.Builder().build();
            loseSound = new SoundPool.Builder().build();
        } else {
            turnSound = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
            winSound = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
            drawSound = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
            loseSound = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        }

        turnSoundId = turnSound.load(this, R.raw.sound1, 1);

        //Change -------------------------------------------------
        winSoundId = winSound.load(this, R.raw.sound2, 2);
        drawSoundId = drawSound.load(this, R.raw.sound2, 2);
        loseSoundId = loseSound.load(this, R.raw.sound2, 2);
    }

    void cancelToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }
}
