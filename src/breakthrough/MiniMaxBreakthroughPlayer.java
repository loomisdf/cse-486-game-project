package breakthrough;

import game.GameMove;
import game.GamePlayer;
import game.GameState;

import java.util.ArrayList;

import game.*;

/**
 * Created by loomisdf on 10/28/2016.
 */
public class MiniMaxBreakthroughPlayer extends GamePlayer {
    public final int MAX_DEPTH = 10;
    // We don't know how to figure out the max score
    private static final int MAX_SCORE = Integer.MAX_VALUE;
    public int depthLimit;

    public ScoredBreakthroughMove mvStack[];

    public MiniMaxBreakthroughPlayer(String nname, int d)
    {
        super(nname, "Breakthrough");
        depthLimit = d;
    }

    protected class ScoredBreakthroughMove extends BreakthroughMove {
        public ScoredBreakthroughMove(int r1, int c1, int r2, int c2, double s) {
            super(r1, c1, r2, c2);
            score = s;
        }

        public ScoredBreakthroughMove(BreakthroughMove mv) {
            this.startRow = mv.startRow;
            this.endingRow = mv.endingRow;
            this.startCol = mv.startCol;
            this.endingCol = mv.endingCol;
            score = 0;
        }

        public void set(int r1, int c1, int r2, int c2, double s) {
            startRow = r1;
            startCol = c2;
            endingRow = r2;
            endingCol = c2;
            score = s;
        }

        public double score;
    }

    /**
     * Initializes the stack of Moves.
     */
    public void init() {
        mvStack = new ScoredBreakthroughMove[MAX_DEPTH];
        for (int i = 0; i < MAX_DEPTH; i++) {
            mvStack[i] = new ScoredBreakthroughMove(0, 0, 0, 0, 0);
        }
    }

    /**
     * Determines if a board represents a completed game. If it is, the
     * evaluation values for these boards is recorded (i.e., 0 for a draw +X,
     * for a HOME win and -X for an AWAY win.
     *
     * @param brd
     *            Connect4 board to be examined
     * @param mv
     *            where to place the score information; column is irrelevant
     * @return true if the brd is a terminal state
     */
    protected boolean terminalValue(GameState brd, ScoredBreakthroughMove mv) {
        GameState.Status status = brd.getStatus();
        boolean isTerminal = true;

        if (status == GameState.Status.HOME_WIN) {
            mv.set(0, 0, 0, 0, MAX_SCORE);
        } else if (status == GameState.Status.AWAY_WIN) {
            mv.set(0, 0, 0, 0, -MAX_SCORE);
        } else if (status == GameState.Status.DRAW) {
            mv.set(0, 0, 0, 0, 0);
        } else {
            isTerminal = false;
        }
        return isTerminal;
    }

    public static int eval(BreakthroughState brd, char who) {
        int score = 0;
        for(int r = 0; r < BreakthroughState.N; r++) {
           for(int c = 0; c < BreakthroughState.N; c++) {
               if(brd.board[r][c] == who) {
                   score++;
               }
           }
        }
        return score;
    }

    public static int evalBoard(BreakthroughState brd) {
        int score = eval(brd, BreakthroughState.homeSym) - eval(brd, BreakthroughState.awaySym);
        if (Math.abs(score) > MAX_SCORE) {
            System.err.println("Problem with eval");
            System.exit(0);
        }
        return score;
    }

    private void minimax(BreakthroughState brd, int currDepth) {
        boolean toMaximize = (brd.getWho() == GameState.Who.HOME);
        boolean isTerminal = terminalValue(brd, mvStack[currDepth]);

        if(isTerminal) {

        }
        else if(currDepth == depthLimit) {
            // Don't understand what this is supposed to represent
           mvStack[currDepth].set(0, 0, 0, 0, evalBoard(brd));
        }
        else {
            ScoredBreakthroughMove tempMv;

            double bestScore = (brd.getWho() == GameState.Who.HOME ? Double.NEGATIVE_INFINITY
                    : Double.POSITIVE_INFINITY);
            // How are the scores for bestMove and nextMove evaluated?
            ScoredBreakthroughMove bestMove = mvStack[currDepth];
            ScoredBreakthroughMove nextMove = mvStack[currDepth + 1];

            bestMove.set(0, 0, 0, 0, bestScore);
            GameState.Who currTurn = brd.getWho();

            BreakthroughState board_copy = (BreakthroughState)brd;
            ArrayList<BreakthroughMove> list = new ArrayList<BreakthroughMove>();
            BreakthroughMove mv = new BreakthroughMove();
            int dir = brd.who == GameState.Who.HOME ? +1 : -1;
            for (int r=0; r<BreakthroughState.N; r++) {
                for (int c=0; c<BreakthroughState.N; c++) {
                    mv.startRow = r;
                    mv.startCol = c;
                    mv.endingRow = r+dir; mv.endingCol = c;
                    if (board_copy.moveOK(mv)) {
                        list.add((BreakthroughMove)mv.clone());
                    }
                    mv.endingRow = r+dir; mv.endingCol = c+1;
                    if (board_copy.moveOK(mv)) {
                        list.add((BreakthroughMove)mv.clone());
                    }
                    mv.endingRow = r+dir; mv.endingCol = c-1;
                    if (board_copy.moveOK(mv)) {
                        list.add((BreakthroughMove)mv.clone());
                    }
                }
            }
            int which = Util.randInt(0, list.size()-1);
            tempMv = new ScoredBreakthroughMove(list.get(which));

            board_copy.makeMove(tempMv);

            minimax(board_copy, currDepth + 1);

            if (toMaximize && nextMove.score > bestMove.score) {
                bestMove.set(nextMove.startRow, nextMove.endingRow, nextMove.startCol, nextMove.endingCol, nextMove.score);
            } else if (!toMaximize && nextMove.score < bestMove.score) {
                bestMove.set(nextMove.startRow, nextMove.endingRow, nextMove.startCol, nextMove.endingCol, nextMove.score);
            }
        }
    }

    @Override
    public GameMove getMove(GameState state, String lastMv) {
        minimax((BreakthroughState) state, 0);
        return mvStack[0];
    }

    public static void main(String[] args) {
        GamePlayer p = new MiniMaxBreakthroughPlayer("Mini Max Player", 6);
        p.compete(args);
        System.out.println("Hi");
    }

}
