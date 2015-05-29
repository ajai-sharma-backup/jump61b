package jump61;

import ucb.gui.TopLevel;
import ucb.gui.LayoutSpec;

import java.io.PrintWriter;
import java.io.Writer;

import java.util.Observable;
import java.util.Observer;

import static jump61.Side.*;

/** The GUI controller for jump61.  To require minimal change to textual
 *  interface, we adopt the strategy of converting GUI input (mouse clicks)
 *  into textual commands that are sent to the Game object through a
 *  a Writer.  The Game object need never know where its input is coming from.
 *  A Display is an Observer of Games and Boards so that it is notified when
 *  either changes.
 *
 *  @author Ajai K. Sharma
 */
class Display extends TopLevel implements Observer {

    /** A new window with given TITLE displaying GAME, and using COMMANDWRITER
     *  to send commands to the current game. */
    Display(String title, Game game, Writer commandWriter) {
        super(title, true);
        _game = game;
        _board = game.getBoard();
        _commandOut = new PrintWriter(commandWriter);
        _boardWidget = new BoardWidget(game, _commandOut);
        add(_boardWidget, new LayoutSpec("y", 1, "width", 2));

        addMenuButton("Game->Restart", "restart");
        addMenuButton("Players->Red->Set to auto", "autoRed");
        addMenuButton("Players->Blue->Set to auto", "autoBlue");
        addMenuButton("Players->Red->Set to manual", "manualRed");
        addMenuButton("Players->Blue->Set to manual", "manualBlue");

        addMenuButton("Size->2", "size");
        addMenuButton("Size->3", "size");
        addMenuButton("Size->4", "size");
        addMenuButton("Size->5", "size");
        addMenuButton("Size->6", "size");
        addMenuButton("Size->7", "size");
        addMenuButton("Size->8", "size");
        addMenuButton("Size->9", "size");
        addMenuButton("Size->10", "size");
        addMenuButton("Game->Quit", "quit");

        _board.addObserver(this);
        _game.addObserver(this);
        display(true);
        _game.play();
        restart("");
        _boardWidget.repaint();
    }

    /** Response to "Restart" button click. */
    void restart(String dummy) {
        _boardWidget.repaint();
        _commandOut.printf("clear%n");
        _commandOut.printf("start%n");
        _boardWidget.repaint();
    }

    /** Response to "Quit" button click. */
    void quit(String dummy) {
        _boardWidget.repaint();
        System.exit(0);
    }

    /** Response to "Size" button click.
     *  @param message is the thing on the button that
     *  gets parsed. */
    void size(String message) {
        _boardWidget.repaint();
        int n = Integer.parseInt(message.split(">")[1]);
        _commandOut.printf("size %d%n", n);
        _boardWidget.repaint();
    }

    /** Response to "Manual" button click. */
    void manualRed(String dummy) {
        _boardWidget.repaint();
        _commandOut.printf("manual Red%n");
        _commandOut.printf("start%n");
        _boardWidget.repaint();
    }

    /** Response to "Manual" button click. */
    void manualBlue(String dummy) {
        _boardWidget.repaint();
        _commandOut.printf("manual Blue%n");
        _commandOut.printf("start%n");
        _boardWidget.repaint();
    }

    /** Response to "Auto" button click. */
    void autoRed(String dummy) {
        _boardWidget.repaint();
        _commandOut.printf("auto Red%n");
        _commandOut.printf("start%n");
        _boardWidget.repaint();
    }

    /** Response to "Auto" button click. */
    void autoBlue(String dummy) {
        _boardWidget.repaint();
        _commandOut.printf("auto Blue%n");
        _commandOut.printf("start%n");
        _boardWidget.repaint();
    }




    @Override
    public void update(Observable obs, Object obj) {
        _boardWidget.update();
        frame.pack();
        _boardWidget.repaint();
    }

    /** The current game that I am controlling. */
    private Game _game;
    /** The board maintained by _game (readonly). */
    private Board _board;
    /** The widget that displays the actual playing board. */
    private BoardWidget _boardWidget;
    /** Writer that sends commands to our game. */
    private PrintWriter _commandOut;
}
