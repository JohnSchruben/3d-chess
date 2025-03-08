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
		this.w = w;
		this.h = h;
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

		gl.glClear(GL.GL_COLOR_BUFFER_BIT);		// Clear the buffer

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

		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);	// Black background

		// See the com.jogamp.opengl.GL API for more on the following settings

		// Smooth points and lines for easier viewing on Hi-DPI displays
		gl.glEnable(GL2.GL_POINT_SMOOTH);	// Turn on point anti-aliasing
		gl.glEnable(GL2.GL_LINE_SMOOTH);	// Turn on line anti-aliasing

		// Enabling alpha blending to allow use of translucent colors in 2-D
		// (Warning: Translucency in 3-D is more difficult and expensive!)
		gl.glEnable(GL.GL_BLEND);			// Turn on color channel blending
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
	}

	// Position and orient the default camera to view rendered elements in 2-D.
	// Translate and scale the projection for drawing the contents of the scene.
	private void	setScreenProjection(GL2 gl)
	{
		GLU	glu = GLU.createGLU();

		gl.glMatrixMode(GL2.GL_PROJECTION);			// Prepare for matrix xform
		gl.glLoadIdentity();						// Set to identity matrix
		glu.gluOrtho2D(0.0f, 1280.0f, 0.0f, 720.0f);// 2D translate and scale
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

		// Draw help+state text for parameter: star fill
		boolean		starFill = model.getStarFill();
		String		sf = ("[f]   Star Fill         = " + starFill);

		renderer.draw(sf, 2, h - voff);		// Draw text on the current line
		voff += Utilities.TEXT_SPACING;		// Move down one line

		// Draw help+state text for parameter: star sides
		int			starSides = model.getStarSides();
		String		sn = ("[q|w] Star Sides [3-32] = " + starSides);

		renderer.draw(sn, 2, h - voff);		// Draw text on the current line
		voff += Utilities.TEXT_SPACING;		// Move down one line

		// Draw help+state text for parameter: star location
		Point2D.Double	starLocation = model.getStarLocation();
		String		sp = ("[←|→|↓|↑] Star Location = " + "[" +
							FORMAT.format(starLocation.x) + "," +
							FORMAT.format(starLocation.y) + "]");

		renderer.draw(sp, 2, h - voff);		// Draw text on the current line
		voff += Utilities.TEXT_SPACING;		// Move down one line

		String		ks = "  (<shift> for coarse-grained movement)";
		
		renderer.draw(ks, 2, h - voff);		// Draw text on the current line
		voff += Utilities.TEXT_SPACING;		// Move down one line

		// Insert empty line
		voff += Utilities.TEXT_SPACING;		// Move down one line

		// Draw help text for mouse click interactions
		String	mc = "mouse click sets star location";

		renderer.draw(mc, 2, h - voff);		// Draw text on the current line
		voff += Utilities.TEXT_SPACING;		// Move down one line

		// Draw help text for mouse wheel interactions
		String	mw = "mouse wheel increases/decreases star sides";

		renderer.draw(mw, 2, h - voff);		// Draw text on the current line
		voff += Utilities.TEXT_SPACING;		// Move down one line

		renderer.endRendering();
	}

	//**********************************************************************
	// Private Methods (Scene Contents)
	//**********************************************************************

	private void	drawMain(GL2 gl)
	{
		setScreenProjection(gl);
		drawStar(gl);
	}

	private void	drawStar(GL2 gl)
	{
		boolean			f = model.getStarFill();
		int				s = model.getStarSides();
		Point2D.Double	p = model.getStarLocation();

		double			theta = 0.5 * Math.PI;		// Start at 12 o'clock
		double			or = 100.0;					// Outer radius
		double			ir = 40.0;					// Inner radius

		// Animate rotation of the star clockwise at 0.25 cycles per second
		theta -= 2.0 * Math.PI * (k / 240.0);		

		if (f)
		{
			// Fill the star using a TRIANGLE_FAN
			setColor(gl, 255, 255, 0);				// Yellow
			gl.glBegin(GL.GL_TRIANGLE_FAN);
			gl.glVertex2d(p.x, p.y);
			doStarVertices(gl, p.x, p.y, s, or, ir, theta);
			gl.glVertex2d(p.x + or * Math.cos(theta),
						  p.y + or * Math.sin(theta));
			gl.glEnd();
		}

		// Edge the star using a LINE_STRIP
		setColor(gl, 255, 0, 0);						// Red
		gl.glBegin(GL.GL_LINE_STRIP);
		doStarVertices(gl, p.x, p.y, s, or, ir, theta);
		gl.glVertex2d(p.x + or * Math.cos(theta),
					  p.y + or * Math.sin(theta));
		gl.glEnd();
	}

	private void	doStarVertices(GL2 gl, double cx, double cy, int sides,
								   double r1, double r2, double theta)
	{
		double	delta = Math.PI / sides;

		for (int i=0; i<sides; i++)
		{
			gl.glVertex2d(cx + r1 * Math.cos(theta), cy + r1 * Math.sin(theta));
			theta += delta;

			gl.glVertex2d(cx + r2 * Math.cos(theta), cy + r2 * Math.sin(theta));
			theta += delta;
		}
	}

	//**********************************************************************
	// Private Methods (Utility Functions)
	//**********************************************************************

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
