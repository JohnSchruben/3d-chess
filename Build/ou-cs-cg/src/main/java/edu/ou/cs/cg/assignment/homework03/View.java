//******************************************************************************
// Copyright (C) 2016-2025 University of Oklahoma Board of Trustees.
//******************************************************************************
// Last modified: Sat Feb 22 09:58:10 2025 by Chris Weaver
//******************************************************************************
// Major Modification History:
//
// 20160209 [weaver]:	Original file.
// 20190203 [weaver]:	Updated to JOGL 2.3.2 and cleaned up.
// 20190227 [weaver]:	Updated to use model and asynchronous event handling.
// 20220225 [weaver]:	Added point smoothing for Hi-DPI displays.
// 20250222 [weaver]:	Updated homework03 for easier carryover from homework02.
//
//******************************************************************************
// Notes:
//
//******************************************************************************

package edu.ou.cs.cg.assignment.homework03;

//import java.lang.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.*;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;
import edu.ou.cs.cg.utilities.Utilities;

//******************************************************************************

/**
 * The <CODE>View</CODE> class.<P>
 *
 * @author  Chris Weaver
 * @version %I%, %G%
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
	private GLU	glu = new GLU();
	private GLUT glut = new GLUT();
	private int							w;			// Canvas width
	private int							h;			// Canvas height

	private TextRenderer				renderer;

	private final FPSAnimator			animator;
	private int							k;			// Frame counter

	private final Model					model;

	private final KeyHandler			keyHandler;
	private final MouseHandler			mouseHandler;

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

		// Initialize controller (interaction handlers)
		keyHandler = new KeyHandler(this, model);
		mouseHandler = new MouseHandler(this, model);

		// Initialize animation
		animator = new FPSAnimator(canvas, DEFAULT_FRAMES_PER_SECOND);
		animator.start();
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
	}

	private void	render(GLAutoDrawable drawable)
	{
		GL2	gl = drawable.getGL().getGL2();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);	// Clear the buffer

		drawMain(gl);							// Draw scene content
		//drawMode(drawable);						// Draw overlaid mode text

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
	}

	// Set the camera in the 3D space
	private void	setCamera(GL2 gl)
	{
		gl.glLoadIdentity();						// Set to identity matrix

		// Set up the camera (eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ)
		// X(left/right), Y(up/down), Z(forward/back)
		glu.gluLookAt(8.8, 4, 29, 8.8, 1, 8.8, 0, 1, 0); //default postion
	}

	//**********************************************************************
	// Private Methods (Mode Text Overlay)
	//**********************************************************************

	private void	drawMode(GLAutoDrawable drawable)
	{
		GL2		gl = drawable.getGL().getGL2();

		renderer.beginRendering(w, h);

		// Draw all text in medium gray
		renderer.setColor(0.75f, 0.75f, 0.75f, 1.0f);

		// Vertical position of text labels
		int 	voff = Utilities.TEXT_SPACING;

		// Draw text
		String		sf = ("");

		renderer.draw(sf, 2, h - voff);		// Draw text on the current line
		voff += Utilities.TEXT_SPACING;		// Move down one line

		renderer.endRendering();
	}

	//**********************************************************************
	// Private Methods (Scene Contents)
	//**********************************************************************

	private void	drawMain(GL2 gl)
	{
		setCamera(gl); //default postion
		// Eventually will move view around board by switching between different camera postitions and angles with the keyboard.
		//drawCube(gl);

		drawChessSet(gl); //Board and pieces
		
	}

	private void drawChessSet(GL2 gl) {
		
		// Draw board in middle of space. Keep still.
		float tileSize = 2.2f; //width and length of each tile
		//createBoardLighting(gl); //NOT READY
		//drawBoardBase(gl); //NOT READY
		drawBoardTiles(gl, tileSize);

		//Use as the x and z cords for each of the postions 0 through 7. 0 by 0 is the top left position.
		float[] boardPositions = {tileSize/2, 3*tileSize/2, 5*tileSize/2, 7*tileSize/2, 9*tileSize/2, 11*tileSize/2, 13*tileSize/2, 15*tileSize/2};
		
		// Draw all the pieces in their starting positions
		//draw white pawn
		drawPawn(gl, boardPositions[1], 0, boardPositions[2], 1);

		//draw black pawn
		drawPawn(gl, boardPositions[5], 0, boardPositions[6], 0);
		

		return;
	}
	
	//draw a foundation to the board
	//NOT FINISHED
	private void drawBoardBase(GL2 gl) {
        gl.glColor3f(0.5f, 0.25f, 0.1f); // Wood brown color
        gl.glPushMatrix();
        gl.glTranslated(4, -0.5, 4);
        gl.glScalef(1.2f, 0.1f, 1.2f); // Slightly larger than the tile area to create a border
        glut.glutSolidCube(8.0f);
        gl.glPopMatrix();
    }

	//draw the chess board tile pattern
	//the board is flat with y = 0
    private void drawBoardTiles(GL2 gl, float tileSize) {
        float startX = 0.0f, startZ = 0.0f; // top-left of top-left tile is at (0, 0, 0)
		
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
            }
        }
    }

	//makes a hoverhoad light for the chess board
	//NOT FINISHED
	private void createBoardLighting(GL2 gl) {
        float[] lightPosition = {8.0f, 16.0f, 8.0f, 1.0f}; // Position of the light
        float[] lightDirection = {0.0f, -1.0f, 0.0f}; // Pointing straight down
        float[] lightDiffuse = {2.0f, 2.0f, 2.0f, 1.0f}; // Stronger white light
        float[] lightSpecular = {2.0f, 2.0f, 2.0f, 1.0f};
        
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPosition, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPOT_DIRECTION, lightDirection, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDiffuse, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightSpecular, 0);
        gl.glLightf(GL2.GL_LIGHT0, GL2.GL_SPOT_CUTOFF, 90.0f); // Wider spotlight angle
        gl.glLightf(GL2.GL_LIGHT0, GL2.GL_SPOT_EXPONENT, 2.0f); // Softer falloff
	}

	//color = 0 -> black
	//color = 1 -> white
	private void drawPawn(GL2 gl, float x, float y, float z, int color) {
		
		GLUquadric quadric = glu.gluNewQuadric();
        glu.gluQuadricNormals(quadric, GLU.GLU_SMOOTH);

		gl.glPushMatrix();
    	gl.glTranslated(x, y, z);  // Move the pawn's base to (x, y, z)

		setPieceColor(gl, quadric, color); //set the color and own light source

        // **1. Draw Head (Sphere)**
        gl.glPushMatrix();
        gl.glTranslated(0, 2.2, 0); // Move up for the head
        glu.gluSphere(quadric, 0.45, 32, 32);
        gl.glPopMatrix();

        // **2. Draw Neck (Small Cylinder)**
        gl.glPushMatrix();
        gl.glTranslated(0, 1.7, 0);
        gl.glRotated(-90, 1, 0, 0); //Surrounds y-axis
		glu.gluCylinder(quadric, 0.5, 0.5, 0.1, 32, 32);
		glu.gluDisk(quadric, 0, 0.5, 32, 32); //bottom cap
		gl.glTranslated(0, 0.1, 0);
		glu.gluDisk(quadric, 0, 0.5, 32, 32); //top cap
        gl.glPopMatrix();

        // **3. Draw Body (Tapered Cylinder)**
        gl.glPushMatrix();
        gl.glTranslated(0, 0.4, 0);
		gl.glRotated(-90, 1, 0, 0); //Surrounds y-axis
        glu.gluCylinder(quadric, 0.5, 0.2, 1.3, 32, 32);
        gl.glPopMatrix();

        // **4. Draw Base (Short/fat Cylinder)**
        gl.glPushMatrix();
        gl.glTranslated(0, 0, 0);
		gl.glRotated(-90, 1, 0, 0); //Surrounds y-axis
		glu.gluCylinder(quadric, 0.8, 0.7, 0.4, 32, 32);
		glu.gluDisk(quadric, 0, 0.8, 32, 32); //bottom cap
		gl.glTranslated(0, 0.4, 0);
		glu.gluDisk(quadric, 0, 0.7, 32, 32); //top cap
        gl.glPopMatrix();

		gl.glPopMatrix();

		gl.glDisable(GL2.GL_LIGHTING); //lighting only affects pieces for now.
		return;
	}

	private void drawBishop(GL2 gl, float x, float y, float z, int color) {
		return;
	}

	private void drawKnight(GL2 gl, float x, float y, float z, int color) {
		return;
	}

	private void drawRook(GL2 gl, float x, float y, float z, int color) {
		return;
	}

	private void drawQueen(GL2 gl, float x, float y, float z, int color) {
		return;
	}

	private void drawKing(GL2 gl, float x, float y, float z, int color) {
		return;
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

	//color = 0 -> black
	//color = 1 -> white
	//includes a light source for the piece, so it appears as 3D.
	//USED BY PIECE CREATION METHODS
	private void setPieceColor(GL2 gl, GLUquadric quadric, int color) {

		// Enable lighting
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);  // Enable the first light source
		gl.glEnable(GL2.GL_NORMALIZE);  // Normalize normals for proper shading

		// Set light properties
		float[] lightPosition = {0.0f, 2.0f, 2.0f, 1.0f}; // Light position relative to piece
		float[] lightAmbient  = {0.2f, 0.2f, 0.2f, 1.0f}; // Ambient light
		float[] lightDiffuse  = {1.0f, 1.0f, 1.0f, 1.0f}; // Diffuse light (soft shading)
		float[] lightSpecular = {1.0f, 1.0f, 1.0f, 1.0f}; // Specular highlight

		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPosition, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmbient, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDiffuse, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightSpecular, 0);

		// Enable material shading  
		float[] materialDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};  // Pawn color (white)
		if (color == 0){
			materialDiffuse = new float[]{0.0f, 0.0f, 0.0f, 1.0f}; //Pawn color (black)
		}
		float[] materialSpecular = {1.0f, 1.0f, 1.0f, 1.0f}; // Shiny reflection
		float[] materialShininess = {50.0f}; // Shininess level

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
}

//******************************************************************************
