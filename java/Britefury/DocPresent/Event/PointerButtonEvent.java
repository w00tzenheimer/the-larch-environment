package Britefury.DocPresent.Event;

import Britefury.DocPresent.Input.PointerInterface;
import Britefury.Math.Xform2;



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
	
	
	
	public PointerButtonEvent transformed(Xform2 xToLocal)
	{
		return new PointerButtonEvent( pointer.transformed( xToLocal ), button, action );
	}
}