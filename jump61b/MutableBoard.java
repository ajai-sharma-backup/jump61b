package jump61;

import static jump61.Side.*;
import static jump61.Square.square;
import java.util.Stack;

/** A Jump61 board state that may be modified.
 *
 *  @author Ajai K. Sharma
 */
class MutableBoard extends Board {

    /** An N x N board in initial configuration. */
    MutableBoard(int N) {
        clear(N);
    }

    /** A board whose initial contents are copied from BOARD0, but whose
     *  undo history is clear. */
    MutableBoard(Board board0) {
        copy(board0);
        resetHistory();
    }

    @Override
    void clear(int N) {
        _size = N;
        _data = new Square[N * N];
        _numOfSide = new int[]{max(), 0, 0};
        for (int i = 0; i < max(); i += 1) {
            set(i, 1, WHITE);
        }
        resetHistory();
        announce();
    }

    @Override
    void copy(Board board) {
        clear(board.size());
        for (int i = 0; i < max(); i += 1) {
            internalSet(i, board.get(i));
        }
        for (Side s : Side.values()) {
            _numOfSide[s.ordinal()] = board.numOfSide(s);
        }
        resetHistory();
        announce();
    }

    /** Copy the contents of BOARD into me, without modifying my undo
     *  history.  Assumes BOARD and I have the same size. */
    private void internalCopy(MutableBoard board) {
        _data = new Square[max()];
        for (int i = 0; i < max(); i += 1) {
            internalSet(i, board.get(i));
        }
        for (Side s : Side.values()) {
            _numOfSide[s.ordinal()] = board.numOfSide(s);
        }
    }

    @Override
    int size() {
        return _size;
    }

    @Override
    Square get(int n) {
        return _data[n];
    }

    @Override
    int numOfSide(Side side) {
        return _numOfSide[side.ordinal()];
    }

    @Override
    int numPieces() {
        int acc = 0;
        for (int i : _numOfSide) {
            acc += i;
        }
        return acc;
    }

    @Override
    void addSpot(Side player, int r, int c) {
        addSpot(player, sqNum(r, c));
    }

    @Override
    void addSpot(Side player, int n) {
        markUndo();
        increment(player, n);
        boolean overflowPossible = true;
        while (getWinner() == null && overflowPossible) {
            overflowPossible = overflowAll();
        }
        announce();
    }

    /** Adds one spot of PLAYER's color to place
     *  N. Does not handle overflowing and will
     *  leave a square with more than the maximum
     *  allowed number of dots. */
    private void increment(Side player, int n) {
        int dots = get(n).getSpots() + 1;
        internalSet(n, dots, player);
    }

    /** If square N has more dots than neighbors,
     *  adds dots to its neighbors and sets N's dots
     *  to 1. Returns true if this is successful,
     *  otherwise false. */
    boolean overflow(int n) {
        if (get(n).getSpots() <= neighbors(n)) {
            return false;
        }

        int size = size();
        Side player = get(n).getSide();
        int[] neighbors = {n - size, n + size,
            n % size > 0          ? n - 1 : -1,
            n % size < (size - 1) ? n + 1 : -1,
        };

        internalSet(n, get(n).getSpots() - neighbors(n), player);

        for (int k : neighbors) {
            if (exists(k)) {
                increment(player, k);
            }
        }

        return true;
    }

    /** Calls overflow on all squares, returning true
     *  if any change took place. */
    boolean overflowAll() {
        boolean result = false;
        for (int i = 0; i < max(); i += 1) {
            result = overflow(i) || result;
        }
        return result;
    }

    @Override
    void set(int r, int c, int num, Side player) {
        set(sqNum(r, c), num, player);
    }

    @Override
    void set(int n, int num, Side player) {
        internalSet(n, num, player);
        resetHistory();
    }

    @Override
    /** Beginning of move marked with null.*/
    void undo() {
        Change c = _history.pop();
        while (c != null && !_history.empty()) {
            internalSet(c.ind(), c.sq());
            _history.pop();
            c = _history.pop();
        }
    }

    /** Record the beginning of a move in the undo history. */
    private void markUndo() {
        _history.push(null);
    }

    /** Set the contents of the square with index IND to SQ. Update counts
     *  of numbers of squares of each color.  */
    private void internalSet(int ind, Square sq) {
        Square old = get(ind);
        if (sq == null) {
            internalSet(ind, 1, WHITE);
            return;
        }
        if (old != null) {
            _numOfSide[old.getSide().ordinal()] -= old.getSpots();
            _numOfSide[sq.getSide().ordinal()] += sq.getSpots();
            createHistory(ind, old);
        }
        _data[ind] = sq;
    }

    /** Set the contents of the square with index IND to the square
     *  with color PLAYER and NUM spots. */
    private void internalSet(int ind, int num, Side player) {
        internalSet(ind, square(player, num));
    }

    /** Adds an change of setting square IND to the contents
     *  of square SQ to the history stack. */
    private void createHistory(int ind, Square sq) {
        _history.push(new Change(ind, sq));
    }

    /** Resets the undo history. */
    private void resetHistory() {
        _history = new Stack<Change>();
    }




    /** Notify all Observers of a change. */
    private void announce() {
        setChanged();
        notifyObservers();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MutableBoard)) {
            return obj.equals(this);
        } else {
            MutableBoard foo = (MutableBoard) obj;
            if (size() != foo.size()) {
                return false;
            }
            for (int i = 0; i < size() * size(); i += 1) {
                if (get(i) != foo.get(i)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int[] temp = {
                numOfSide(WHITE),
                numOfSide(RED),
                numOfSide(BLUE),
                get(0).hashCode(),
                get(size()).hashCode()
        };
        int acc = 0;
        for (int foo : temp) {
            acc ^= foo;
            acc <<= 5;
        }
        return acc;
    }

    /** Array containing the data of the board. */
    private Square[] _data;
    /** Stack containing the undo history of the board. */
    private Stack<Change> _history;
    /** The size of the board. */
    private int _size;


    /** Tuples would be nice. */
    private static class Change {
        /** Represents a change to the board of setting index IND to
         *  square SQ with a pseudo-tuple. */
        Change(int ind, Square sq) {
            _ind = ind;
            _sq = sq;
        }

        private final int _ind;
        /**  @return the index of the stored change */
        int ind() { return _ind; }
        private final Square _sq;
        /**  @return the square of the stored change */
        Square sq() { return _sq; }
    }

    /** Stores the number of spots owned by each side. */
    private int[] _numOfSide;
}
