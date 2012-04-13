//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.Input;

import java.awt.geom.AffineTransform;
import java.util.Iterator;
import java.util.Stack;

import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.LSpace.Event.PointerNavigationEvent;
import BritefuryJ.LSpace.Event.PointerNavigationPanEvent;
import BritefuryJ.LSpace.Event.PointerNavigationZoomEvent;
import BritefuryJ.LSpace.Event.PointerScrollEvent;
import BritefuryJ.LSpace.Interactor.AbstractElementInteractor;
import BritefuryJ.LSpace.Interactor.NavigationElementInteractor;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public class PointerNavigationInteractor extends AbstractPointerDragInteractor
{
	private int navigationButton = 0;
	private AffineTransform navigationDragElementRootToLocalXform = null;
	private Point2 navigationDragStartPos = new Point2();
	private Point2 navigationDragCurrentPos = new Point2();
	private boolean bNavigationDragInProgress = false;
	
	private PointerInputElement navElement = null;
	private NavigationElementInteractor navInteractor = null;
	
	
	public PointerNavigationInteractor()
	{
	}





	private boolean testNavigationModifiers(PointerInterface pointer)
	{
		int modifiers = pointer.getModifiers();
		int keys = Modifier.getKeyModifiers( modifiers );
		return keys == Modifier.ALT;
	}
	
	
	
	public boolean dragBegin(PointerButtonEvent event)
	{
		PointerInterface pointer = event.getPointer();
		if ( testNavigationModifiers( pointer ) )
		{
			if ( !bNavigationDragInProgress )
			{
				Point2 pos = pointer.getLocalPos();
				navigationButton = event.getButton();
				navigationDragStartPos = pos.copy();
				navigationDragCurrentPos = pos.copy();
				bNavigationDragInProgress = true;
				handleNavigationGestureBegin( pointer, event );
			}
			return true;
		}
		else
		{
			return false;
		}
	}

	public void dragEnd(PointerButtonEvent event, Point2 dragStartPos, int dragButton)
	{
		if ( bNavigationDragInProgress )
		{
			if ( event.getButton() == navigationButton ) 
			{
				handleNavigationGestureEnd( event.getPointer(), event );
				bNavigationDragInProgress = false;
			}
		}
	}


	public void dragMotion(PointerMotionEvent event, Point2 dragStartPos, int dragButton)
	{
		if ( bNavigationDragInProgress )
		{
			Point2 pos = event.getPointer().getLocalPos();
			Vector2 delta = pos.sub( navigationDragCurrentPos );
			navigationDragCurrentPos = pos.copy();
			
			PointerNavigationEvent navEvent = null;
			
			if ( navigationButton == 1  ||  navigationButton == 2 )
			{
				navEvent = new PointerNavigationPanEvent( event.getPointer(), delta );
			}
			else if ( navigationButton == 3 )
			{
				double scaleDeltaPixels = delta.x + delta.y;
				double scaleDelta = Math.pow( 2.0, scaleDeltaPixels / 200.0 );
				
				navEvent = new PointerNavigationZoomEvent( event.getPointer(), navigationDragStartPos, scaleDelta );
			}
		
			if ( navEvent != null )
			{
				handleNavigationGestureDrag( event.getPointer(), navEvent );
			}
		}
	}
	
	
	public boolean scroll(Pointer pointer, PointerScrollEvent event)
	{
		if ( testNavigationModifiers( pointer ) )
		{
			double delta = (double)event.getScrollY();
			double scaleDelta = Math.pow( 2.0,  ( delta / 1.5 ) );
			
			handleNavigationGestureClick( pointer, new PointerNavigationZoomEvent( pointer, pointer.getLocalPos(), scaleDelta ) );
		}
		else
		{
			double delta = (double)event.getScrollY();
			handleNavigationGestureClick( pointer, new PointerNavigationPanEvent( pointer, new Vector2( 0.0, delta * 75.0 ) ) );
		}
		
		return true;
	}
	
	
	
	
	private void handleNavigationGestureBegin(PointerInterface pointer, PointerButtonEvent event)
	{
		Stack<PointerInputElement> elements = pointer.concretePointer().getLastElementPathUnderPoint( pointer.getLocalPos() );
		Stack<PointerButtonEvent> events = Pointer.eventStack( event, elements );
		
		while ( !elements.isEmpty() )
		{
			PointerInputElement element = elements.peek();
			PointerButtonEvent elementSpaceEvent = events.peek();
			
			Iterable<AbstractElementInteractor> interactors = element.getElementInteractors( NavigationElementInteractor.class );
			if ( interactors != null )
			{
				for (AbstractElementInteractor interactor: interactors )
				{
					NavigationElementInteractor navInt = (NavigationElementInteractor)interactor;
					if ( navInt.navigationGestureBegin( element, elementSpaceEvent ) )
					{
						navElement = element;
						navigationDragElementRootToLocalXform = Pointer.rootToLocalTransform( elements );
						navInteractor = navInt;
						return;
					}
				}
			}
			
			elements.pop();
			events.pop();
		}
	}

	private void handleNavigationGestureEnd(PointerInterface pointer, PointerButtonEvent event)
	{
		if ( navInteractor != null )
		{
			navInteractor.navigationGestureEnd( navElement, (PointerButtonEvent)event.transformed( navigationDragElementRootToLocalXform ) );
		}
	}

	private void handleNavigationGestureDrag(PointerInterface pointer, PointerNavigationEvent event)
	{
		if ( navInteractor != null )
		{
			navInteractor.navigationGesture( navElement, (PointerNavigationEvent)event.transformed( navigationDragElementRootToLocalXform ) );
		}
	}

	private static void handleNavigationGestureClick(PointerInterface pointer, PointerNavigationEvent event)
	{
		Stack<PointerInputElement> elements = pointer.concretePointer().getLastElementPathUnderPoint( pointer.getLocalPos() );
		Stack<PointerNavigationEvent> events = Pointer.eventStack( event, elements );
		
		while ( !elements.isEmpty() )
		{
			PointerInputElement element = elements.pop();
			PointerNavigationEvent elementSpaceEvent = events.pop();
			
			Iterable<AbstractElementInteractor> interactors = element.getElementInteractors( NavigationElementInteractor.class );
			if ( interactors != null )
			{
				Iterator<AbstractElementInteractor> iter = interactors.iterator();
				if ( iter.hasNext() )
				{
					NavigationElementInteractor navInt = (NavigationElementInteractor)iter.next();
					navInt.navigationGesture( element, elementSpaceEvent );
					return;
				}
			}
		}
	}
}
