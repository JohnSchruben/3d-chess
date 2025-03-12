//******************************************************************************
// Copyright (C) 2019-2025 University of Oklahoma Board of Trustees.
//******************************************************************************
// Last modified: Sat Feb 22 09:58:10 2025 by Chris Weaver
//******************************************************************************
// Major Modification History:
//
// 20190227 [weaver]:	Original file.
// 20240224 [weaver]:	Updated ViewPointUpdater to account for pixel scaling.
// 20250222 [weaver]:	Updated homework03 for easier carryover from homework02.
//
//******************************************************************************
//
// The model manages all of the user-adjustable variables utilized in the scene.
// (You can store non-user-adjustable scene data here too, if you want.)
//
// For each variable that you want to make interactive:
//
//   1. Add a member of the right type
//   2. Initialize it to a reasonable default value in the constructor.
//   3. Add a method to access a copy of the variable's current value.
//   4. Add a method to modify the variable.
//
// Concurrency management is important because the JOGL and the Java AWT run on
// different threads. The modify methods use the GLAutoDrawable.invoke() method
// so that all changes to variables take place on the JOGL thread. Because this
// happens at the END of GLEventListener.display(), all changes will be visible
// to the View.update() and render() methods in the next animation cycle.
//
//******************************************************************************

package edu.ou.cs.cg.assignment.homework03;

//import java.lang.*;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.*;
import com.jogamp.opengl.*;
import edu.ou.cs.cg.utilities.Utilities;

//******************************************************************************

/**
 * The <CODE>Model</CODE> class.
 *
 * @author  Chris Weaver
 * @version %I%, %G%
 */
public final class Model
{
	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) variables
	private final View	view;

	// Model variables
	private ChessGame game;
	

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public Model(View view)
	{
		this.view = view;

		// Initialize user-adjustable variables (with reasonable default values)
		game = new ChessGame();
	}

	//**********************************************************************
	// Public Methods (Access Variables)
	//**********************************************************************

	public Piece getPiece(int row, int col)
	{
		return this.game.getBoard().getPiece(row, col);
	}

	//**********************************************************************
	// Public Methods (Modify Variables)
	//**********************************************************************

	public void	BasicSet()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				
			}
		});;
	}
	
	public void	MakeMove(Move move)
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				game.makeMove(move);
			}
		});;
	}


	public void	PointSet(Point q)
	{
		view.getCanvas().invoke(false, new ViewPointUpdater(q) {
			public void	update(double[] p) {
				
			}
		});;
	}

	//**********************************************************************
	// Inner Classes
	//**********************************************************************

	// Convenience class to simplify the implementation of most updaters.
	private abstract class BasicUpdater implements GLRunnable
	{
		public final boolean	run(GLAutoDrawable drawable)
		{
			GL2	gl = drawable.getGL().getGL2();

			update(gl);

			return true;	// Let animator take care of updating the display
		}

		public abstract void	update(GL2 gl);
	}

	// Convenience class to simplify updates in cases in which the input is a
	// single point in view coordinates (integers/pixels).
	private abstract class ViewPointUpdater extends BasicUpdater
	{
		private final Point	q;

		public ViewPointUpdater(Point q)
		{
			//this.q = q;

			// Account for pixel scaling in point coordinates of input events
			this.q = Utilities.convertToPixelUnits(view.getCanvas(), q);
		}

		public final void	update(GL2 gl)
		{
			int		h = view.getHeight();
			double[]	p = Utilities.mapViewToScene(gl, q.x, h - q.y, 0.0);

			update(p);
		}

		public abstract void	update(double[] p);
	}


	
public class Board {
	private Piece[][] squares = new Piece[8][8];
	
	public Board() {
		initialize();  
	}

	private void initialize(){
		//black
		squares[0][0] = new Rook(false);
		squares[0][1] = new Knight(false);
		squares[0][2] = new Bishop(false);
		squares[0][3] = new Queen(false);
		squares[0][4] = new King(false);
		squares[0][5] = new Bishop(false);
		squares[0][6] = new Knight(false);
		squares[0][7] = new Rook(false);
	
		// pawns
		for (int col = 0; col < 8; col++) {
			squares[1][col] = new Pawn(false);
		}
	
		for (int row = 2; row <= 5; row++) {
			for (int col = 0; col < 8; col++) {
				squares[row][col] = null;
			}
		}
	
		// white pawns
		for (int col = 0; col < 8; col++) {
			squares[6][col] = new Pawn(true);
		}
	
		// white
		squares[7][0] = new Rook(true);
		squares[7][1] = new Knight(true);
		squares[7][2] = new Bishop(true);
		squares[7][3] = new Queen(true);
		squares[7][4] = new King(true);
		squares[7][5] = new Bishop(true);
		squares[7][6] = new Knight(true);
		squares[7][7] = new Rook(true);
	}

	public Piece getPiece(int row, int col) {
		return squares[row][col];
	}

	public void setPiece(int row, int col, Piece piece) {
		squares[row][col] = piece;
	}
}

public abstract class Piece {
	protected boolean isWhite;      
	protected PieceType type;       
	
	public Piece(boolean isWhite, PieceType type) {
		this.isWhite = isWhite;
		this.type = type;
	}
	
	public boolean isWhite() {
		return isWhite;
	}

}

public class Rook extends Piece {
	public Rook(boolean isWhite) {
		super(isWhite, PieceType.ROOK);
	}
}

public class Knight extends Piece {
	public Knight(boolean isWhite) {
		super(isWhite, PieceType.KNIGHT);
	}
}

public class Bishop extends Piece {
	public Bishop(boolean isWhite) {
		super(isWhite, PieceType.BISHOP);
	}
}
public class Pawn extends Piece {
	public Pawn(boolean isWhite) {
		super(isWhite, PieceType.PAWN);
	}
}
public class King extends Piece {
	public King(boolean isWhite) {
		super(isWhite, PieceType.KING);
	}
}
public class Queen extends Piece {
	public Queen(boolean isWhite) {
		super(isWhite, PieceType.QUEEN);
	}
}
public class ChessGame {
	private Board board;
	private boolean whiteToMove;

	public ChessGame() {
		board = new Board();
		whiteToMove = true;
	}

	public Board getBoard(){
		return board;
	}

	public boolean makeMove(Move move) {
		return false;
	}
}

public enum PieceType {
    PAWN,
    KNIGHT,
    BISHOP,
    ROOK,
    QUEEN,
    KING
}

public class Move {
	private final int startRow;
	private final int startCol;
	private final int endRow;
	private final int endCol;
	private final Piece pieceMoved;
	private Piece pieceCaptured;  
	private boolean isEnPassant;
	private boolean isCastling;

	public Move(int startRow, int startCol, int endRow, int endCol, Piece pieceMoved) {
		this.startRow = startRow;
		this.startCol = startCol;
		this.endRow = endRow;
		this.endCol = endCol;
		this.pieceMoved = pieceMoved;
	}
}
}


//******************************************************************************
