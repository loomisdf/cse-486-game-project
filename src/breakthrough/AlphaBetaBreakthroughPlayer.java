package breakthrough;

import game.GameMove;
import game.GamePlayer;
import game.GameState;

import java.util.ArrayList;

/**
 * Created by loomisdf on 10/28/2016.
 */
public class AlphaBetaBreakthroughPlayer extends GamePlayer {
    GameState gameState;
    public final int MAX_DEPTH = 10;

    public AlphaBetaBreakthroughPlayer(String nname)
    {
        super(nname, "Breakthrough");
        gameState = new BreakthroughState();
    }

    protected class ScoredBreakthroughMove extends BreakthroughMove {
        public ScoredBreakthroughMove(int r1, int c1, int r2, int c2, double s) {
            super(r1, c1, r2, c2);
            score = s;
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

    @Override
    public GameMove getMove(GameState state, String lastMv) {
        BreakthroughState board = (BreakthroughState)state;
        ArrayList<BreakthroughMove> list = new ArrayList<BreakthroughMove>();
        BreakthroughMove mv = new BreakthroughMove();
        int dir = state.who == GameState.Who.HOME ? +1 : -1;
        for (int r=0; r<BreakthroughState.N; r++) {
            for (int c=0; c<BreakthroughState.N; c++) {
                mv.startRow = r;
                mv.startCol = c;
                mv.endingRow = r+dir; mv.endingCol = c;
                if (board.moveOK(mv)) {
                    list.add((BreakthroughMove)mv.clone());
                }
                mv.endingRow = r+dir; mv.endingCol = c+1;
                if (board.moveOK(mv)) {
                    list.add((BreakthroughMove)mv.clone());
                }
                mv.endingRow = r+dir; mv.endingCol = c-1;
                if (board.moveOK(mv)) {
                    list.add((BreakthroughMove)mv.clone());
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        GamePlayer p = new AlphaBetaBreakthroughPlayer("Alpha Beta Player");
        p.compete(args);
        System.out.println("Hi");
    }

}
