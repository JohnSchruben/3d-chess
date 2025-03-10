//******************************************************************************
// Copyright (C) 2016-2021 University of Oklahoma Board of Trustees.
//******************************************************************************
// Last modified: Fri Mar  5 19:03:54 2021 by Chris Weaver
//******************************************************************************
// Major Modification History:
//
// 20160209 [weaver]:	Original file.
// 20190129 [weaver]:	Updated to JOGL 2.3.2 and cleaned up.
// 20190203 [weaver]:	Additional cleanup and more extensive comments.
// 20200121 [weaver]:	Modified to set up OpenGL and UI on the Swing thread.
// 20201215 [weaver]:	Added setIdentifyPixelScale() to canvas setup.
// 20210209 [weaver]:	Added point smoothing for Hi-DPI displays.
//
//******************************************************************************
// Notes:
//
// Warning! This code uses deprecated features of OpenGL, including immediate
// mode vertex attribute specification, for sake of easier classroom learning.
// See www.khronos.org/opengl/wiki/Legacy_OpenGL
//
//******************************************************************************

package edu.ou.cs.cg.assignment.homework01;

//import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;
import javax.swing.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;

//******************************************************************************

/**
 * The <CODE>Application</CODE> class.<P>
 *
 * @author  Chris Weaver
 * @version %I%, %G%
 */
