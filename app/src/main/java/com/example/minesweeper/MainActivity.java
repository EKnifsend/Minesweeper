package com.example.minesweeper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int COLUMN_COUNT = 10;
    private static final int ROW_COUNT = 12;
    private final int BOMBS = 4;

    // save the TextViews of all cells in an array, so later on,
    // when a TextView is clicked, we know which cell it is
    private ArrayList<Cell> cells;

    private ArrayList<Integer> bombLocations;

    Timer timer;
    TimerTask timerTask;
    TextView timerCount;
    private int timeElapsed = 0;

    private int mode = 0;   // mode == 0 means they are selecting, mode == 1 means they are flagging

    private int flags;

    private int cellsVisited = 0;

    private boolean gameOver = false;
    private boolean gameLost = false;

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void updateFlagCount() {
        TextView flagsCount = (TextView) findViewById(R.id.flagsCount);
        flagsCount.setText(String.valueOf(flags));
    }

    private void createBombs() {
        bombLocations = new ArrayList<Integer>(BOMBS);

        Random random = new Random(System.currentTimeMillis());

        int cellCount = COLUMN_COUNT * ROW_COUNT;
        int bombsCreated = 0;
        while (bombsCreated < BOMBS) {
            // Generate a random number between 0 and cellCount
            int location = random.nextInt(cellCount);

            if (!bombLocations.contains(location)) {            // location is not already in array
                bombLocations.add(location);
                bombsCreated ++;
            }
        }
    }

    private int getRow (int id) {
        return id/COLUMN_COUNT;
    }

    private int getCol (int id) {
        return id%COLUMN_COUNT;
    }

    private int getId (int row, int col) {
        if (row < 0 || row >= ROW_COUNT || col < 0 || col >= COLUMN_COUNT) {         // if row or
            // column is out of bounds
            return -1;
        }
        else {
            return row * COLUMN_COUNT + col;
        }
    }

    private int countAdjacentBombs (int id) {
        int row = getRow(id);
        int col = getCol(id);

        int bombCount = 0;

        //check upper left
        if (bombLocations.contains(getId (row - 1, col - 1)))  {
            bombCount ++;
        }
        //check upper
        if (bombLocations.contains(getId (row - 1, col)))  {
            bombCount ++;
        }
        //check upper right
        if (bombLocations.contains(getId (row - 1, col + 1)))  {
            bombCount ++;
        }
        //check right
        if (bombLocations.contains(getId(row, col + 1)))  {
            bombCount ++;
        }
        //check lower right
        if (bombLocations.contains(getId(row + 1, col + 1)))  {
            bombCount ++;
        }
        //check lower
        if (bombLocations.contains(getId(row + 1, col)))  {
            bombCount ++;
        }
        //check lower left
        if (bombLocations.contains(getId(row + 1, col - 1)))  {
            bombCount ++;
        }
        //check left
        if (bombLocations.contains(getId(row, col - 1)))  {
            bombCount ++;
        }

        return bombCount;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cells = new ArrayList<Cell>();

        // set flags
        flags = BOMBS;
        updateFlagCount();

        // generate bomb locations
        createBombs();

        // wire bottom button
        TextView bottomButton = (TextView) findViewById(R.id.bottomButton);
        bottomButton.setOnClickListener(this::changeMode);

        // Create Timer
        timerCount = (TextView) findViewById(R.id.timerCount);

        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                timeElapsed++;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateTimerCount();
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 1000);

        // Make cells
        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout01);
        grid.setRowCount(ROW_COUNT);
        grid.setColumnCount(COLUMN_COUNT);
        for (int row = 0; row < ROW_COUNT; row++) {
            for (int col = 0; col < COLUMN_COUNT; col++) {
                TextView tv = new TextView(this);
                tv.setHeight(dpToPixel(30));
                tv.setWidth(dpToPixel(30));
                tv.setTextSize(20);//dpToPixel(32) );
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                tv.setTextColor(Color.GRAY);
                tv.setBackgroundColor(Color.GREEN);
                tv.setOnClickListener(this::onClickTV);

                int id = getId(row, col);
                Cell c;

                if (bombLocations.contains(id)) {           // id is listed as a bomb location
                    c = new Cell(this, id, true, 0, tv);
                }
                else {
                    int adjacentBombs = countAdjacentBombs(id);
                    c = new Cell(this, id, false, adjacentBombs, tv);
                }

                cells.add(c);

                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.setMargins(dpToPixel(2), dpToPixel(2), dpToPixel(2), dpToPixel(2));
                lp.rowSpec = GridLayout.spec(row);
                lp.columnSpec = GridLayout.spec(col);

                grid.addView(cells.get(id).tv, lp);
            }
        }
    }

    private void updateTimerCount() {
        timerCount.setText(String.valueOf(timeElapsed));
    }

    private void revealAll() {
        for (int row = 0; row < ROW_COUNT; row++) {
            for (int col = 0; col < COLUMN_COUNT; col++) {
                Cell c = cells.get(getId(row, col));

                if (!c.isBomb() || !c.isRevealed()) {       // only don't reveal if c is bomb and
                    // already revealed
                    cells.get(getId(row, col)).reveal();
                }
            }
        }
    }

    private void floodReveal(int id) {
        // Base case: Check for out-of-bounds or non-'0' cells
        if (id == -1 || cells.get(id).isRevealed()) {         // CORRECT?
            return;
        }

        // if cell at id is flagged, return flag
        if (cells.get(id).isFlagged()) {
            cells.get(id).unFlag();
            flags++;
            updateFlagCount();
        }

        // reveal cell at id
        cells.get(id).reveal();
        cellsVisited ++;

        int row = getRow(id);
        int col = getCol(id);

        // if cell is not numbered, recursively visit adjacent cells
        if (cells.get(id).getAdjacentBombs() == 0) {
            floodReveal(getId (row - 1, col - 1));  // upper left
            floodReveal(getId (row - 1, col));// upper
            floodReveal(getId (row - 1, col + 1)); // upper right
            floodReveal(getId (row, col + 1));// right
            floodReveal(getId (row + 1, col + 1));// lower right
            floodReveal(getId (row + 1, col));// lower
            floodReveal(getId (row + 1, col - 1));// lower left
            floodReveal(getId (row, col - 1));// left
        }
    }

    public void changeMode(View view){
        TextView tv = (TextView) view;

        if (mode == 0) {    // currently selecting, switch to flagging
            mode = 1;
            tv.setText(getString(R.string.flag));
        }
        else {              // currently flagging, switch to selecting
            mode = 0;
            tv.setText(getString(R.string.pick));
        }
    }

    private int findIndexOfCellTextView(TextView tv) {
        for (int n=0; n<cells.size(); n++) {
            if ((cells.get(n)).tv == tv)
                return n;
        }
        return -1;
    }

    public void onClickTV(View view){
        TextView tv = (TextView) view;
        int n = findIndexOfCellTextView(tv);
        Cell c = cells.get(n);

        if (gameOver) {         // if game over, trigger end page
            endGame();
        }
        else {                  // otherwise, do action based on mode
            if (mode==0) {          // if selecting
                selectingAction(c);
            }
            else {                  // if flagging
                flaggingAction(c);
            }
        }
    }

    private void selectingAction(Cell c) {
        if (!c.isFlagged()) {       // only make able to select if unflagged
            if (c.isBomb()) {           // if cell is a bomb, trigger losing scenario
                destruction(c);
            }
            else {                      // otherwise, reveal cell
                floodReveal(c.ID);

                if (cellsVisited >= ROW_COUNT * COLUMN_COUNT - BOMBS) {     // if all non-bomb cells
                    // have been selected, trigger winning scenario
                    victory();
                }
            }
        }
    }

    private void flaggingAction(Cell c) {
        if (!c.isRevealed()) {          // can only flag if cell is not yet revealed
            if (c.isFlagged()) {            // if c is flagged, unflag
                c.unFlag();
                flags ++;
                updateFlagCount();
            }
            else {                          // if c is unflagged, attempt to flag
                if (flags > 0) {                // can only flag if you have available flags
                    c.flag();
                    flags --;
                    updateFlagCount();
                }
            }
        }
    }

    // winning scenario
    private void victory() {
        // stop timer
        timer.cancel();

        gameOver = true;
    }

    // losing scenario
    private void destruction(Cell c) {
        // stop timer
        timer.cancel();

        revealAll();
        c.tv.setBackgroundColor(Color.RED);

        gameOver = true;
        gameLost = true;
    }

    private void endGame() {
        // make ending message
        String message = "Used " + timeElapsed + " seconds.\n";

        if (!gameLost) {
            message += "You won.\nGood job!";
        }
        else {
            message += "You lost.\nBetter luck next time!";
        }

        // call results page activity
        Intent intent = new Intent(this, DisplayResultsActivity.class);
        intent.putExtra("message", message);

        startActivity(intent);
    }
}