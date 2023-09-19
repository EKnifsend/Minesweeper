package com.example.minesweeper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
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

        flags = BOMBS;
        updateFlagCount();

        // generate bomb locations
        createBombs();

        /*
        // Method (1): add statically created cells
        TextView tv00 = (TextView) findViewById(R.id.textView00);
        TextView tv01 = (TextView) findViewById(R.id.textView01);
        TextView tv10 = (TextView) findViewById(R.id.textView10);
        TextView tv11 = (TextView) findViewById(R.id.textView11);

        tv00.setTextColor(Color.GRAY);
        tv00.setBackgroundColor(Color.GRAY);
        tv00.setOnClickListener(this::onClickTV);

        tv01.setTextColor(Color.GRAY);
        tv01.setBackgroundColor(Color.GRAY);
        tv01.setOnClickListener(this::onClickTV);

        tv10.setTextColor(Color.GRAY);
        tv10.setBackgroundColor(Color.GRAY);
        tv10.setOnClickListener(this::onClickTV);

        tv11.setTextColor(Color.GRAY);
        tv11.setBackgroundColor(Color.GRAY);
        tv11.setOnClickListener(this::onClickTV);

        cell_tvs.add(tv00);
        cell_tvs.add(tv01);
        cell_tvs.add(tv10);
        cell_tvs.add(tv11);
         */

        TextView bottomButton = (TextView) findViewById(R.id.bottomButton);
        bottomButton.setOnClickListener(this::changeMode);

        /*
        TextView timerCount = (TextView) findViewById(R.id.timerCount);
        timer = new Timer(this, timerCount);
        timer.startTimer();
         */

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

        // Method (2): add four dynamically created cells
        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout01);
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

        /*
        // Method (3): add four dynamically created cells with LayoutInflater
        LayoutInflater li = LayoutInflater.from(this);
        for (int i = 4; i<=5; i++) {
            for (int j=0; j<=1; j++) {
                TextView tv = (TextView) li.inflate(R.layout.custom_cell_layout, grid, false);
                //tv.setText(String.valueOf(i)+String.valueOf(j));
                tv.setTextColor(Color.GRAY);
                tv.setBackgroundColor(Color.GRAY);
                tv.setOnClickListener(this::onClickTV);

                GridLayout.LayoutParams lp = (GridLayout.LayoutParams) tv.getLayoutParams();
                lp.rowSpec = GridLayout.spec(i);
                lp.columnSpec = GridLayout.spec(j);

                grid.addView(tv, lp);

                cell_tvs.add(tv);
            }
        }
         */

    }

    private void updateTimerCount() {
        timerCount.setText(String.valueOf(timeElapsed));
    }

    private void revealAll() {
        for (int row = 0; row < ROW_COUNT; row++) {
            for (int col = 0; col < COLUMN_COUNT; col++) {
                Cell c = cells.get(getId(row, col));

                if (!c.isBomb() || !c.isRevealed()) {
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

        if (mode == 0) {    // currently selecting
            mode = 1;
            tv.setText(getString(R.string.flag));
        }
        else {              // currently flagging
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
        //int i = n/COLUMN_COUNT;
        //int j = n%COLUMN_COUNT;
        //tv.setText(String.valueOf(i)+String.valueOf(j));

        Cell c = cells.get(n);
        /*
        if (tv.getCurrentTextColor() == Color.GREEN) {

        }else {
        }
        */

        if (gameOver) {
            // TRIGGER END PAGE
        }
        else {
            if (mode==0) {
                selectingAction(c);
            }
            else {
                flaggingAction(c);
            }
        }
    }

    private void selectingAction(Cell c) {
        if (!c.isFlagged()) {
            if (c.isBomb()) {
                destruction(c);
            }
            else {
                floodReveal(c.ID);

                // check if won
                if (cellsVisited >= ROW_COUNT * COLUMN_COUNT - BOMBS) {
                    // WIN CONDITION
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

    private void victory() {          // winning condition
        // stop timer
        timer.cancel();

        gameOver = true;
    }

    private void destruction(Cell c) {          // losing condition
        c.reveal();
        c.tv.setBackgroundColor(Color.RED);

        // stop timer
        timer.cancel();
        revealAll();

        gameOver = true;
        gameLost = true;
    }
}