public final class Application
	implements GLEventListener, Runnable
{
	//**********************************************************************
	// Private Class Members
	//**********************************************************************

	private static final String			DEFAULT_NAME = "Homework01";
	private static final Dimension		DEFAULT_SIZE = new Dimension(750, 750);
	
	// number of points to draw
	private int m = 1;

	// the mode of drawing. 4 total.
	private int mode = 0;

	//**********************************************************************
	// Public Class Members
	//**********************************************************************

	public static final GLUT			MYGLUT = new GLUT();
	public static final Random			RANDOM = new Random();

	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) variables
	private int				w;				// Canvas width
	private int				h;				// Canvas height
	private int				k = 0;			// Animation counter
	
	private TextRenderer		renderer;

	//**********************************************************************
	// Main
	//**********************************************************************

	public static void	main(String[] args)
	{
		SwingUtilities.invokeLater(new Application(args));
	}

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public Application(String[] args)
	{
	}

	//**********************************************************************
	// Override Methods (Runnable)
	//**********************************************************************

	public void	run()
	{
		GLProfile		profile = GLProfile.getDefault();

		System.out.println("Running on Java version " + 
			System.getProperty("java.version"));
		System.out.println("Running with OpenGL version " +
			profile.getName());

		GLCapabilities	capabilities = new GLCapabilities(profile);
		GLCanvas		canvas = new GLCanvas(capabilities);	// Single-buffer
		//GLJPanel		canvas = new GLJPanel(capabilities);	// Double-buffer
		JFrame			frame = new JFrame(DEFAULT_NAME);

		// Rectify display scaling issues when in Hi-DPI mode on macOS.
		edu.ou.cs.cg.utilities.Utilities.setIdentityPixelScale(canvas);

		// Specify the starting width and height of the canvas itself
		canvas.setPreferredSize(DEFAULT_SIZE);

		// Populate and show the frame
		frame.setBounds(50, 50, 200, 200);
		frame.getContentPane().add(canvas);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Exit when the user clicks the frame's close button
		frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});

		// Register this class to update whenever OpenGL needs it
		canvas.addGLEventListener(this);

		// Have OpenGL call display() to update the canvas 60 times per second
		FPSAnimator	animator = new FPSAnimator(canvas, 60);

		animator.start();
	}

	//**********************************************************************
	// Override Methods (GLEventListener)
	//**********************************************************************

	// Called immediately after the GLContext of the GLCanvas is initialized.
	public void	init(GLAutoDrawable drawable)
	{
		w = drawable.getSurfaceWidth();
		h = drawable.getSurfaceHeight();

		renderer = new TextRenderer(new Font("Serif", Font.PLAIN, 18),
									true, true);

		GL2	gl = drawable.getGL().getGL2();

		// Make points easier to see on Hi-DPI displays
		gl.glEnable(GL2.GL_POINT_SMOOTH);	// Turn on point anti-aliasing
	}

	// Notification to release resources for the GLContext.
	public void	dispose(GLAutoDrawable drawable)
	{
		renderer = null;
	}

	// Called to initiate rendering of each frame into the GLCanvas.
	public void	display(GLAutoDrawable drawable)
	{
		update(drawable);
		render(drawable);
	}

	// Called during the first repaint after a resize of the GLCanvas.
	public void	reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
	{
		this.w = w;
		this.h = h;
	}

	//**********************************************************************
	// Private Methods (Rendering)
	//**********************************************************************
	// Update the scene model for the current animation frame.
	private void	update(GLAutoDrawable drawable)
	{
		k++;									// Advance animation counter
		
		if (m > 100000)	
		{
			// updating mode
			mode = (mode + 1) % 4;

			// reseting m;
			m = 1;	
		}						
		else
		{
			m++;								
		}

		// increasing at a faster rate
		m = (int)Math.floor(m * 1.05) + 1;
	}

	float angle = 0.0f;
	// Render the scene model and display the current animation frame.
	private void	render(GLAutoDrawable drawable)
	{
		GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        gl.glTranslatef(0.0f, 0.0f, -5.0f);
        gl.glRotatef(angle, 1.0f, 1.0f, 1.0f);

        // Draw a Cube
        gl.glBegin(GL2.GL_QUADS);
        renderer.setColor(1.0f, 1.0f, 0.0f, 1.0f);
        // Front Face
        gl.glColor3f(1, 0, 0);
        gl.glVertex3f(-1, -1, 1);
        gl.glVertex3f(1, -1, 1);
        gl.glVertex3f(1, 1, 1);
        gl.glVertex3f(-1, 1, 1);

        // Back Face
        gl.glColor3f(0, 1, 0);
        gl.glVertex3f(-1, -1, -1);
        gl.glVertex3f(-1, 1, -1);
        gl.glVertex3f(1, 1, -1);
        gl.glVertex3f(1, -1, -1);

        // Top Face
        gl.glColor3f(0, 0, 1);
        gl.glVertex3f(-1, 1, -1);
        gl.glVertex3f(-1, 1, 1);
        gl.glVertex3f(1, 1, 1);
        gl.glVertex3f(1, 1, -1);

        // Bottom Face
        gl.glColor3f(1, 1, 0);
        gl.glVertex3f(-1, -1, -1);
        gl.glVertex3f(1, -1, -1);
        gl.glVertex3f(1, -1, 1);
        gl.glVertex3f(-1, -1, 1);

        // Right face
        gl.glColor3f(1, 0, 1);
        gl.glVertex3f(1, -1, -1);
        gl.glVertex3f(1, 1, -1);
        gl.glVertex3f(1, 1, 1);
        gl.glVertex3f(1, -1, 1);

        // Left Face
        gl.glColor3f(0, 1, 1);
        gl.glVertex3f(-1, -1, -1);
        gl.glVertex3f(-1, -1, 1);
        gl.glVertex3f(-1, 1, 1);
        gl.glVertex3f(-1, 1, -1);

        gl.glEnd();

        angle += 0.5f;

		gl.glFlush();							// Finish and display
	}

	//**********************************************************************
	// Private Methods (Pipeline)
	//**********************************************************************

	// Position and orient the default camera to view in 2-D, centered above.
	private void	setProjection(GL2 gl)
	{
		GLU	glu = GLU.createGLU();

		gl.glMatrixMode(GL2.GL_PROJECTION);		// Prepare for matrix xform
		gl.glLoadIdentity();						// Set to identity matrix
		glu.gluOrtho2D(-1.0f, 1.0f, -1.0f, 1.0f);	// 2D translate and scale
	}

	//**********************************************************************
	// Private Methods (Scene)
	//**********************************************************************

	// This page is helpful (scroll down to "Drawing Lines and Polygons"):
	// www.linuxfocus.org/English/January1998/article17.html
	private void	drawSomething(GL2 gl)
	{
		// swapping between drawing styles.
		gl.glBegin(mode == 0 || mode == 3 ? GL.GL_POINTS : GL.GL_LINE_STRIP);	

		gl.glColor3f(1.0f, 1.0f, 1.0f);	// Draw in white

		// tinkerbell map constants, using defaults because they look the best.
		double a = 0.9;
		double b = -0.6013;
		double c = 2.0;
		double d = 0.50;
		
		// initial x and y 
		double x = -0.72;
		double y = -0.64;

		// scale to make the scene fit in the window. 
		double scale = 0.9;

		// offset to center the scene in the window
		double offset = 0.5;

		// writing the map each frame
		for (int i = 0; i < m; i++) {

			// tinkerbell map math
			double nextX = Math.pow(x, 2) - Math.pow(y, 2) + (a * x) + (b * y);
			double nextY = (2 * x * y) + (c * x) + (d * y);
			
			// changing the pattern based on mode
			double sx = mode <= 1 ? x : y;
			double sy = mode <= 1 ? y : nextY;

			// scaling and offsetting
			sx = (sx + offset) * scale;
			sy = (sy + offset) * scale;

			// drawing
			gl.glVertex2d(sx, sy);

			// updating x and y
			x = nextX;
			y = nextY;
		}
	
		gl.glEnd();						// Stop specifying points
	}

	// Warning! Text is drawn in unprojected canvas/viewport coordinates.
	// For more on text rendering, the example on this page is long but helpful:
	// jogamp.org/jogl-demos/src/demos/j2d/FlyingText.java
	private void	drawText(GLAutoDrawable drawable)
	{
		renderer.beginRendering(w, h);
		renderer.setColor(1.0f, 1.0f, 0.0f, 1.0f);
		renderer.draw("Mode: " + (mode + 1) +" (Frame: " + k + ")", w/2 + 8, 5);
		renderer.endRendering();
	}
}

//******************************************************************************
