//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Input.Modifier;
import BritefuryJ.DocPresent.StyleSheets.LinkStyleSheet;

public class DPLink extends DPStaticText
{
	public interface LinkListener
	{
		public boolean onLinkClicked(DPLink link, PointerButtonEvent buttonEvent);
	}
	
	
	protected static class LinkTargetListener implements LinkListener
	{
		private String targetLocation;
		
		
		public LinkTargetListener(String targetLocation)
		{
			this.targetLocation = targetLocation;
		}
		
		public boolean onLinkClicked(DPLink link, PointerButtonEvent buttonEvent)
		{
			PageController pageController = link.presentationArea.getPageController();
			if ( ( buttonEvent.getPointer().getModifiers() & Modifier.CTRL ) != 0 )
			{
				if ( buttonEvent.getButton() == 1  ||  buttonEvent.getButton() == 2 )
				{
					pageController.openLocation( targetLocation, PageController.OpenOperation.OPEN_IN_NEW_WINDOW );
					return true;
				}
			}
			else
			{
				if ( buttonEvent.getButton() == 1 )
				{
					pageController.openLocation( targetLocation, PageController.OpenOperation.OPEN_IN_CURRENT_TAB );
					return true;
				}
				else if ( buttonEvent.getButton() == 2 )
				{
					pageController.openLocation( targetLocation, PageController.OpenOperation.OPEN_IN_NEW_TAB );
					return true;
				}
			}

			return false;
		}
	}
	
	
	protected static class PyLinkListener implements LinkListener
	{
		private PyObject callable;
		
		
		public PyLinkListener(PyObject callable)
		{
			this.callable = callable;
		}
		
		public boolean onLinkClicked(DPLink link, PointerButtonEvent buttonEvent)
		{
			return Py.py2boolean( callable.__call__( Py.java2py( link ), Py.java2py( buttonEvent ) ) );
		}
	}
	
	
	
	
	protected LinkListener listener;
	




	public DPLink(ElementContext context, String text, String targetLocation)
	{
		this( context, LinkStyleSheet.defaultStyleSheet, text, new LinkTargetListener( targetLocation ) );
	}
	
	public DPLink(ElementContext context, LinkStyleSheet styleSheet, String text, String targetLocation)
	{
		this( context, styleSheet, text, new LinkTargetListener( targetLocation ) );
	}

	public DPLink(ElementContext context, String text, LinkListener listener)
	{
		this( context, LinkStyleSheet.defaultStyleSheet, text, listener );
	}
	
	public DPLink(ElementContext context, LinkStyleSheet styleSheet, String text, LinkListener listener)
	{
		super( context, styleSheet, text );
		this.listener = listener;
	}

	public DPLink(ElementContext context, String text, PyObject listener)
	{
		this( context, LinkStyleSheet.defaultStyleSheet, text, new PyLinkListener( listener ) );
	}
	
	public DPLink(ElementContext context, LinkStyleSheet styleSheet, String text, PyObject listener)
	{
		this( context, styleSheet, text, new PyLinkListener( listener ) );
	}


	
	
	
	protected void onEnter(PointerMotionEvent event)
	{
		super.onEnter( event );
		if ( isRealised() )
		{
			presentationArea.setCursorHand( event.getPointer() );
		}
	}

	protected void onLeave(PointerMotionEvent event)
	{
		if ( isRealised() )
		{
			presentationArea.setCursorArrow( event.getPointer() );
		}
		super.onLeave( event );
	}



	protected boolean onButtonDown(PointerButtonEvent event)
	{
		super.onButtonDown( event );
		return true;
	}

	protected boolean onButtonUp(PointerButtonEvent event)
	{
		super.onButtonUp( event );
		
		if ( isRealised() )
		{
			return listener.onLinkClicked( this, event );
		}
		
		return false;
	}
}
