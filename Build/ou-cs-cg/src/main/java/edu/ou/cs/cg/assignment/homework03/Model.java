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
	private List<Point> legalMoves = new ArrayList<>();
	private Point hoverSquare = null;
	private long  hoverStartTime = 0L;
	private static final long HOVER_DURATION = 400L;  // ms
	private Point ghostStartSquare;

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
		tiles[row][col] = new Tile(col, row, bounds);
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
				// only let current player pick their own pieces
				if (clickedPiece.isWhite() == game.isWhiteTurn()) {
				  selectedSquare = new Point(row, col);
				  setSelectedPiece(clickedPiece);
				  System.out.println("Piece selected: " + selectedPiece);
				  legalMoves = game.generateLegalMoves(row, col);
				}
				else {
				  System.out.println((game.isWhiteTurn() ? "White" : "Black") + " to move.  You clicked the opponent’s piece.");
				  clearSelection();
				}
			  }
		} else if(selectedPiece == clickedPiece){
			clearSelection();
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

	  /** Let the view (and selectSquare) know whose turn it is */
	public boolean isWhiteTurn() {
		return game.isWhiteTurn();
	}

	public Point getSelectedSquare() {
		return selectedSquare;
	}

	public void setHoverSquare(Point sq) {
		if (!Objects.equals(sq, hoverSquare)) {
			ghostStartSquare = (hoverSquare == null ? selectedSquare : hoverSquare);
		  	hoverSquare    = sq;
		  	hoverStartTime = (sq != null ? System.currentTimeMillis() : 0L);
		}
	}

	public Point getHoverSquare() {
		return hoverSquare;
	}

	public float getHoverProgress() {
		if (hoverSquare == null) return 0f;
		long dt = System.currentTimeMillis() - hoverStartTime;
		return Math.min(1f, dt / (float)HOVER_DURATION);
	}

	public Point getGhostStartSquare() {
		return ghostStartSquare;
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

	/** allow View to ask which squares to highlight */
	public List<Point> getLegalMoves() {
		return legalMoves;
	}

	public Piece getSelectedPiece() {
		return selectedPiece;
	}

	//**********************************************************************
	// private methods
	//**********************************************************************

	// clears and unselects piece
	private void clearSelection() {
		setSelectedPiece(null);
		selectedSquare = null;
		legalMoves.clear();
		hoverSquare = null;
		ghostStartSquare   = null;
 		hoverStartTime = 0L;
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
		protected boolean isGhost;

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
		public void setIsGhost(boolean value) {
			isGhost = value;
		}
		public boolean getIsGhost() {
			return isGhost;
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

		private boolean whiteTurn = true;

		public ChessGame() {
			board = new Board();
		}

		public Board getBoard(){
			return board;
		}

		private void toggleTurn() 
		{ 
			whiteTurn = !whiteTurn; 
		}

		/** true if it's White's turn, false if Black's turn */
		public boolean isWhiteTurn() {
			return whiteTurn;
		}
  
		public boolean makeMove(Move move) {

			if(move.pieceMoved == null){
				return false;
			}
			if (move.pieceMoved.isWhite() != whiteTurn) {
				return false;             // not your piece
			}
    		if (!isValidMove(move)) {
				return false;             // illegal pattern
			}

			System.out.println("move.pieceMoved " + move.pieceMoved);
			board.setPiece(move, move.pieceMoved);
			toggleTurn();
			return true;
		}

		private boolean isValidMove(Move move) {
			switch (move.pieceMoved.type) {
			  case PAWN:   return validatePawn(move);
			  case KNIGHT: return validateKnight(move);
			  case BISHOP: return validateSliding(move, /*diags=*/true, /*straights=*/false);
			  case ROOK:   return validateSliding(move, /*diags=*/false, /*straights=*/true);
			  case QUEEN:  return validateSliding(move, /*diags=*/true, /*straights=*/true);
			  case KING:   return validateKing(move);
			  default:     return false;
			}
		}
		
		/** Return all (row,col) this piece could legally move to from (r,c). */
		public List<Point> generateLegalMoves(int r, int c) {
			Piece p = board.getPiece(r, c);
			List<Point> moves = new ArrayList<>();
			if (p == null || p.isWhite() != whiteTurn) return moves;
		
			// scan every square
			for (int rr = 0; rr < 8; rr++) {
			for (int cc = 0; cc < 8; cc++) {
				Move m = new Move(r, c, rr, cc, p);
				if (isValidMove(m)) {
				moves.add(new Point(rr, cc));
				}
			}
			}
			return moves;
		}

		private boolean validatePawn(Move m) {
			int dr = m.endRow - m.startRow;
			int dc = m.endCol - m.startCol;
			Piece target = board.getPiece(m.endRow, m.endCol);
		  
			// 1) Single step forward
			boolean forward = (m.pieceMoved.isWhite() ? dr == -1 : dr == 1)
							  && dc == 0
							  && target == null;
		  
			// 2) Double step on first move
			boolean doubleForward = false;
			if (dc == 0) {
			  // White pawns start on row 6 and move to row 4 (dr = -2)
			  if (m.pieceMoved.isWhite() && m.startRow == 6 && dr == -2) {
				// both intermediate (row 5) and landing squares must be empty
				if (board.getPiece(5, m.startCol) == null && target == null) {
				  doubleForward = true;
				}
			  }
			  // Black pawns start on row 1 and move to row 3 (dr = +2)
			  else if (!m.pieceMoved.isWhite() && m.startRow == 1 && dr == 2) {
				if (board.getPiece(2, m.startCol) == null && target == null) {
				  doubleForward = true;
				}
			  }
			}
		  
			// 3) Standard diagonal capture
			boolean capture = Math.abs(dc) == 1
							  && (m.pieceMoved.isWhite() ? dr == -1 : dr == 1)
							  && target != null
							  && target.isWhite() != m.pieceMoved.isWhite();
		  
			return forward || doubleForward || capture;
		  }		  
		  
		  private boolean validateKnight(Move m) {
			int dr = Math.abs(m.endRow - m.startRow);
			int dc = Math.abs(m.endCol - m.startCol);
			Piece target = board.getPiece(m.endRow, m.endCol);
			return ((dr == 2 && dc == 1) || (dr == 1 && dc == 2))
				   && (target == null || target.isWhite() != m.pieceMoved.isWhite());
		}
		  
		  private boolean validateKing(Move m) {
			int dr = Math.abs(m.endRow - m.startRow);
			int dc = Math.abs(m.endCol - m.startCol);
			Piece target = board.getPiece(m.endRow, m.endCol);
			return dr <= 1 && dc <= 1
				   && (target == null || target.isWhite() != m.pieceMoved.isWhite());
		}
		  
		  private boolean validateSliding(Move m, boolean diags, boolean straights) {
			int dr = m.endRow - m.startRow, dc = m.endCol - m.startCol;
			int stepR = Integer.signum(dr), stepC = Integer.signum(dc);
		  
			// direction check
			if (dr == 0 && dc == 0) return false;
			if (!straights && (dr == 0 || dc == 0)) return false;
			if (!diags && Math.abs(dr) == Math.abs(dc) && dr != 0) return false;
			if (dr != 0 && dc != 0 && Math.abs(dr) != Math.abs(dc)) return false;
		  
			// path must be clear
			int r = m.startRow + stepR, c = m.startCol + stepC;
			while (r != m.endRow || c != m.endCol) {
			  if (board.getPiece(r, c) != null) return false;
			  r += stepR;
			  c += stepC;
			}
		  
			// destination must be empty or enemy
			Piece target = board.getPiece(m.endRow, m.endCol);
			return target == null || target.isWhite() != m.pieceMoved.isWhite();
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
			float somersaultAngle = 360.0f * t * 10f; // 2 full flips

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
