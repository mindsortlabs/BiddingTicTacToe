package com.mindsortlabs.biddingtictactoe.ai;

import android.support.v4.util.Pair;
import android.util.Log;

import com.mindsortlabs.biddingtictactoe.log.LogUtil;

import java.util.Random;
import java.util.Vector;

/**
 * Computes AI next move returning bidding coins and place to move if wins
 * Based on Richman Theory
 * Pre Computes bidding values for all position possible
 * Uses bitmask-DP to reduced time complexity
 */
public class BiddingTicTacToeAi {

    private static final String LOG_TAG = BiddingTicTacToeAi.class.getSimpleName();
    // Richman Value for all possible states
    private double RichmanValue[][];
    private double biddingValue = 0;
    private int totalCoins = 200, first;
    private Pair<Integer, Integer> favouredChild;
    private int level = 0;// 0->EASY    1->MEDIUM    2->HARD


    public BiddingTicTacToeAi(int totalCoins, int level) {
        RichmanValue = new double[513][513];
        this.totalCoins = totalCoins;
        this.level = level;

        for (int i = 0; i < 513; i++) {
            for (int j = 0; j < 513; j++) {
                RichmanValue[i][j] = -1.00;
                //Bid[i][j]=-1.00;
            }
        }
        int board[][] = new int[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = 0;
            }
        }
        nextMove(board, 0, 0, 0, 0);
        if (LogUtil.islogOn()) {
            Log.d(LOG_TAG, "complete.");
        }
    }

    /**
     * Reverse the player 3 <-> 5
     * 5 -> AI
     * 3 -> Real player
     */
    private int rev(int player) {
        if (player == 3)
            return 5;
        else
            return 3;
    }

    /**
     * Checks which player wins from board configuration
     * Returns 0,1 or 2
     * 0 -> continue playing no one is winning
     * 1 -> AI wins
     * 2 -> Other player wins or match is Drawn
     * Here draw is as bad as loss from user for AI
     */
    private int matchWinner(int board[][]) {

        //Check for AI win
        for (int i = 0; i < 3; i++) {
            if (board[i][0] + board[i][1] + board[i][2] == 3 * first) {
                return 1;
            }
            if (board[0][i] + board[1][i] + board[2][i] == 3 * first) {
                return 1;
            }
        }
        if (board[0][0] + board[1][1] + board[2][2] == 3 * first) {
            return 1;
        }
        if (board[0][2] + board[1][1] + board[2][0] == 3 * first) {
            return 1;
        }
        //end

        //check for 2nd player win

        for (int i = 0; i < 3; i++) {
            if (board[i][0] + board[i][1] + board[i][2] == 3 * rev(first)) {
                return 2;
            }
            if (board[0][i] + board[1][i] + board[2][i] == 3 * rev(first)) {
                return 2;
            }
        }
        if (board[0][0] + board[1][1] + board[2][2] == 3 * rev(first)) {
            return 2;
        }
        if (board[0][2] + board[1][1] + board[2][0] == 3 * rev(first)) {
            return 2;
        }

        //end


        // Check for draw if true return as other player win
        int flag = 1;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == 0)
                    flag = 0;
            }
        }

        if (flag == 1) return 2;

        // Game still in play continue playing
        return 0;
    }

    /**
     * Return Richman value for that position
     * Saves the final position and biddingValue in favouredChild and biddingValue
     */
    private double nextMove(int board[][], int depth, int firstPlayer, int secondPlayer, int check) {

        if (check == 0)
            if (Math.abs(RichmanValue[firstPlayer][secondPlayer] + 1.00) > 0.0001)
                return RichmanValue[firstPlayer][secondPlayer];

        Pair<Integer, Integer> sol = null;
        int p = matchWinner(board);
        /*
         *   p can return 0,1
         *   p==0 --> continue play
         *   p==1 --> AI wins
         *   p==2 --> Player Wins or Draw
         */
        if (p > 0) {
            if (p == 2) {
                return (1.00);
            }
            if (p == 1) {
                return (0.00);
            }
        }

        double Fmax = -1.00;
        double Fmin = 2.01;

        for (int i = 0; i < 3; i++) {

            for (int j = 0; j < 3; j++) {

                if (board[i][j] == 0) {

                    board[i][j] = first;
                    double x = nextMove(board, depth + 1, firstPlayer | getSingleValue(i, j), secondPlayer, 0);
                    board[i][j] = 0;

                    if (Fmin >= x) {
                        sol = Pair.create(i, j);
                        Fmin = x;
                    }
                }

                if (board[i][j] == 0) {

                    board[i][j] = rev(first);
                    double x = nextMove(board, depth + 1, firstPlayer, secondPlayer | getSingleValue(i, j), 0);
                    board[i][j] = 0;

                    if (Fmax <= x) {
                        //sol = Pair.create(i,j);
                        Fmax = x;
                    }
                }
            }
        }

        if (depth == 0) {
            favouredChild = sol;
            biddingValue = Math.abs(Fmax - Fmin) / 2;
        }

        return RichmanValue[firstPlayer][secondPlayer] = Math.abs(Fmax + Fmin) / 2;
    }

    /**
     * Converts position in 3*3 matrix into a integer
     * Used in bitmasking storing all position in an integer
     * For example -
     * (2,1) --> position 8 --> return 2^8
     * <p>
     * Positions in a Tic Tac Toe
     * 1 2 3
     * 4 5 6
     * 7 8 9
     */
    private int getSingleValue(int i, int j) {
        return (int) Math.pow(2, i * 3 + j);
    }

    /**
     * Returns number of coins to biddingValue with position to place the mark
     */
    public Pair<Integer, Pair<Integer, Integer>> getSolution(Vector<String> board, int aicoins, int opponentcoins, char player) {


        if (LogUtil.islogOn()) {
            Log.d(LOG_TAG, "BOARD ::" + "  " + board.toString());
            Log.d(LOG_TAG, "COINS ::" + "  " + aicoins);
        }
        if (aicoins == 0)
            return Pair.create(0, Pair.create(0, 0));

        int boards[][] = new int[3][3];

        //If player is X, I'm the first player.
        //If player is O, I'm the second player.
        //cin >> player;
        //player = 'X';

        //Read the board now. The board is a 3x3 array filled with X, O or _.

        int firstPLayer = 0, secondPlayer = 0;
        for (int i = 0; i < 3; i++) {

            for (int j = 0; j < 3; j++) {

                if (board.get(i).charAt(j) == '_') {
                    boards[i][j] = 0;
                } else if (board.get(i).charAt(j) == player) {
                    boards[i][j] = 5;
                    firstPLayer |= getSingleValue(i, j);
                } else {
                    boards[i][j] = 3;
                    secondPlayer |= getSingleValue(i, j);
                }


            }

        }

        first = 5;

        int minBid;

        //if(Math.abs(RichmanValue[firstPLayer][secondPlayer]+1.00)<0.0001)
        if (isAiAboutToWin(boards)) {

            minBid = totalCoins - aicoins + 1;

        } else if ( (aicoins<2*opponentcoins+2)  && isOpponentAboutToWin(boards)) {

            minBid = totalCoins - aicoins + 1;

        } else {
            nextMove(boards, 0, firstPLayer, secondPlayer, 1);

            //biddingValue = Bid[firstPLayer][secondPlayer];
            //favouredChild = Pairs[firstPLayer][secondPlayer];

            biddingValue = biddingValue * totalCoins;

            minBid = (int) biddingValue;
            if (level == 1) {
                //MEDIUM LEVEL
                Random r = new Random();
                minBid = minBid + (r.nextBoolean() ? r.nextInt(5) : -r.nextInt(5));
            }

        }

        if (level == 0) {
            //EASY LEVEL
            Random r = new Random();
            minBid = minBid + (r.nextBoolean() ? r.nextInt(10) : -r.nextInt(10));
        }

        int opponentBid = totalCoins - aicoins;

        minBid = Math.min(minBid, opponentBid + 1);
        minBid = Math.min(minBid, aicoins);
        if (LogUtil.islogOn()) {
            Log.d(LOG_TAG, "BIDDING ::" + "  " + minBid);
            // Log.d(LOG_TAG , "CHILDRENS ::"+"  "+favouredChild.first +"    "+favouredChild.second);
        }
        if (favouredChild == null) {
            return Pair.create(minBid, Pair.create(0, 0));
        }
        return Pair.create(minBid, favouredChild);
    }

    /**
     * Returns true if AI can win in one move
     */
    private boolean isAiAboutToWin(int[][] board) {

        boolean flag = false;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {

                if (board[i][j] == 0) {

                    board[i][j] = first;
                    int temp_flag = matchWinner(board);

                    if (temp_flag == 1) {
                        favouredChild = Pair.create(i, j);
                    }
                    flag = flag || (temp_flag == 1);
                    board[i][j] = 0;
                }
            }
        }
        return flag;
    }

    /**
     * Returns true if player can win in one move
     */
    private boolean isOpponentAboutToWin(int[][] board) {

        boolean flag = false;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {

                if (board[i][j] == 0) {

                    board[i][j] = rev(first);
                    int temp_flag = matchWinner(board);

                    if (temp_flag == 2) {
                        favouredChild = Pair.create(i, j);
                    }
                    flag = flag || (temp_flag == 2);
                    board[i][j] = 0;
                }
            }
        }
        return flag;
    }

}