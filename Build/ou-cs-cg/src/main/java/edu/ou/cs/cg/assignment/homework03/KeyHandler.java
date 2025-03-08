//******************************************************************************
// Copyright (C) 2016-2025 University of Oklahoma Board of Trustees.
//******************************************************************************
// Last modified: Sat Feb 22 09:58:10 2025 by Chris Weaver
//******************************************************************************
// Major Modification History:
//
// 20160225 [weaver]:	Original file.
// 20190227 [weaver]:	Updated to use model and asynchronous event handling.
// 20250222 [weaver]:	Updated homework03 for easier carryover from homework02.
//
//******************************************************************************
// Notes:
//
//******************************************************************************

package edu.ou.cs.cg.assignment.homework03;

//import java.lang.*;
import java.awt.Component;
import java.awt.event.*;
import java.awt.geom.Point2D;
import edu.ou.cs.cg.utilities.Utilities;

//******************************************************************************

/**
 * The <CODE>KeyHandler</CODE> class.<P>
 *
 * @author  Chris Weaver
 * @version %I%, %G%
 */
public final class KeyHandler extends KeyAdapter
{
	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) variables
	private final View		view;
	private final Model		model;

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public KeyHandler(View view, Model model)
	{
		this.view = view;
		this.model = model;

		Component	component = view.getCanvas();

		component.addKeyListener(this);
	}

	//**********************************************************************
	// Override Methods (KeyListener)
	//**********************************************************************

	public void		keyPressed(KeyEvent e)
	{
		// The shift key determines whether star moving is fine or coarse
		int				amount = (Utilities.isShiftDown(e) ? 50 : 10);

		switch (e.getKeyCode())
		{
			case KeyEvent.VK_F:							// The 'f' key
				return;

			case KeyEvent.VK_Q:							// The 'q' key
				return;

			case KeyEvent.VK_W:							// The 'w' key
				return;

			case KeyEvent.VK_LEFT:						// The left arrow key
				break;

			case KeyEvent.VK_RIGHT:						// The right arrow key
				break;

			case KeyEvent.VK_DOWN:						// The down arrow key
				break;

			case KeyEvent.VK_UP:						// The up arrow key
				break;
		}
	}
}

//******************************************************************************
