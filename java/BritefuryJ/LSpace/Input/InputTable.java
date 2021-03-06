//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Input;

import java.util.ArrayList;
import java.util.WeakHashMap;

import javax.swing.JComponent;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.PresentationComponent;
import BritefuryJ.LSpace.LSRootElement;




public class InputTable
{
	private WeakHashMap<LSElement, ArrayList<PointerInterface>> pointersWithinBoundsByElement;

	protected Pointer mouse;
	protected LSElement rootElement;
	protected JComponent component;
	
	
	public InputTable(LSRootElement rootElement, DndController dndController, PresentationComponent component)
	{
		pointersWithinBoundsByElement = new WeakHashMap<LSElement, ArrayList<PointerInterface>>();
		this.rootElement = rootElement;
		this.component = component;
		mouse = new Pointer( this, rootElement, dndController, component );
	}
	
	
	public Pointer getMouse()
	{
		return mouse;
	}
	
	
	
	public void onElementUnrealised(LSElement element)
	{
		mouse.onElementUnrealised( element );
	}
	
	public void onRootElementReallocate()
	{
		mouse.onRootElementReallocate();
	}
	
	
	public boolean arePointersWithinBoundsOfElement(LSElement element)
	{
		return pointersWithinBoundsByElement.containsKey( element );
	}
	
	public ArrayList<PointerInterface> getPointersWithinBoundsOfElement(LSElement element)
	{
		return pointersWithinBoundsByElement.get( element );
	}
	
	
	protected void addPointerWithinElementBounds(PointerInterface pointer, LSElement element)
	{
		ArrayList<PointerInterface> pointersWithinBounds = pointersWithinBoundsByElement.get( element );
		if ( pointersWithinBounds == null )
		{
			pointersWithinBounds = new ArrayList<PointerInterface>();
			pointersWithinBoundsByElement.put( element, pointersWithinBounds );
		}
		if ( !pointersWithinBounds.contains( pointer ) )
		{
			pointersWithinBounds.add( pointer );
		}
	}

	protected void removePointerWithinElementBounds(PointerInterface pointer, LSElement element)
	{
		ArrayList<PointerInterface> pointersWithinBounds = pointersWithinBoundsByElement.get( element );
		if ( pointersWithinBounds != null )
		{
			pointersWithinBounds.remove( pointer );
			if ( pointersWithinBounds.isEmpty() )
			{
				pointersWithinBounds = null;
				pointersWithinBoundsByElement.remove( element );
			}
		}
	}
}
