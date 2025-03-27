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
import java.awt.Rectangle;

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
	private int camPosition; 				//The current of the many numbered positions of the camera
	private boolean isHighCam; 				//Whether the camera is high up.
	private Tile[][] tiles;					// used to hit test
	private Piece selectedPiece;
	private Point selectedSquare = null;     // the square where the selected piece is

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public Model(View view)
	{
		this.view = view;

		// Initialize user-adjustable variables (with reasonable default values)
		//board side: Black left and white right.
		game = new ChessGame();
		camPosition = 3; 
		isHighCam = true;
		tiles = new Tile[8][8]; 
		selectedPiece = null;
	}

	//**********************************************************************
	// Public Methods (Access Variables)
	//**********************************************************************

	public Tile getTileAt(Point screenPoint) {
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				if (tiles[row][col].contains(screenPoint)) {
					return tiles[row][col];
				}
			}
		}
		return null;
	}

	// gets called on the view on every render
	public void setTile(int row, int col, Rectangle bounds) {
		tiles[row][col] = new Tile(row, col, bounds);
	}
	
	public void selectSquare(Point p)
	{
		Tile tile = getTileAt(p);

		if (tile == null) {
			System.out.println("Clicked outside the board.");
			clearSelection();
			return;
		}

		int row = tile.row;
		int col = tile.col;
		System.out.println("Square clicked: " + row + " " + col);

		Piece clickedPiece = game.board.getPiece(row, col);

		if (selectedPiece == null) {
			// First click — try to select a piece
			if (clickedPiece != null) {
				selectedSquare = new Point(row, col);
				setSelectedPiece(clickedPiece);
				System.out.println("Piece selected: " + selectedPiece);
			}
		} else if(selectedPiece == clickedPiece){
			setSelectedPiece(null);
		} else {
			// Second click — try to move the selected piece
			Point start = selectedSquare;
			Point end = new Point(row, col);

			Move move = new Move(start.x, start.y, end.x, end.y, selectedPiece);
			if (game.makeMove(move)) {
				System.out.println("Move made.");
			} else {
				System.out.println("Invalid move.");
			}
			clearSelection(); // Always clear after attempting move
		}
	}

	public Piece getPiece(int row, int col)
	{
		return this.game.getBoard().getPiece(row, col);
	}

	public ArrayList<Piece> getAnimatingPieces()
	{
		return this.game.getBoard().getAnimatingPieces();
	}

	public ArrayList<Piece> getCaptures()
	{
		return this.game.getBoard().getCaptures();
	}

	public int getCamPosition() {
		return camPosition;
	}

	public boolean getIsHighCam() {
		return isHighCam;
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

	//set the camera position to one of the preset positions
	public void	setCamera(char move)
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				
				switch(move)
				{
					case 'l':
						camPosition = (camPosition + 3) % 4; //move the camera to the position left of it
					break;	
					case 'r':
						camPosition = (camPosition + 1) % 4; //move the camera to the position right of it
					break;
					case 'f':
						camPosition = (camPosition + 2) % 4; //move the camera to the position in front of it
					break;
					case 'h':
						isHighCam = !isHighCam; //toggle high cam setting
					break;
				}

			view.getCanvas().display();
			}
		});;
	}
	public void	updateAnimations()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				game.board.updateAnimations();
			}
		});;
	}
	public void	makeMove(Move move)
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
	// private methods
	//**********************************************************************

	// clears and unselects piece
	private void clearSelection() {
		setSelectedPiece(null);
		selectedSquare = null;
	}

	// sets and selects piece
	private void setSelectedPiece(Piece piece){
		if(selectedPiece != null){
			selectedPiece.setIsSelected(false);
		}

		selectedPiece = piece;
		if (selectedPiece != null){
			selectedPiece.setIsSelected(true);
		}
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
		private ArrayList<Piece> captures = new ArrayList<Piece>();
		private final ArrayList<Piece> animatingPieces = new ArrayList<>();

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

		public ArrayList<Piece> getAnimatingPieces()
		{
			return animatingPieces;
		}
		public ArrayList<Piece> getCaptures()
		{
			return captures;
		}
		public void updateAnimations() {
			Iterator<Piece> it = animatingPieces.iterator();
			while (it.hasNext()) {
				Piece p = it.next();
				p.tickAnimation();
		
				if (!p.isAnimating()) {
					System.out.println("@!isAnimating");
		
					Animation a = p.getAnimation();
					if (a instanceof MoveAnimation) {
						System.out.println("Animation is MoveAnimation!");
						MoveAnimation anim = (MoveAnimation) a;
						squares[anim.getDestRow()][anim.getDestCol()] = p;
					}
					else if (a instanceof CapturedAnimation)  {
						System.out.println("Animation is CapturedAnimation!");
						CapturedAnimation anim = (CapturedAnimation) a;
						//squares[anim.getDestRow()][anim.getDestCol()] = null;
						captures.add(p);
					}
					else if (a instanceof AttackAnimation)  {
						System.out.println("Animation is AttackAnimation!");
						AttackAnimation anim = (AttackAnimation) a;
						squares[anim.getDestRow()][anim.getDestCol()] = p;
					}
		
					p.setAnimation(null); // Now safe to clear
					it.remove();
				} 
			}
		}
		
		public void setPiece(Move move, Piece piece) {
			Piece existingPiece = squares[move.endRow][move.endCol];
			if (existingPiece != null){
				existingPiece.setAnimation(new CapturedAnimation(existingPiece.isWhite, move.endRow, move.endCol, 60));
				animatingPieces.add(existingPiece);
				squares[move.endRow][move.endCol] = null;
				piece.setAnimation(new AttackAnimation(move.startRow, move.startCol, move.endRow, move.endCol, 30));
			}
			else{
				piece.setAnimation(new MoveAnimation(move.startRow, move.startCol, move.endRow, move.endCol, 60));
			}

			animatingPieces.add(piece);
			squares[move.startRow][move.startCol] = null;
		}
	}

	public abstract class Piece {
		protected boolean isWhite;     
		private Animation animation;

		protected PieceType type;       
		protected boolean isSelected;
		public Animation getAnimation() {
			return animation;
		}

		public void setAnimation(Animation animation) {
			this.animation = animation;
		}

		public boolean isAnimating() {
			return animation != null && !animation.isDone();
		}

		public void tickAnimation() {
			if (animation != null) {
				animation.tick();
			}
		}

		public Piece(boolean isWhite, PieceType type) {
			this.isWhite = isWhite;
			this.type = type;
		}
		public void setIsSelected(boolean value) {
			isSelected = value;
		}
		public boolean getIsSelected() {
			return isSelected;
		}
		public boolean isWhite() {
			return isWhite;
		}

		@Override
		public String toString(){
			return type + " " + (isWhite ? "white" : "black");
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

		public ChessGame() {
			board = new Board();
		}

		public Board getBoard(){
			return board;
		}

		public boolean makeMove(Move move) {

			if(move.pieceMoved == null){
				return false;
			}

			System.out.println("move.pieceMoved " + move.pieceMoved);
			board.setPiece(move, move.pieceMoved);
			return true;
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
		public Move(int startRow, int startCol, int endRow, int endCol, Piece pieceMoved) {
			this.startRow = startRow;
			this.startCol = startCol;
			this.endRow = endRow;
			this.endCol = endCol;
			this.pieceMoved = pieceMoved;
		}
	}

	public class Tile {
		public final int row;
		public final int col;
		public final Rectangle bounds; // screen-space bounds (2D)

		public Tile(int col, int row, Rectangle bounds) {
			this.row = row;
			this.col = col;
			this.bounds = bounds;
		}

		public boolean contains(Point p) {
			return bounds.contains(p);
		}
	}

	public abstract class Animation {
		protected int counter = 0;
		protected int duration;
	
		public Animation(int duration) {
			this.duration = duration;
		}
	
		public void tick() {
			counter++;
		}
	
		public boolean isDone() {
			return counter >= duration;
		}
	
		public float getProgress() {
			return Math.min(1.0f, (float) counter / duration);
		}
	
		public abstract void applyTransform(GL2 gl,  float startX, float startZ, float endX, float endZ) ;
		public abstract int getStartRow();
		public abstract int getStartCol();
		public abstract int  getDestRow();
		public abstract int getDestCol();
	}
	public class MoveAnimation extends Animation {
		private final int destRow, destCol;
		private final int startRow, startCol;
	
		public MoveAnimation(int startRow, int startCol, int destRow, int destCol, int duration) {
			super(duration);
			this.destRow = destRow;
			this.destCol = destCol;
			this.startRow = startRow;
			this.startCol = startCol;
		}
	
		@Override
		public int getStartRow() { return startRow; }
		@Override
		public int getStartCol() { return startCol; }
		@Override
		public int getDestRow() { return destRow; }
		@Override
		public int getDestCol() { return destCol; }
	
		@Override
		public void applyTransform(GL2 gl,  float startX, float startZ, float endX, float endZ)  {
			float t = getProgress();
			float x = startX + (endX - startX) * t;
			float z = startZ + (endZ - startZ) * t;
		
			gl.glTranslatef(x, 0.0f, z);
		}
	}
	public class CapturedAnimation extends Animation {
		private final int destRow, destCol;
		private final boolean isWhite;
		public CapturedAnimation(boolean isWhite, int destRow, int destCol, int duration) {
			super(duration);
			this.isWhite = isWhite;
			this.destRow = destRow;
			this.destCol = destCol;
		}
	
		@Override
		public int getStartRow() { return destRow; }
		@Override
		public int getStartCol() { return destCol; }
		@Override
		public int getDestRow() { return isWhite ? 0:7; }
		@Override
		public int getDestCol() { return isWhite ? 0:7; }
	
		@Override
		public void applyTransform(GL2 gl, float startX, float startZ, float endX, float endZ) {
			float t = getProgress();

			// Interpolate position
			float x = startX + (endX - startX) * t;
			float z = startZ + (endZ - startZ) * t;

			// Movement direction in degrees (so piece rotates forward in its movement direction)
			float dx = endX - startX;
			float dz = endZ - startZ;
			float movementAngle = (float) Math.toDegrees(Math.atan2(dx, dz)); // rotate around Y to face movement

			// Somersault angle (full forward flips)
			float somersaultAngle = 360.0f * t * 2f; // 2 full flips

			// Apply transforms
			gl.glTranslatef(x, 2.0f, z);                    // Move to position
			gl.glRotatef(movementAngle, 0, 1, 0);           // Face movement direction
			gl.glRotatef(somersaultAngle, 1, 0, 0);         // Somersault forward (rotate around X)
		}

	}
	public class AttackAnimation extends Animation {
		private final int destRow, destCol;
		private final int startRow, startCol;
	
		public AttackAnimation(int startRow, int startCol, int destRow, int destCol, int duration) {
			super(duration);
			this.destRow = destRow;
			this.destCol = destCol;
			this.startRow = startRow;
			this.startCol = startCol;
		}
	
		@Override
		public int getStartRow() { return startRow; }
		@Override
		public int getStartCol() { return startCol; }
		@Override
		public int getDestRow() { return destRow; }
		@Override
		public int getDestCol() { return destCol; }
	
		@Override
		public void applyTransform(GL2 gl, float startX, float startZ, float endX, float endZ) {
			float t = getProgress();
			
			// Interpolate position
			float x = startX + (endX - startX) * t;
			float z = startZ + (endZ - startZ) * t;

			// Compute direction angle in degrees
			float dx = endX - startX;
			float dz = endZ - startZ;
			float angle = (float) Math.toDegrees(Math.atan2(dx, dz)); // rotate around axis perpendicular to movement

			// Lean back then return upright as t goes from 0 → 1
			float leanAmount = (float)(Math.sin(Math.PI * t) * 15.0f); // up to 15 degrees lean

			// Transform
			gl.glTranslatef(x, 0.0f, z);                   // Move piece
			gl.glRotatef(angle, 0.0f, 1.0f, 0.0f);          // Rotate to face direction of movement
			gl.glRotatef(-leanAmount, 1.0f, 0.0f, 0.0f);    // Lean forward/backward around X-axis
		}
	}
}

//******************************************************************************
