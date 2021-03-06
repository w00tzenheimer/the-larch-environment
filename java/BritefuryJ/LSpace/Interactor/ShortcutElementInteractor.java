//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Interactor;

import java.awt.event.KeyEvent;
import java.util.HashMap;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Shortcut.Shortcut;
import BritefuryJ.Shortcut.ShortcutElementAction;

public class ShortcutElementInteractor implements KeyElementInteractor
{
	private HashMap<Shortcut, ShortcutElementAction> shortcuts = new HashMap<Shortcut, ShortcutElementAction>();
	
	
	public ShortcutElementInteractor()
	{
	}
	
	
	public void addShortcut(Shortcut shortcut, ShortcutElementAction action)
	{
		shortcuts.put( shortcut, action );
	}
	
	public void removeShortcut(Shortcut shortcut)
	{
		shortcuts.remove( shortcut );
	}
	
	public boolean isEmpty()
	{
		return shortcuts.isEmpty();
	}
	

	@Override
	public boolean keyPressed(LSElement element, KeyEvent event)
	{
		ShortcutElementAction action = shortcuts.get( Shortcut.fromPressedEvent( event ) );
		if ( action != null )
		{
			try
			{
				action.invoke( element );
			}
			catch (Throwable e)
			{
				element.notifyExceptionDuringElementInteractor( action, "invoke", e );
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean keyReleased(LSElement element, KeyEvent event)
	{
		ShortcutElementAction action = shortcuts.get( Shortcut.fromPressedEvent( event ) );
		return action != null;
	}

	@Override
	public boolean keyTyped(LSElement element, KeyEvent event)
	{
		return false;
	}
}
