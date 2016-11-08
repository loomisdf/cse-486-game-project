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
    public final int PANIC_TIME = 1000;
    public final int MID_GAME_MOVE_TIME = 4000;
    public final int EARLY_GAME_MOVE_TIME = 1000;
    public final int LATE_GAME_TIME = 8000;
    public static double moveTimeLeft;
    // We don't know how to figure out the max score
    private static final int MAX_SCORE = Integer.MAX_VALUE;
    public int depthLimit;

    public ArrayList<ScoredBreakthroughMove> mvStack;

    public MiniMaxBreakthroughPlayer(String nname, int d)
    {
        super(nname, "Breakthrough");
        moveTimeLeft = 240 * 1000; // ms
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
            startCol = c1;
            endingRow = r2;
            endingCol = c2;
            score = s;
        }

        @Override
        public String toString() {
            return super.toString() + " " + this.score;
        }

        public double score;
    }

    /**
     * Initializes the stack of Moves.
     */
    public void init() {
        mvStack = new ArrayList<>(10);
        for(int i = 0; i < 10; i++) {
            mvStack.add(new ScoredBreakthroughMove(0, 0, 0, 0, 0));
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
    public static boolean hasNeighbor(BreakthroughState state, int r, int c, char who) {
        boolean hasNeighbor = false;
        if(r != 0 && state.board[r-1][c] == who) {
            hasNeighbor = true;
        }
        if(r != BreakthroughState.N - 1 && state.board[r+1][c] == who) {
            hasNeighbor = true;
        }
        if(c != 0 && state.board[r][c-1] == who) {
            hasNeighbor = true;
        }
        if(c != BreakthroughState.N - 1 && state.board[r][c+1] == who) {
            hasNeighbor = true;
        }
        if(r != 0 && c != 0 && state.board[r-1][c-1] == who) {
            hasNeighbor = true;
        }
        if(r != 0 && c != BreakthroughState.N - 1 && state.board[r-1][c+1] == who) {
            hasNeighbor = true;
        }
        if(r != BreakthroughState.N - 1 && c != 0 && state.board[r+1][c-1] == who) {
            hasNeighbor = true;
        }
        if(r != BreakthroughState.N - 1 && c != BreakthroughState.N - 1 && state.board[r+1][c+1] == who) {
            hasNeighbor = true;
        }
        return hasNeighbor;
    }

    public static int eval(BreakthroughState state, char who) {
        int score = 0;
        for(int r = 0; r < BreakthroughState.N; r++) {
            for (int c = 0; c < BreakthroughState.N; c++) {
                if (state.board[r][c] == who) {
                    score+= 10;
                }
                if (!hasNeighbor(state, r, c, who)) {
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

    protected static void shuffle(ArrayList<BreakthroughMove> ary) {
        int len = ary.size();
        for (int i = 0; i < len; i++) {
            int spot = Util.randInt(i, len - 1);
            BreakthroughMove tmp = ary.get(i);
            ary.set(i, ary.get(spot)); // ary[i] = ary[spot];
            ary.set(spot, tmp); // ary[spot] = tmp;
        }
    }

    private ArrayList<BreakthroughMove> getAvailableMoves(BreakthroughState brd) {
        ArrayList<BreakthroughMove> list = new ArrayList<BreakthroughMove>();
        BreakthroughMove mv = new BreakthroughMove();
        int dir = brd.who == GameState.Who.HOME ? +1 : -1;
        for (int r=0; r<BreakthroughState.N; r++) {
            for (int c=0; c<BreakthroughState.N; c++) {
                mv.startRow = r;
                mv.startCol = c;
                mv.endingRow = r+dir; mv.endingCol = c;
                if (brd.moveOK(mv)) {
                    list.add((BreakthroughMove)mv.clone());
                }
                mv.endingRow = r+dir; mv.endingCol = c+1;
                if (brd.moveOK(mv)) {
                    list.add((BreakthroughMove)mv.clone());
                }
                mv.endingRow = r+dir; mv.endingCol = c-1;
                if (brd.moveOK(mv)) {
                    list.add((BreakthroughMove)mv.clone());
                }
            }
        }
        return list;
    }

    private void minimax(BreakthroughState brd, double alpha, double beta, int currDepth, double timeRemaining) {
        if(mvStack.size() < currDepth + 2) {
            mvStack.add(new ScoredBreakthroughMove(0, 0, 0, 0, 0));
        }
        double beforeTime = System.currentTimeMillis();
        boolean toMaximize = (brd.getWho() == GameState.Who.HOME);
        boolean toMinimize = !toMaximize;
        boolean isTerminal = terminalValue(brd, mvStack.get(currDepth));

        if (currDepth == depthLimit) {
            System.out.println();
        }
        if(isTerminal) {

        }
        else if(timeRemaining <= 0) {
           mvStack.get(currDepth).set(0, 0, 0, 0, evalBoard(brd));
        }
        else {

            double bestScore = (brd.getWho() == GameState.Who.HOME ? Double.NEGATIVE_INFINITY
                    : Double.POSITIVE_INFINITY);
            ScoredBreakthroughMove bestMove = mvStack.get(currDepth);
            ScoredBreakthroughMove nextMove = mvStack.get(currDepth + 1);

            System.out.println("next move: " + nextMove);

            bestMove.set(0, 0, 0, 0, bestScore);

            ArrayList<BreakthroughMove> mvList = getAvailableMoves(brd);
            shuffle(mvList);

            for(BreakthroughMove mv : mvList) {
                ScoredBreakthroughMove tempMv = new ScoredBreakthroughMove(mv);
                BreakthroughState board_copy = (BreakthroughState)brd.clone();
                board_copy.makeMove(tempMv);

                double afterTime = System.currentTimeMillis();
                double timeTaken = afterTime - beforeTime;
                minimax(board_copy, alpha, beta, currDepth + 1, timeRemaining - timeTaken);


                if (toMaximize && nextMove.score > bestMove.score) {
                    bestMove.set(tempMv.startRow, tempMv.startCol, tempMv.endingRow, tempMv.endingCol, nextMove.score);
                } else if (!toMaximize && nextMove.score < bestMove.score) {
                    bestMove.set(tempMv.startRow, tempMv.startCol, tempMv.endingRow, tempMv.endingCol, nextMove.score);
                }

                // Update alpha and beta. Perform pruning, if possible.
                if (toMinimize) {
                    beta = Math.min(bestMove.score, beta);
                    if (bestMove.score <= alpha || bestMove.score == -MAX_SCORE) {
                        return;
                    }
                } else {
                    alpha = Math.max(bestMove.score, alpha);
                    if (bestMove.score >= beta || bestMove.score == MAX_SCORE) {
                        return;
                    }
                }
            }
        }
    }

    @Override
    public GameMove getMove(GameState state, String lastMv) {
        if(state.numMoves == 0) {
            moveTimeLeft = 240 * 1000;
        }
        double beforeTime = System.currentTimeMillis();
        double timeLimit = calculateTimeLimit(state);

        minimax((BreakthroughState) state, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0, timeLimit);

        double timeTaken = System.currentTimeMillis() - beforeTime;
//        try {
//            if(!state.moveOK(mvStack.get(0)))
//                throw new Exception("Move invalid");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        moveTimeLeft -= timeTaken;

        System.out.println("Move time left: " + moveTimeLeft / 1000 + "secs");

        return mvStack.get(0);
    }

    private double calculateTimeLimit(GameState state) {
        double time = 1;
        // PANIC
        if(moveTimeLeft <= LATE_GAME_TIME + 1000) {
            System.out.println("ENTERING PANIC MODE");
           time = 1; // use 1 depth level
        }
        // check for beginning of game
        else if(state.numMoves <= 4) {
            time = EARLY_GAME_MOVE_TIME;
        }
        else if(state.numMoves > 4) {
            if(moveTimeLeft <= 100 * 1000) {
                time = LATE_GAME_TIME;
            }
            else {
                time = MID_GAME_MOVE_TIME;
            }
        }
        return time;
    }

    public static void main(String[] args) {
        GamePlayer p = new MiniMaxBreakthroughPlayer("Mini Max Player", 1);
        p.compete(args);
        System.out.println("Hi");
//        p.solvePuzzles(new String [] {"BTPuzzle1", "BTPuzzle2"});
    }

}
