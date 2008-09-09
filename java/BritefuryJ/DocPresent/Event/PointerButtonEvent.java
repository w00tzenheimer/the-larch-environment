//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.Event;

import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.Math.Xform2;



public class PointerButtonEvent extends PointerEvent {
	public enum Action
	{
		DOWN,
		DOWN2,
		DOWN3,
		UP
	}
	
	public int button;
	public Action action;
	
	
	public PointerButtonEvent(PointerInterface pointer, int button, Action action)
	{
		super( pointer );
		
		this.button = button;
		this.action = action;
	}
	
	
	public int getButton()
	{
		return button;
	}
	
	public Action getAction()
	{
		return action;
	}
	
	
	
	public PointerButtonEvent transformed(Xform2 xToLocal)
	{
		return new PointerButtonEvent( pointer.transformed( xToLocal ), button, action );
	}
}
