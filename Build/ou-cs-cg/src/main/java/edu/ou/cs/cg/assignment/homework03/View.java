package edu.ou.cs.cg.assignment.homework03;

//import java.lang.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.nio.channels.Pipe;
import java.text.DecimalFormat;
import java.util.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.*;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;

import edu.ou.cs.cg.assignment.homework03.Model.Piece;
import edu.ou.cs.cg.utilities.Utilities;


//******************************************************************************

/**
 * The <CODE>View</CODE> class.<P>
 */
public final class View
	implements GLEventListener
{
	//**********************************************************************
	// Private Class Members
	//**********************************************************************

	private static final int			DEFAULT_FRAMES_PER_SECOND = 60;
	private static final DecimalFormat	FORMAT = new DecimalFormat("0.000");

	//**********************************************************************
	// Public Class Members
	//**********************************************************************

	public static final GLUT			MYGLUT = new GLUT();
	public static final Random			RANDOM = new Random();

	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) variables
	private final GLJPanel				canvas;
	private GLU	glu;
	private GLUT glut;
	private GLUquadric quadric;

	private int							w;			// Canvas width
	private int							h;			// Canvas height

	private TextRenderer				renderer;

	private final FPSAnimator			animator;
	private int							k;			// Frame counter

	private final Model					model;

	private final KeyHandler			keyHandler;
	private final MouseHandler			mouseHandler;

	// board information
	private float[] boardPositions; 
	private final float tileSize; //width and length of each tile
	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public View(GLJPanel canvas)
	{
		this.canvas = canvas;

		// Initialize rendering
		k = 0;
		canvas.addGLEventListener(this);

		// Initialize model (scene data and parameter manager)
		model = new Model(this);
		tileSize = 2.2f;
		
		//Use as the x and z cords for each of the postions 0 through 7. 0 by 0 is the top left position.
		boardPositions = new float[]{
			tileSize / 2, 3 * tileSize / 2, 5 * tileSize / 2, 7 * tileSize / 2,
			9 * tileSize / 2, 11 * tileSize / 2, 13 * tileSize / 2, 15 * tileSize / 2
		};

		// Initialize controller (interaction handlers)
		keyHandler = new KeyHandler(this, model);
		mouseHandler = new MouseHandler(this, model);

		// Initialize animation
		animator = new FPSAnimator(canvas, DEFAULT_FRAMES_PER_SECOND);
		animator.start();

		glu = new GLU();
		glut = new GLUT();
	}

	//**********************************************************************
	// Getters and Setters
	//**********************************************************************

	public GLJPanel	getCanvas()
	{
		return canvas;
	}

	public int	getWidth()
	{
		return w;
	}

	public int	getHeight()
	{
		return h;
	}

	//**********************************************************************
	// Override Methods (GLEventListener)
	//**********************************************************************

	public void	init(GLAutoDrawable drawable)
	{
		w = drawable.getSurfaceWidth();
		h = drawable.getSurfaceHeight();

		renderer = new TextRenderer(new Font("Monospaced", Font.PLAIN, 12),
									true, true);

		initPipeline(drawable);
	}

	public void	dispose(GLAutoDrawable drawable)
	{
		renderer = null;
	}

	public void	display(GLAutoDrawable drawable)
	{
		update(drawable);
		render(drawable);
	}

	public void	reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
	{
		GL2 gl = drawable.getGL().getGL2();
		
        if (h == 0) h = 1;
        float aspect = (float) w / h;

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0, aspect, 1.0, 100.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
		
		/*
		this.w = w;
		this.h = h;
		*/
	}

	//**********************************************************************
	// Private Methods (Rendering)
	//**********************************************************************

	private void	update(GLAutoDrawable drawable)
	{
		k++;									// Advance animation counter
		model.updateAnimations();
	}

	private void	render(GLAutoDrawable drawable)
	{
		GL2	gl = drawable.getGL().getGL2();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);	// Clear the buffer

		drawMain(gl);							// Draw scene content
		drawMode(drawable);						// Draw overlaid mode text

		gl.glFlush();							// Finish and display
	}

	//**********************************************************************
	// Private Methods (Pipeline)
	//**********************************************************************

	private void	initPipeline(GLAutoDrawable drawable)
	{
		GL2	gl = drawable.getGL().getGL2();

		gl.glClearColor(0.6f, 0.85f, 0.92f, 1.0f);	// baby blue background

		// See the com.jogamp.opengl.GL API for more on the following settings

		gl.glEnable(GL2.GL_DEPTH_TEST);  // Enable depth testing

		// Smooth points and lines for easier viewing on Hi-DPI displays
		gl.glEnable(GL2.GL_POINT_SMOOTH);	// Turn on point anti-aliasing
		gl.glEnable(GL2.GL_LINE_SMOOTH);	// Turn on line anti-aliasing

		// Enabling alpha blending to allow use of translucent colors in 2-D
		// (Warning: Translucency in 3-D is more difficult and expensive!)
		gl.glEnable(GL.GL_BLEND);			// Turn on color channel blending
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		quadric = glu.gluNewQuadric();
        glu.gluQuadricNormals(quadric, GLU.GLU_SMOOTH);
	}

	// Set the camera in the 3D space
	private void	setCamera(GL2 gl)
	{
		gl.glLoadIdentity(); // Set to identity matrix

		//Cam move view around board by switching between different camera postitions and angles with the keyboard.
		switch(model.getCamPosition())
		{
			case 0:
				if (model.getIsHighCam())
					// Set up the camera (eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ)
					// X(left/right), Y(up/down), Z(forward/back)
					glu.gluLookAt(8.8, 13, 29, 8.8, -1, 8.8, 0, 1, 0); 
				else
					glu.gluLookAt(8.8, 4, 29, 8.8, 1, 8.8, 0, 1, 0); //side cam: black on left and white on right
			break;
			case 1:
				if (model.getIsHighCam())
					glu.gluLookAt(29, 13, 8.8, 8.8, -1, 8.8, 0, 1, 0); 
				else
					glu.gluLookAt(29, 4, 8.8, 8.8, 1, 8.8, 0, 1, 0); //cam for white piece player
			break;
			case 2:
				if (model.getIsHighCam())
					glu.gluLookAt(8.8, 13, -11.4, 8.8, -1, 8.8, 0, 1, 0); 
				else
					glu.gluLookAt(8.8, 4, -11.4, 8.8, 1, 8.8, 0, 1, 0); //side cam: white on left and black on right
			break;
			case 3:
				if (model.getIsHighCam())
					glu.gluLookAt(-11.4, 13, 8.8, 8.8, -1, 8.8, 0, 1, 0); 
				else
					glu.gluLookAt(-11.4, 4, 8.8, 8.8, 1, 8.8, 0, 1, 0); //cam for black piece player
			break;
		}
		
	}

	//**********************************************************************
	// Private Methods (Mode Text Overlay)
	//**********************************************************************

	private void	drawMode(GLAutoDrawable drawable)
	{
		renderer.beginRendering(w, h);

		// Draw all text in black
		renderer.setColor(0.0f, 0.0f, 0.0f, 1.0f);

		// Vertical position of text labels
		int voff = Utilities.TEXT_SPACING;

		String	camera = "Camera:";
		renderer.draw(camera, 2, h - voff);	// Draw text for "Camera"
		voff += Utilities.TEXT_SPACING;		// Move down one line

		// Draw camera position controls
		String	cameraControls = "A (left), W (forward), D (right)";
		renderer.draw(cameraControls, 2, h - voff);
		voff += Utilities.TEXT_SPACING;		// Move down one line

		// Draw camera angle controls
		String	highCam = "S (toggle highcam)";
		renderer.draw(highCam, 2, h - voff);
		voff += Utilities.TEXT_SPACING;		// Move down one line

		// Draw piece controls
		String	pieceControl = "Click tiles to select and move pieces";
		renderer.draw(pieceControl, 2, h - voff);
		voff += Utilities.TEXT_SPACING;		// Move down one line

		// Draw current turn
		String turnMsg = model.isWhiteTurn() ? "White's turn" : "Black's turn";
		renderer.draw("Turn: " + turnMsg, 2, h - voff);
		voff += Utilities.TEXT_SPACING;
		
		renderer.endRendering();
	}

	//**********************************************************************
	// Private Methods (Scene Contents)
	//**********************************************************************

	private void	drawMain(GL2 gl)
	{
		setCamera(gl);
		
		drawChessSet(gl); //Board and pieces
		
	}

	private void drawChessSet(GL2 gl) {
		
		// Draw board in middle of space. Keep still.
		drawBoardBase(gl, tileSize);
		drawBoardTiles(gl, tileSize);
		drawHighlights(gl, tileSize);
		drawGhost(gl);
		drawCapturedPieces(gl, tileSize);

		//Draw all the pieces in their starting positions
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
			  Model.Piece piece = model.getPiece(row, col);
			  if (piece != null) {
				float x = boardPositions[col];
				float z = boardPositions[row];
				drawPiece(gl, x, 0, z, piece);
			  }
			}
		  }		  

		// animate last
		drawAnimatingPieces(gl, tileSize);
		
		return;
	}

	private void drawAnimatingPieces(GL2 gl, float tileSize){
		for (Model.Piece piece : model.getAnimatingPieces()) {
			gl.glPushMatrix();

			Model.Animation anim = piece.getAnimation();
	
			// compute start and end positions
			float startX = boardPositions[anim.getStartCol()];
			float startZ = boardPositions[anim.getStartRow()];
			float endX   = boardPositions[anim.getDestCol()];
			float endZ   = boardPositions[anim.getDestRow()];

	
			// apply animation transform (translates to correct spot)
			anim.applyTransform(gl, startX, startZ, endX, endZ);
	
			// draw at origin (since transform moved us)
			drawPiece(gl, 0, 0, 0, piece);
	
			gl.glPopMatrix();
		}
	}
	
	private void drawPiece(GL2 gl, float x, float y, float z, Model.Piece piece){
		switch(piece.type)
		{
			case PAWN:
			drawPawn(gl, x, y, z, piece);
			break;	
			case KNIGHT:
			drawKnight(gl, x, y, z, piece);
			break;
			case ROOK:
			drawRook(gl, x, y, z, piece);
			break;
			case BISHOP:
			drawBishop(gl, x, y, z, piece);
			break;
			case QUEEN:
			drawQueen(gl, x, y, z, piece);
			break;
			case KING:
			drawKing(gl, x, y, z, piece);
			break;
		}
	}

	private void drawCapturedPieces(GL2 gl, float tileSize) {
		float y = 0.0f; // Exact same height as the board
	
		ArrayList<Model.Piece> whiteCaptured = new ArrayList<>();
		ArrayList<Model.Piece> blackCaptured = new ArrayList<>();
	
		for (Model.Piece piece : model.getCaptures()) {
			if (piece.isWhite()) {
				whiteCaptured.add(piece);
			} else {
				blackCaptured.add(piece);
			}
		}
	
		float whiteX = boardPositions[0] - tileSize;  
	
		float blackX = boardPositions[7] + tileSize; 
	
		// White captured 
		for (int i = 0; i < whiteCaptured.size() && i < boardPositions.length; i++) {
			float z = boardPositions[i];
			gl.glPushMatrix();
			gl.glTranslated(whiteX, y, z);
			drawPiece(gl, whiteX, y,  z/(tileSize*8), whiteCaptured.get(i));
			gl.glPopMatrix();
		}
	
		// Black captured 
		for (int i = 0; i < blackCaptured.size() && i < boardPositions.length; i++) {
			float z = boardPositions[i]; 
			gl.glPushMatrix();
			gl.glTranslated(blackX, y, z);
			drawPiece(gl, whiteX+ tileSize, y, z/(tileSize*8), blackCaptured.get(i));
			gl.glPopMatrix();
		}
	}
	
	
	
	//draw a foundation to the board
	private void drawBoardBase(GL2 gl, float tileSize) {
        gl.glColor3f(0.25f, 0.13f, 0.05f); // Wood brown color
        gl.glPushMatrix();
        gl.glTranslated(tileSize*4, -0.182, tileSize*4);
        gl.glScalef(1.07f, 0.02f, 1.07f); // Slightly larger than the tile area to create a border
        glut.glutSolidCube(tileSize * 8);
        gl.glPopMatrix();
    }

	//draw the chess board tile pattern
	//the board is flat with y = 0
    private void drawBoardTiles(GL2 gl, float tileSize) {
        float startX = 0.0f, startZ = 0.0f; // top-left of top-left tile is at (0, 0, 0)
		
		// Setup for projection
		GLU glu = new GLU();
		int[] viewport = new int[4];
		double[] modelview = new double[16];
		double[] projection = new double[16];
		gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
		gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, modelview, 0);
		gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, projection, 0);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                boolean isDark = (row + col) % 2 == 0;
                if (isDark) {
                    gl.glColor3f(0.5f, 0.25f, 0.1f); // Brown
                } else {
                    gl.glColor3f(0.9f, 0.8f, 0.6f); // Tan
                }

                float x = startX + col * tileSize;
                float z = startZ + row * tileSize;
                gl.glPushMatrix();
                gl.glTranslated(x, 0, z); // Place tiles above the base
                
				gl.glBegin(GL2.GL_QUADS);
				gl.glVertex3f(0, 0, 0);
				gl.glVertex3f(tileSize, 0, 0);
				gl.glVertex3f(tileSize, 0, tileSize);
				gl.glVertex3f(0, 0, tileSize);
				gl.glEnd();
				
                gl.glPopMatrix();
				
				// Project all four corners
				double[] corner1 = new double[3];
				double[] corner2 = new double[3];
				double[] corner3 = new double[3];
				double[] corner4 = new double[3];

				glu.gluProject(x, 0, z, modelview, 0, projection, 0, viewport, 0, corner1, 0);
				glu.gluProject(x + tileSize, 0, z, modelview, 0, projection, 0, viewport, 0, corner2, 0);
				glu.gluProject(x + tileSize, 0, z + tileSize, modelview, 0, projection, 0, viewport, 0, corner3, 0);
				glu.gluProject(x, 0, z + tileSize, modelview, 0, projection, 0, viewport, 0, corner4, 0);

				// Convert to screen space (flip Y)
				int sx1 = (int) corner1[0];
				int sy1 = (int) (viewport[3] - corner1[1]);
				int sx2 = (int) corner2[0];
				int sy2 = (int) (viewport[3] - corner2[1]);
				int sx3 = (int) corner3[0];
				int sy3 = (int) (viewport[3] - corner3[1]);
				int sx4 = (int) corner4[0];
				int sy4 = (int) (viewport[3] - corner4[1]);

				// Find bounds
				int minX = Math.min(Math.min(sx1, sx2), Math.min(sx3, sx4));
				int maxX = Math.max(Math.max(sx1, sx2), Math.max(sx3, sx4));
				int minY = Math.min(Math.min(sy1, sy2), Math.min(sy3, sy4));
				int maxY = Math.max(Math.max(sy1, sy2), Math.max(sy3, sy4));

				Rectangle tileRect = new Rectangle(minX, minY, maxX - minX, maxY - minY);
				model.setTile(row, col, tileRect);
            }
        }
    }

	private void drawHighlights(GL2 gl, float tileSize) {
		// save & disable lighting/depth/write so quads truly overlay
		gl.glPushAttrib(GL2.GL_ENABLE_BIT | GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_COLOR_BUFFER_BIT);
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glDisable(GL2.GL_DEPTH_TEST);
		gl.glDepthMask(false);
	  
		// blending for translucency
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
	  
		// inset amount (20% margin on each side)
		float inset = tileSize * 0.015f;
		float size  = tileSize - 2 * inset;
  
		// draw one inset quad per legal move square
		for (Point sq : model.getLegalMoves()) {
		  int row = sq.x, col = sq.y;
		  // pick green for quiet moves, red for captures
			Model.Piece target = model.getPiece(row, col);
			if (target != null) {
				// attack-move highlight: pale red
				gl.glColor4f(1.0f, 0.6f, 0.6f, 0.7f);
			}
			else {
				// regular move highlight: pale green
				gl.glColor4f(0.6f, 1.0f, 0.6f, 0.7f);
			}
		  // center based coordinates, subtract half a tile, then add inset
		  float x0 = boardPositions[col] - tileSize/2 + inset;
		  float z0 = boardPositions[row] - tileSize/2 + inset;
	  
		  gl.glBegin(GL2.GL_QUADS);
			gl.glVertex3f(x0,         0.01f, z0);
			gl.glVertex3f(x0 + size,  0.01f, z0);
			gl.glVertex3f(x0 + size,  0.01f, z0 + size);
			gl.glVertex3f(x0,         0.01f, z0 + size);
		  gl.glEnd();
		}
	  
		// restore everything
		gl.glPopAttrib();
	  }

	private void drawPawn(GL2 gl, float x, float y, float z, Model.Piece piece) {

		gl.glPushMatrix();
    	gl.glTranslated(x, y, z);  // Move the pawn's base to (x, y, z)

		setPieceColor(gl, piece);//set the color and own light source

        // **1. Draw Head (Sphere)**
        gl.glPushMatrix();
        gl.glTranslated(0, 2.2, 0); // Move up for the head
        glu.gluSphere(quadric, 0.45, 24, 24);
        gl.glPopMatrix();

        // **2. Draw Neck (Small Cylinder)**
        gl.glPushMatrix();
        gl.glTranslated(0, 1.7, 0);
        gl.glRotated(-90, 1, 0, 0); //Surrounds y-axis
		glu.gluCylinder(quadric, 0.5, 0.5, 0.1, 16, 1);
		glu.gluDisk(quadric, 0.0, 0.5, 16, 1); //bottom cap
		gl.glTranslated(0, 0, 0.1); //post rotation, z is now up
		glu.gluDisk(quadric, 0.0, 0.5, 16, 1); //top cap
        gl.glPopMatrix();

        // **3. Draw Body (Tapered Cylinder)**
        gl.glPushMatrix();
        gl.glTranslated(0, 0.4, 0);
		gl.glRotated(-90, 1, 0, 0); //Surrounds y-axis
        glu.gluCylinder(quadric, 0.5, 0.2, 1.3, 16, 16);
        gl.glPopMatrix();

        // **4. Draw Base (Short/fat Cylinder)**
        gl.glPushMatrix();
		gl.glRotated(-90, 1, 0, 0); //Surrounds y-axis
		glu.gluCylinder(quadric, 0.8, 0.7, 0.4, 24, 8);
		glu.gluDisk(quadric, 0.0, 0.8, 24, 1); //bottom cap
		gl.glTranslated(0, 0, 0.4); //post rotation, z is now up
		glu.gluDisk(quadric, 0.0, 0.7, 24, 1); //top cap
        gl.glPopMatrix();

		gl.glPopMatrix();
		gl.glDisable(GL2.GL_LIGHTING); //lighting only affects pieces for now.
		return;
	}

	private void drawKnight(GL2 gl, float x, float y, float z, Model.Piece piece) {

		gl.glPushMatrix();
		gl.glTranslated(x, y, z);  // Move base to (x, y, z)
	
		setPieceColor(gl, piece); // Set color and lighting
	
		Drawing.drawKnight(gl);

		gl.glPopMatrix();
		gl.glDisable(GL2.GL_LIGHTING);
	}

	private void drawRook(GL2 gl, float x, float y, float z, Model.Piece piece) {
		gl.glPushMatrix();
		gl.glTranslated(x, y, z);
	
		setPieceColor(gl, piece);
		
		Drawing.drawRook(gl, glut);

		gl.glPopMatrix();
		gl.glDisable(GL2.GL_LIGHTING);
	}

	private void drawQueen(GL2 gl, float x, float y, float z, Model.Piece piece) {
		gl.glPushMatrix();
		gl.glTranslated(x, y, z);  // Move the queen's base to (x, y, z)
	
		setPieceColor(gl, piece); // Set color and lighting
	
		Drawing.drawQueen(gl);
	
		gl.glPopMatrix();
		gl.glDisable(GL2.GL_LIGHTING);
	}

	private void drawKing(GL2 gl, float x, float y, float z, Model.Piece piece) {
		gl.glPushMatrix();
		gl.glTranslated(x, y, z);  // Move the king's base to (x, y, z)
	
		setPieceColor(gl, piece); // Set color and lighting
	
		Drawing.drawKing(gl);
	
		gl.glPopMatrix();
		gl.glDisable(GL2.GL_LIGHTING);
	}

	// Render a simple cube for testing
	private void drawCube(GL2 gl) {
		gl.glBegin(GL2.GL_QUADS);
		gl.glColor3f(1, 0, 0); gl.glVertex3f(-1, -1, -1);
		gl.glColor3f(0, 1, 0); gl.glVertex3f(1, -1, -1);
		gl.glColor3f(0, 0, 1); gl.glVertex3f(1, 1, -1);
		gl.glColor3f(1, 1, 0); gl.glVertex3f(-1, 1, -1);
		gl.glEnd();
		return;
	}
	
	//**********************************************************************
	// Private Methods (Utility Functions)
	//**********************************************************************

	//includes a light source for the piece, so it appears as 3D.
	//USED BY PIECE CREATION METHODS
	private void setPieceColor(GL2 gl,  Model.Piece piece) {

		// Enable lighting
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);  // Enable the 0th light source
		gl.glEnable(GL2.GL_NORMALIZE);  // Normalize normals for proper shading
		
		// Set light properties
		float[] lightPosition0 = null; // Light position relative to piece depending on the camera location
		switch(model.getCamPosition())
		{
			case 0:
				lightPosition0 = new float[]{0.0f, 1.0f, 2.0f, 0.0f};
			break;
			case 1:
				lightPosition0 = new float[]{2.0f, 1.0f, 0.0f, 0.0f};
			break;
			case 2:
				lightPosition0 = new float[]{0.0f, 1.0f, -2.0f, 0.0f};
			break;
			case 3:
				lightPosition0 = new float[]{-2.0f, 1.0f, 0.0f, 0.0f};
			break;
		}
		float[] lightAmbient  = {0.4f, 0.4f, 0.4f, 1.0f}; // Ambient light
		float[] lightDiffuse = {0.9f, 0.9f, 0.9f, 1.0f}; // Pure white diffuse light
		float[] lightSpecular = {1.5f, 1.5f, 1.5f, 1.0f}; // Specular highlight

		//light0
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPosition0, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmbient, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDiffuse, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightSpecular, 0);

		// Enable material shading  
		float alpha = 1.0f;
		if (piece.getIsGhost())
			alpha = 0.5f; //Ghost transparency

		float[] materialDiffuse = {1.0f, 1.0f, 1.0f, alpha};  // Pawn color (white)
		if (!piece.isWhite){
			materialDiffuse = new float[]{0.0f, 0.0f, 0.0f, alpha}; //Pawn color (black)
		}

		if (piece.isSelected){
			materialDiffuse = new float[]{1.0f, 0.0f, 0.0f, 1.0f}; // red for selected
		}

		float[] materialSpecular = {0.3f, 0.3f, 0.3f, 1.0f}; // how shiny reflection is
		float[] materialShininess = {10.0f}; // Shininess level
		
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, materialDiffuse, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, materialSpecular, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SHININESS, materialShininess, 0);

	}

	private void	setColor(GL2 gl, int r, int g, int b, int a)
	{
		gl.glColor4f(r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f);
	}

	private void	setColor(GL2 gl, int r, int g, int b)
	{
		setColor(gl, r, g, b, 255);
	}

	private void	fillRect(GL2 gl, int x, int y, int w, int h)
	{
		gl.glBegin(GL2.GL_POLYGON);
		gl.glVertex2i(x+0, y+0);
		gl.glVertex2i(x+0, y+h);
		gl.glVertex2i(x+w, y+h);
		gl.glVertex2i(x+w, y+0);
		gl.glEnd();
	}

	private void	drawRect(GL2 gl, int x, int y, int w, int h)
	{
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex2i(x+0, y+0);
		gl.glVertex2i(x+0, y+h);
		gl.glVertex2i(x+w, y+h);
		gl.glVertex2i(x+w, y+0);
		gl.glEnd();
	}

	private void	fillOval(GL2 gl, int cx, int cy, int w, int h)
	{
		gl.glBegin(GL2.GL_POLYGON);

		for (int i=0; i<32; i++)
		{
			double	a = (2.0 * Math.PI) * (i / 32.0);

			gl.glVertex2d(cx + w * Math.cos(a), cy + h * Math.sin(a));
		}

		gl.glEnd();
	}

	private void	drawOval(GL2 gl, int cx, int cy, int w, int h)
	{
		gl.glBegin(GL.GL_LINE_LOOP);

		for (int i=0; i<32; i++)
		{
			double	a = (2.0 * Math.PI) * (i / 32.0);

			gl.glVertex2d(cx + w * Math.cos(a), cy + h * Math.sin(a));
		}

		gl.glEnd();
	}

	private void	fillPoly(GL2 gl, int startx, int starty, Point[] offsets)
	{
		gl.glBegin(GL2.GL_POLYGON);

		for (int i=0; i<offsets.length; i++)
			gl.glVertex2i(startx + offsets[i].x, starty + offsets[i].y);

		gl.glEnd();
	}

	private void	drawPoly(GL2 gl, int startx, int starty, Point[] offsets)
	{
		gl.glBegin(GL2.GL_LINE_LOOP);

		for (int i=0; i<offsets.length; i++)
			gl.glVertex2i(startx + offsets[i].x, starty + offsets[i].y);

		gl.glEnd();
	}
	
	
	private void drawDisk(GL2 gl, double height, double topRadius, double bottomRadius, int slices, int stacks) {
		gl.glPushMatrix();
	
		gl.glRotated(-90, height, 0, 0); // orient cylinder upright
		glu.gluCylinder(quadric, bottomRadius, topRadius, height, slices, stacks);
		glu.gluDisk(quadric, 0.0, bottomRadius, slices, 1); // bottom cap
	
		gl.glTranslated(0, 0, height); // move to top
		glu.gluDisk(quadric, 0.0, topRadius, slices, 1); // top cap
	
		gl.glPopMatrix();
	}

	//**********************************************************************
	// Piece part functions
	//**********************************************************************

	private void drawBishop(GL2 gl, float x, float y, float z, Model.Piece piece) {
		gl.glPushMatrix();
		gl.glTranslated(x, y, z);
		setPieceColor(gl, piece);
		
		Drawing.drawBishop(gl);

		gl.glPopMatrix();
		gl.glDisable(GL2.GL_LIGHTING);
	}

	private void drawGhost(GL2 gl) {
		Point hover = model.getHoverSquare();
		Point sel   = model.getGhostStartSquare();
		if (hover == null || sel == null) return;
	  
		// progress from start→end [0..1]
		float t = model.getHoverProgress();
	  
		// rows=x,cols=y
		int sr = sel.x, sc = sel.y;
		int er = hover.x, ec = hover.y;
	  
		// world coords of tile centers
		float sx = boardPositions[sc], sz = boardPositions[sr];
		float ex = boardPositions[ec], ez = boardPositions[er];
	  
		// interpolate
		float ix = sx + (ex - sx) * t;
		float iz = sz + (ez - sz) * t;
	  
		// overlay state
		gl.glPushAttrib(GL2.GL_ENABLE_BIT | GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_COLOR_BUFFER_BIT);
		gl.glDisable(GL2.GL_DEPTH_TEST);
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl.glDisable(GL2.GL_LIGHTING);
	  
		// pick ghost color based on side
		Piece ghost = model.getSelectedPiece();
		ghost.setIsGhost(true);
		boolean wasSelected = ghost.getIsSelected();
		ghost.setIsSelected(false);
	  
		// draw at (ix, iz)
		gl.glPushMatrix();
		gl.glTranslated(ix, 0.01f, iz);

		switch (ghost.type) {
			case PAWN:
			  // reuse your drawPawn but disable lighting/materials:
			  drawPawn(gl, 0, 0, 0, ghost);
			  break;
			case KNIGHT:
			  drawKnight(gl, 0, 0, 0, ghost);
			  break;
			case BISHOP:
			  drawBishop(gl, 0, 0, 0, ghost);
			  break;
			case ROOK:
			  drawRook(gl, 0, 0, 0, ghost);
			  break;
			case QUEEN:
			  drawQueen(gl, 0, 0, 0, ghost);
			  break;
			case KING:
			  drawKing(gl, 0, 0, 0, ghost);
			  break;
		}
		ghost.setIsSelected(wasSelected);
		ghost.setIsGhost(false);
		gl.glPopMatrix();
		
		gl.glPopAttrib();
	}	  
}

//******************************************************************************
