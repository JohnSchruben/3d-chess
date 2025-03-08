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
	private final View			view;

	// Model variables
	private boolean				starFill;		// Whether to fill it or not
	private int					starSides;		// Number of sides
	private Point2D.Double		starLocation;	// Location in scene coords

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public Model(View view)
	{
		this.view = view;

		// Initialize user-adjustable variables (with reasonable default values)
		starFill = false;
		starSides = 3;
		starLocation = new Point2D.Double(640.0, 360.0);	// Center of scene
	}

	//**********************************************************************
	// Public Methods (Access Variables)
	//**********************************************************************

	public boolean	getStarFill()
	{
		return starFill;
	}

	public int	getStarSides()
	{
		return starSides;
	}

	public Point2D.Double	getStarLocation()
	{
		return new Point2D.Double(starLocation.x, starLocation.y);
	}

	//**********************************************************************
	// Public Methods (Modify Variables)
	//**********************************************************************

	public void		toggleStarFill()
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				starFill = !starFill;
			}
		});;
	}

	public void		setStarSides(int n)
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				starSides = Math.max(3, Math.min(32, n));	// Limit to [3, 32]
			}
		});;
	}

	public void		setStarLocationInViewCoordinates(Point q)
	{
		view.getCanvas().invoke(false, new ViewPointUpdater(q) {
			public void	update(double[] p) {
				starLocation = new Point2D.Double(p[0], p[1]);
			}
		});;
	}

	public void		setStarLocationInSceneCoordinates(Point2D.Double q)
	{
		view.getCanvas().invoke(false, new BasicUpdater() {
			public void	update(GL2 gl) {
				starLocation = new Point2D.Double(q.x, q.y);
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
}

//******************************************************************************
