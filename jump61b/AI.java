package jump61;

import java.util.ArrayList;

/** An automated Player.
 *  @author Ajai K. Sharma
 */
class AI extends Player {

    /** Time allotted to all but final search depth (milliseconds). */
    private static final long TIME_LIMIT = 15000;

    /** Number of calls to minmax between checks of elapsed time. */
    private static final long TIME_CHECK_INTERVAL = 10000;

    /** Number of milliseconds in one second. */
    private static final double MILLIS = 1000.0;

    /** Enum describing possible strategies the AI can use. */
    private enum Strategy {
        /** Possible strategies. */
        FIRST_LEGAL, STATIC_EVAL, MINIMAX
    }

    /** Strategy used by the AI. */
    private Strategy _strat;

    /** Depth taken in minimax strategy. */
    private static final int DEPTH = 4;

    /** Large number used to avoid issues with negating
     *  Integer.MIN_VALUE. */
    private static final int LARGE_NUMBER = 100000;

    /** A new player of GAME initially playing COLOR that chooses
     *  moves automatically.
     */
    AI(Game game, Side color) {
        super(game, color);
        _strat = Strategy.MINIMAX;
    }

    @Override
    void makeMove() {
        int move = generateMove();
        int size = getBoard().size();
        String side = getSide().toString();
        side = side.substring(0, 1).toUpperCase() + side.substring(1);
        getGame().makeMove(move);
        getGame().message("%s moves %d %d.%n",
                    side, move / size + 1, move % size + 1);
    }

    /** Returns the move that the AI will take, depending on
     *  the strategy it is using. */
    int generateMove() {
        switch (_strat) {
        case FIRST_LEGAL :
            return firstLegalStrat();
        case STATIC_EVAL :
            return staticEvalStrat(new MutableBoard(getBoard()));
        case MINIMAX :
            return minimaxStrat(new MutableBoard(getBoard()));
        default:
            return -1;
        }
    }

    /** Generates a move by returning first legal move.
     *  Deterministic. */
    int firstLegalStrat() {
        return findLegalMove(getBoard());
    }

    /** Returns the static value of player P moving to index
     *  N on mutable board B. Leaves the board in its
     *  original state. */
    int tryMoveStatic(Side p, int n, MutableBoard b) {
        b.addSpot(p, n);
        int result = staticEval(p, b);
        b.undo();
        return result;
    }

    /** Generates a move by choosing a move that yields
     *  the highest possible static evaluation given board B.
     *  Nondeterministic; chooses randomly from moves that yield
     *  highest static value. */
    int staticEvalStrat(MutableBoard b) {
        ArrayList<Integer> moves = new ArrayList<Integer>();
        staticEvalAll(getSide(), b, moves);
        int index = getGame().randInt(moves.size());
        return moves.get(index);
    }

    /** Returns the largest static value side P can attain given initial
     *  board B. Changes MOVES to contain the moves P can take to get
     *  largest value; if MOVES is null, it is ignored. */
    int staticEvalAll(Side p, MutableBoard b, ArrayList<Integer> moves) {
        int maxValue = -LARGE_NUMBER;
        int currValue;
        for (int i = 0; i < b.max(); i += 1) {
            if (b.isLegal(getSide(), i)) {
                currValue = tryMoveStatic(p, i, b);
                if (currValue > maxValue) {
                    maxValue = currValue;
                    if (moves != null) {
                        moves.clear();
                    }
                }
                if (currValue == maxValue && moves != null) {
                    moves.add(i);
                }
            }
        }
        return maxValue;
    }

    /** Returns the smallest index larger than MIN that the AI can
     *  legally move to on BOARD, -1 if no such index exists. */
    int findLegalMove(int min, Board board) {
        for (int i = 0; i < board.max(); i += 1) {
            if (board.isLegal(getSide(), i)) {
                return i;
            }
        }
        return -1;
    }

    /** Returns a legal move on BOARD, -1 if no such move. */
    int findLegalMove(Board board) {
        return findLegalMove(0, board);
    }



    /** Return the minimum of CUTOFF and the minmax value of board B
     *  (which must be mutable) for player P to a search depth of D
     *  (where D == 0 denotes statically evaluating just the next move).
     *  If MOVES is not null and CUTOFF is not exceeded, set MOVES to
     *  a list of all highest-scoring moves for P; clear it if
     *  non-null and CUTOFF is exceeded. the contents of B are
     *  invariant over this call. */
    private int minmax(Side p, Board b, int d, int cutoff,
                       ArrayList<Integer> moves) {
        Side winner = b.getWinner();
        if (winner == p) { return LARGE_NUMBER; }
        if (winner == p.opposite()) { return -LARGE_NUMBER; }
        if (d == 0) { return staticEvalAll(p, (MutableBoard) b, moves); }

        int bestSoFar = -LARGE_NUMBER;
        for (int i = 0; i < b.max(); i += 1) {
            if (!b.isLegal(p, i)) {
                continue;
            }
            b.addSpot(p, i);
            int value = -minmax(p.opposite(), b,
                d - 1, -bestSoFar, null);
            b.undo();

            if (value > bestSoFar) {
                bestSoFar = value;
                if (value > cutoff) {
                    assert moves == null;
                    break;
                }
                if (moves != null) {
                    moves.clear();
                }
            }
            if (value == bestSoFar && moves != null) {
                moves.add(i);
            }
        }
        return bestSoFar;
    }

    /** Returns a move on board B using minimax. */
    int minimaxStrat(MutableBoard b) {
        ArrayList<Integer> moves = new ArrayList<Integer>();
        staticEvalAll(getSide(), b, moves);
        minmax(getSide(), b, DEPTH, LARGE_NUMBER, moves);
        int index = getGame().randInt(moves.size());
        return moves.get(index);
    }

    /** Returns heuristic value of board B for player P.
     *  Higher is better for P. */
    private int staticEval(Side p, Board b) {
        Side winner = b.getWinner();
        if (winner == p) { return LARGE_NUMBER; }
        if (winner == p.opposite()) { return -LARGE_NUMBER; }
        int extra = 0;
        for (int i = 0; i < b.size(); i += 1) {
            for (int j : new int[]
            {i, b.max() - i - 1, i * b.size(),
                (i + 1) * b.size() - 1}) {
                if (b.get(j).getSide() == p) {
                    extra += 1;
                }
            }
        }
        return extra + b.numOfSide(p) - b.numOfSide(p.opposite());
    }
}
