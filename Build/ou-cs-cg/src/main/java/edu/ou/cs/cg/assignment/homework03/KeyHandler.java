package edu.ou.cs.cg.assignment.homework03;

//import java.lang.*;
import java.awt.Component;
import java.awt.event.*;
import java.awt.geom.Point2D;
import edu.ou.cs.cg.utilities.Utilities;

//******************************************************************************

/**
 * The <CODE>KeyHandler</CODE> class.<P>
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
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_F:							// The 'f' key
				return;

			case KeyEvent.VK_Q:							// The 'q' key
				return;

			case KeyEvent.VK_A:							// The 'a' key
				model.setCamera('l'); 				//move cam left
				return;

			case KeyEvent.VK_W:							// The 'w' key
				model.setCamera('f'); 				//move cam forward
				return;

			case KeyEvent.VK_D:							// The 'd' key
				model.setCamera('r'); 				//move cam right
				return;
			
			case KeyEvent.VK_S:							// The 's' key
				model.setCamera('h'); 				//toggle high cam
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
