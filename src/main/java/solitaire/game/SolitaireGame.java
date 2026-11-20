package solitaire.game;

import solitaire.enums.GameStatus;

import solitaire.move.Move;
import solitaire.move.MoveValidator;
import solitaire.enums.Suit;
import solitaire.model.Card;
import solitaire.pile.TableauPile;

import java.util.List;

/**
 * Owns a live board, validates moves, and tracks progress without exposing mutable state.
 * @author Farhiya Ayyub
 */
public final class SolitaireGame {
    private final GameState state;
    private int moves;
    private GameStatus status = GameStatus.RUNNING;

    /** Creates a game from a detached copy of the supplied board. */
    public SolitaireGame(GameState state) {
        state.verify();
        this.state = state.copy();
        if (state.won()) status = GameStatus.WON;
    }

    /** Returns a detached board snapshot so callers cannot modify the owned board. */
    public GameState state() {
        return state.copy();
    }

    /** Returns the number of accepted moves. */
    public int moves() {
        return moves;
    }

    /** Returns the current game status. */
    public GameStatus status() {
        return status;
    }

    /** Records the result; rejects WON unless the board is actually complete. */
    public void finish(GameStatus status) {
        java.util.Objects.requireNonNull(status, "status");
        if (status == GameStatus.WON && !state.won())
            throw new IllegalArgumentException("Cannot declare an unfinished board won");
        this.status = state.won() ? GameStatus.WON : status;
    }

    /** Validates and applies one move; returns false without mutation for an illegal move. */
    public boolean play(Move move) {
        if (!new MoveValidator().isLegal(state, move)) return false;
        apply(state, move);
        state.verify();
        moves++;
        if (state.won()) status = GameStatus.WON;
        return true;
    }

    /** Applies a legal move to the supplied mutable board; throws IllegalArgumentException for illegal moves. */
    public static void apply(GameState board, Move move) {
        if (!new MoveValidator().isLegal(board, move)) throw new IllegalArgumentException("Illegal move: " + move);
        switch (move.type()) {
            case DRAW -> {
                for (int drawIndex = 0; drawIndex < move.count(); drawIndex++) board.stock().drawTo(board.waste());
            }
            case RECYCLE -> board.stock().recycle(board.waste());
            case TABLEAU_TO_TABLEAU -> {
                TableauPile sourceColumn = board.tableau().get(move.source());
                TableauPile destinationColumn = board.tableau().get(move.destination());
                List<Card> movedCards = sourceColumn.removeSequence(move.count());
                destinationColumn.addSequence(movedCards);
            }
            case TABLEAU_TO_FOUNDATION -> {
                TableauPile sourceColumn = board.tableau().get(move.source());
                Card movedCard = sourceColumn.removeSequence(1).get(0);
                Suit destinationSuit = Suit.values()[move.destination()];
                board.foundation(destinationSuit).push(movedCard);
            }
            case WASTE_TO_TABLEAU -> board.tableau().get(move.destination()).addSequence(List.of(board.waste().pop()));
            case WASTE_TO_FOUNDATION -> board.foundation(Suit.values()[move.destination()]).push(board.waste().pop());
            case FOUNDATION_TO_TABLEAU ->
                board.tableau().get(move.destination()).addSequence(List.of(board.foundation(Suit.values()[move.source()]).pop()));
        }
    }
}
