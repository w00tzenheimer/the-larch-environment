//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;

import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Event.PointerScrollEvent;
import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;




public abstract class DPContainer extends DPWidget
{
	public static class CouldNotFindChildException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	protected static interface ClosestPointChildSearcher
	{
		DPWidget getLeafClosestToLocalPointFromChild(ChildEntry entry, Point2 localPos, WidgetFilter filter);
	}
	
	protected static class ClosestPointChildContainerSearcher implements ClosestPointChildSearcher
	{
		private DPContainer container;
		
		
		public ClosestPointChildContainerSearcher(DPContainer container)
		{
			this.container = container;
		}

		public DPWidget getLeafClosestToLocalPointFromChild(ChildEntry entry, Point2 localPos, WidgetFilter filter)
		{
			return container.getChildLeafClosestToLocalPoint( localPos, filter );
		}
	}
	
	
	
	
	protected static class ChildEntry
	{
		public DPWidget child;
		public Xform2 childToContainerXform, containerToChildXform;
		public AABox2 box;
		public Point2 pos;
		public Vector2 size;
		
		
		public ChildEntry(DPWidget child)
		{
			this.child = child;
			childToContainerXform = new Xform2();
			containerToChildXform = new Xform2();
			box = new AABox2();
			pos = new Point2();
			size = new Vector2();
		}
		
		
		public boolean isContainerSpacePointWithinBounds(Point2 p)
		{
			return box.containsPoint( p );
		}
	}
	
	
	
	
	protected ArrayList<ChildEntry> childEntries;
	protected HashMap<DPWidget, ChildEntry> childToEntry;
	protected ChildEntry pressGrabChildEntry;
	protected int pressGrabButton;
	protected HashMap<PointerInterface, ChildEntry> pointerChildEntryTable, pointerDndChildEntryTable;
	
	
	
	
	//
	// Constructors
	//
	
	public DPContainer()
	{
		this( ContainerStyleSheet.defaultStyleSheet );
	}

	public DPContainer(ContainerStyleSheet styleSheet)
	{
		super( styleSheet );
		
		childEntries = new ArrayList<ChildEntry>();
		childToEntry = new HashMap<DPWidget, ChildEntry>();
		
		pointerChildEntryTable = new HashMap<PointerInterface, ChildEntry>();
		pointerDndChildEntryTable = new HashMap<PointerInterface, ChildEntry>();
	}
	
	
	
	//
	// Geometry methods
	//
	
	protected Xform2 getChildTransformRelativeToAncestor(DPWidget child, DPWidget ancestor, Xform2 x) throws IsNotInSubtreeException
	{
		ChildEntry entry = childToEntry.get( child );
		Xform2 localX = x.concat( entry.childToContainerXform );
		return getTransformRelativeToAncestor( ancestor, localX );
	}

	protected Point2 getChildLocalPointRelativeToAncestor(DPWidget child, DPWidget ancestor, Point2 p) throws IsNotInSubtreeException
	{
		ChildEntry entry = childToEntry.get( child );
		Point2 localP = entry.childToContainerXform.transform( p );
		return getLocalPointRelativeToAncestor( ancestor, localP );
	}
	

	protected void refreshScale(double scale, double rootScale)
	{
		super.refreshScale( scale, rootScale );
		
		for (ChildEntry childEntry: childEntries)
		{
			childEntry.child.setScale( 1.0, rootScale );
		}
	}
	
	
	
	
	//
	// Child registration methods
	//
	
	protected ChildEntry registerChildEntry(ChildEntry childEntry)
	{
		DPWidget child = childEntry.child;
		
		childToEntry.put( child, childEntry );
		
		child.unparent();
		
		child.setParent( this, presentationArea );
		
		if ( isRealised() )
		{
			child.handleRealise();
		}
		
		structureChanged();
		
		return childEntry;
	}
	
	protected void unregisterChildEntry(ChildEntry childEntry)
	{
		DPWidget child = childEntry.child;
		
		if ( isRealised() )
		{
			child.handleUnrealise( child );
		}
		
		child.setParent( null, null );
		
		childToEntry.remove( child );

		structureChanged();
	}
	
	
	
	
	
	
	//
	// Tree structure methods
	//
	
	
	protected abstract void removeChild(DPWidget child);
	
	public boolean hasChild(DPWidget child)
	{
		for (ChildEntry entry: childEntries)
		{
			if ( child == entry.child )
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	protected abstract List<DPWidget> getChildren();
	
	
	
	
	
	protected void structureChanged()
	{
		if ( parent != null )
		{
			parent.structureChanged();
		}
	}
	

	
	
	//
	// Event handling methods
	//
	
	protected void onLeaveIntoChild(PointerMotionEvent event, DPWidget child)
	{
	}
	
	protected void onEnterFromChild(PointerMotionEvent event, DPWidget child)
	{
	}
	
	
	
	
	
	protected void onChildResizeRequest(DPWidget child)
	{
		queueResize();
	}
	
	protected void childResizeRequest(DPWidget child)
	{
		onChildResizeRequest( child );
	}
	
	
	
	
	protected void allocateChildX(DPWidget child, double localPosX, double localWidth)
	{
		double childWidth = localWidth / child.scale;
		child.allocateX( childWidth );
		ChildEntry entry = childToEntry.get( child );
		
		entry.pos.x = localPosX;
		entry.size.x = localWidth;
	}
	
	protected void allocateChildY(DPWidget child, double localPosY, double localHeight)
	{
		double childHeight = localHeight / child.scale;
		child.allocateY( childHeight );
		ChildEntry entry = childToEntry.get( child );
		
		entry.pos.y = localPosY;
		entry.size.y = localHeight;
		
		entry.childToContainerXform = new Xform2( child.scale, entry.pos.toVector2() );
		entry.containerToChildXform = entry.childToContainerXform.inverse();
		entry.box = new AABox2( entry.pos, entry.pos.add( entry.size ) );
	}
	
	
	
	
	
	
	protected void childRedrawRequest(DPWidget child, Point2 childPos, Vector2 childSize)
	{
		ChildEntry entry = childToEntry.get( child );
		Point2 localPos = entry.childToContainerXform.transform( childPos );
		Vector2 localSize = entry.childToContainerXform.transform( childSize );
		queueRedraw( localPos, localSize );
	}
	
	
	
	protected ChildEntry getChildEntryAtLocalPoint(Point2 localPos)
	{
		for (ChildEntry entry: childEntries)
		{
			if ( entry.box.containsPoint( localPos ) )
			{
				return entry;
			}
		}
		
		return null;
	}
	
	//
	// Drag and drop methods
	//
	
	protected DndDrag handleDndButtonDown(PointerButtonEvent event)
	{
		ChildEntry entry = getChildEntryAtLocalPoint( event.pointer.getLocalPos() );
		if ( entry != null )
		{
			DndDrag drag = entry.child.handleDndButtonDown( event.transformed( entry.containerToChildXform ) );
			if ( drag != null )
			{
				return drag;
			}
			else
			{
				return super.handleDndButtonDown( event );
			}
		}
		
		return null;
	}
	
	protected boolean handleDndMotion(PointerMotionEvent event, DndDrag drag)
	{
		ChildEntry entry = getChildEntryAtLocalPoint( event.pointer.getLocalPos() );
		if ( entry != null )
		{
			boolean bDropped = entry.child.handleDndMotion( event.transformed( entry.containerToChildXform ), drag );
			if ( bDropped )
			{
				return true;
			}
			else
			{
				return super.handleDndMotion( event, drag );
			}
		}
		
		return false;
	}
	
	protected boolean handleDndButtonUp(PointerButtonEvent event, DndDrag drag)
	{
		ChildEntry entry = getChildEntryAtLocalPoint( event.pointer.getLocalPos() );
		if ( entry != null )
		{
			boolean bDropped = entry.child.handleDndButtonUp( event.transformed( entry.containerToChildXform ), drag );
			if ( bDropped )
			{
				return true;
			}
			else
			{
				return super.handleDndButtonUp( event, drag );
			}
		}
		
		return false;
	}
	
	
	
	
	
	//
	// Regular events
	//
	
	protected boolean handleButtonDown(PointerButtonEvent event)
	{
		if ( pressGrabChildEntry == null )
		{
			ChildEntry entry = getChildEntryAtLocalPoint( event.pointer.getLocalPos() );
			if ( entry != null )
			{
				boolean bHandled = entry.child.handleButtonDown( event.transformed( entry.containerToChildXform ) );
				if ( bHandled )
				{
					pressGrabChildEntry = entry;
					pressGrabButton = event.button;
					return true;
				}
			}
			
			if ( pressGrabChildEntry != null )
			{
				return onButtonDown( event );
			}
			else
			{
				return false;
			}
		}
		else
		{
			return pressGrabChildEntry.child.handleButtonDown( event.transformed( pressGrabChildEntry.containerToChildXform ) );
		}
	}
	
	protected boolean handleButtonDown2(PointerButtonEvent event)
	{
		if ( pressGrabChildEntry != null )
		{
			return pressGrabChildEntry.child.handleButtonDown2( event.transformed( pressGrabChildEntry.containerToChildXform ) );
		}
		else
		{
			return onButtonDown2( event );
		}
	}
	
	protected boolean handleButtonDown3(PointerButtonEvent event)
	{
		if ( pressGrabChildEntry != null )
		{
			return pressGrabChildEntry.child.handleButtonDown3( event.transformed( pressGrabChildEntry.containerToChildXform ) );
		}
		else
		{
			return onButtonDown3( event );
		}
	}
	
	protected boolean handleButtonUp(PointerButtonEvent event)
	{
		if ( pressGrabChildEntry != null )
		{
			PointerButtonEvent childSpaceEvent = event.transformed( pressGrabChildEntry.containerToChildXform );
			if ( event.button == pressGrabButton )
			{
				pressGrabButton = 0;
				Point2 localPos = event.pointer.getLocalPos();
				if ( !pressGrabChildEntry.isContainerSpacePointWithinBounds( localPos ) )
				{
					pressGrabChildEntry.child.handleLeave( new PointerMotionEvent( childSpaceEvent.pointer, PointerMotionEvent.Action.LEAVE ) );
				}
				
				boolean bHandled = pressGrabChildEntry.child.handleButtonUp( childSpaceEvent );
				ChildEntry savedPressGrabChildEntry = pressGrabChildEntry;
				pressGrabChildEntry = null;
				
				if ( localPos.x >= 0.0  &&  localPos.x <= allocation.x  &&  localPos.y >= 0.0  &&  localPos.y <= allocation.y )
				{
					ChildEntry entry = getChildEntryAtLocalPoint( localPos );
					if ( entry != null )
					{
						if ( entry != savedPressGrabChildEntry )
						{
							entry.child.handleEnter( new PointerMotionEvent( childSpaceEvent.pointer, PointerMotionEvent.Action.ENTER ) );
						}
						pointerChildEntryTable.put( event.pointer.concretePointer(), entry );
					}
					else
					{
						pointerChildEntryTable.remove( event.pointer.concretePointer() );
						onEnter( new PointerMotionEvent( event.pointer, PointerMotionEvent.Action.ENTER ) );
					}
				}
				
				return bHandled;
			}
			else
			{
				return pressGrabChildEntry.child.handleButtonUp( childSpaceEvent );
			}
		}
		else
		{
			return onButtonUp( event );
		}
	}


	protected void handleMotion(PointerMotionEvent event)
	{
		if ( pressGrabChildEntry != null )
		{
			pressGrabChildEntry.child.handleMotion( event.transformed( pressGrabChildEntry.containerToChildXform ) );
		}
		else
		{
			ChildEntry pointerChildEntry = pointerChildEntryTable.get( event.pointer.concretePointer() );
			ChildEntry oldPointerChildEntry = pointerChildEntry;
			
			if ( pointerChildEntry != null )
			{
				if ( !pointerChildEntry.isContainerSpacePointWithinBounds( event.pointer.getLocalPos() ) )
				{
					pointerChildEntry.child.handleLeave( new PointerMotionEvent( event.pointer.transformed( pointerChildEntry.containerToChildXform ), PointerMotionEvent.Action.LEAVE ) );
					pointerChildEntryTable.remove( event.pointer.concretePointer() );
					pointerChildEntry = null;
				}
				else
				{
					pointerChildEntry.child.handleMotion( event.transformed( pointerChildEntry.containerToChildXform ) );
				}
			}
			
			if ( pointerChildEntry == null )
			{
				ChildEntry entry = getChildEntryAtLocalPoint( event.pointer.getLocalPos() );
				if ( entry != null )
				{
					entry.child.handleEnter( event.transformed( entry.containerToChildXform ) );
					pointerChildEntry = entry;
					pointerChildEntryTable.put( event.pointer.concretePointer(), pointerChildEntry );
				}
			}
			
			if ( oldPointerChildEntry == null  &&  pointerChildEntry != null )
			{
				onLeaveIntoChild( new PointerMotionEvent( event.pointer, PointerMotionEvent.Action.LEAVE ), pointerChildEntry.child );
			}
			else if ( oldPointerChildEntry != null  &&  pointerChildEntry == null )
			{
				onEnterFromChild( new PointerMotionEvent( event.pointer, PointerMotionEvent.Action.ENTER ), oldPointerChildEntry.child );
			}
		}
		
		onMotion( event );
	}
	
	protected void handleEnter(PointerMotionEvent event)
	{
		onEnter( event );
		
		Point2 localPos = event.pointer.getLocalPos();
		
		for (int i = childEntries.size() - 1; i >= 0; i--)
		{
			ChildEntry entry = childEntries.get( i );
			if ( entry.isContainerSpacePointWithinBounds( localPos ) )
			{
				entry.child.handleEnter( event.transformed( entry.containerToChildXform ) );
				pointerChildEntryTable.put( event.pointer.concretePointer(), entry );
				onLeaveIntoChild( new PointerMotionEvent( event.pointer, PointerMotionEvent.Action.LEAVE ), entry.child );
				break;
			}
		}
	}
	
	protected void handleLeave(PointerMotionEvent event)
	{
		if ( pressGrabChildEntry == null )
		{
			ChildEntry pointerChildEntry = pointerChildEntryTable.get( event.pointer.concretePointer() );
			if ( pointerChildEntry != null )
			{
				pointerChildEntry.child.handleLeave( event.transformed( pointerChildEntry.containerToChildXform ) );
				onEnterFromChild( new PointerMotionEvent( event.pointer, PointerMotionEvent.Action.ENTER ), pointerChildEntry.child );
				pointerChildEntryTable.remove( event.pointer.concretePointer() );
			}
		}

		onLeave( event );
	}
	
	
	
	protected boolean handleScroll(PointerScrollEvent event)
	{
		ChildEntry pointerChildEntry = pointerChildEntryTable.get( event.pointer.concretePointer() );
		if ( pressGrabChildEntry != null )
		{
			pressGrabChildEntry.child.handleScroll( event.transformed( pressGrabChildEntry.containerToChildXform ) );
		}
		else if ( pointerChildEntry != null )
		{
			pointerChildEntry.child.handleScroll( event.transformed( pressGrabChildEntry.containerToChildXform ) );
		}
		return onScroll( event );
	}
	
	
	
	protected void handleRealise()
	{
		super.handleRealise();
		for (ChildEntry entry: childEntries)
		{
			entry.child.handleRealise();
		}
	}
	
	protected void handleUnrealise(DPWidget unrealiseRoot)
	{
		for (ChildEntry entry: childEntries)
		{
			entry.child.handleUnrealise( unrealiseRoot );
		}
		super.handleUnrealise( unrealiseRoot );
	}
	
	
	
	protected void drawBackground(Graphics2D graphics)
	{
		Color backgroundColour = getStyleSheet().getBackgroundColour();
		if ( backgroundColour != null )
		{
			graphics.setColor( backgroundColour );
			graphics.fill( new Rectangle2D.Double( 0.0, 0.0, allocation.x, allocation.y ) );
		}
	}
	
	protected void handleDraw(Graphics2D graphics, AABox2 areaBox)
	{
		drawBackground( graphics );
		super.handleDraw( graphics, areaBox );
		
		AffineTransform currentTransform = graphics.getTransform();
		for (ChildEntry entry: childEntries)
		{
			if ( entry.box.intersects( areaBox ) )
			{
				entry.childToContainerXform.apply( graphics );
				entry.child.handleDraw( graphics, entry.containerToChildXform.transform( areaBox ) );
				graphics.setTransform( currentTransform );
			}
		}
	}
	
	
	
	
	protected ChildEntry createChildEntryForChild(DPWidget child)
	{
		return new ChildEntry( child );
	}
	
	
	
	protected void setPresentationArea(DPPresentationArea area)
	{
		super.setPresentationArea( area );
		
		for (ChildEntry entry: childEntries)
		{
			entry.child.setPresentationArea( area );
		}
	}


	
	
	//
	//
	// CONTENT LEAF METHODS
	//
	//

	public DPContentLeaf getLeftContentLeaf()
	{
		// Check the child nodes
		List<DPWidget> navList = horizontalNavigationList();
		if ( navList != null )
		{
			for (DPWidget w: navList)
			{
				DPContentLeaf l = w.getLeftContentLeaf();
				if ( l != null )
				{
					return l;
				}
			}
		}
		
		return null;
	}
	
	public DPContentLeaf getRightContentLeaf()
	{
		// Check the child nodes
		List<DPWidget> navList = horizontalNavigationList();
		if ( navList != null )
		{
			for (int i = navList.size() - 1; i >= 0; i--)
			{
				DPWidget w = navList.get( i );
				DPContentLeaf l = w.getRightContentLeaf();
				if ( l != null )
				{
					return l;
				}
			}
		}
		
		return null;
	}

	protected DPContentLeaf getTopOrBottomContentLeaf(boolean bBottom, Point2 cursorPosInRootSpace, boolean bSkipWhitespace)
	{
		List<DPWidget> navList = verticalNavigationList();
		if ( navList != null )
		{
			if ( bBottom )
			{
				for (int i = navList.size() - 1; i >= 0; i--)
				{
					DPWidget w = navList.get( i );
					DPContentLeaf l = w.getTopOrBottomContentLeaf( bBottom, cursorPosInRootSpace, bSkipWhitespace );
					if ( l != null )
					{
						return l;
					}
				}
			}
			else
			{
				for (DPWidget w: navList)
				{
					DPContentLeaf l = w.getTopOrBottomContentLeaf( bBottom, cursorPosInRootSpace, bSkipWhitespace );
					if ( l != null )
					{
						return l;
					}
				}
			}
			
			return null;
		}
		else
		{
			navList = horizontalNavigationList();
			if ( navList != null )
			{
				double closestDistance = 0.0;
				DPContentLeaf closestNode = null;
				for (DPWidget item: navList)
				{
					AABox2 bounds = getLocalAABox();
					double lower = item.getLocalPointRelativeToRoot( bounds.getLower() ).x;
					double upper = item.getLocalPointRelativeToRoot( bounds.getUpper() ).x;
					if ( cursorPosInRootSpace.x >=  lower  &&  cursorPosInRootSpace.x <= upper )
					{
						DPContentLeaf l = item.getTopOrBottomContentLeaf( bBottom, cursorPosInRootSpace, bSkipWhitespace );
						if ( l != null )
						{
							return l;
						}
					}
					else
					{
						double distance;
						if ( cursorPosInRootSpace.x < lower )
						{
							// Cursor to the left of the box
							distance = lower - cursorPosInRootSpace.x;
						}
						else // cursorPosInRootSpace.x > upper
						{
							// Cursor to the right of the box
							distance = cursorPosInRootSpace.x - upper;
						}
						
						if ( closestNode == null  ||  distance < closestDistance )
						{
							DPContentLeaf l = item.getTopOrBottomContentLeaf( bBottom, cursorPosInRootSpace, bSkipWhitespace );
							if ( l != null )
							{
								closestDistance = distance;
								closestNode = l;
							}
						}
					}
				}
				
				if ( closestNode != null )
				{
					return closestNode;
				}
			}
			
			return null;
		}
	}
	
	
	protected DPContentLeaf getContentLeafToLeftFromChild(DPWidget child)
	{
		List<DPWidget> navList = horizontalNavigationList();
		if ( navList != null )
		{
			int index = navList.indexOf( child );
			if ( index != -1 )
			{
				for (int i = index - 1; i >= 0; i--)
				{
					DPWidget w = navList.get( i );
					DPContentLeaf l = w.getRightContentLeaf();
					if ( l != null )
					{
						return l;
					}
				}
			}
		}
		
		if ( parent != null )
		{
			return parent.getContentLeafToLeftFromChild( this );
		}
		else
		{
			return null;
		}
	}
	
	protected DPContentLeaf getContentLeafToRightFromChild(DPWidget child)
	{
		List<DPWidget> navList = horizontalNavigationList();
		if ( navList != null )
		{
			int index = navList.indexOf( child );
			if ( index != -1 )
			{
				for (int i = index + 1; i < navList.size(); i++)
				{
					DPWidget w = navList.get( i );
					DPContentLeaf l = w.getLeftContentLeaf();
					if ( l != null )
					{
						return l;
					}
				}
			}
		}
		
		if ( parent != null )
		{
			return parent.getContentLeafToRightFromChild( this );
		}
		else
		{
			return null;
		}
	}
	
	protected DPContentLeaf getContentLeafAboveOrBelowFromChild(DPWidget child, boolean bBelow, Point2 localCursorPos, boolean bSkipWhitespace)
	{
		List<DPWidget> navList = verticalNavigationList();
		if ( navList != null )
		{
			int index = navList.indexOf( child );
			if ( index != -1 )
			{
				Point2 cursorPosInRootSpace = getLocalPointRelativeToRoot( localCursorPos );
				if ( bBelow )
				{
					for (int i = index + 1; i < navList.size(); i++)
					{
						DPWidget w = navList.get( i );
						DPContentLeaf l = w.getTopOrBottomContentLeaf( false, cursorPosInRootSpace, bSkipWhitespace );
						if ( l != null )
						{
							return l;
						}
					}
				}
				else
				{
					for (int i = index - 1; i >= 0; i--)
					{
						DPWidget w = navList.get( i );
						DPContentLeaf l = w.getTopOrBottomContentLeaf( true, cursorPosInRootSpace, bSkipWhitespace );
						if ( l != null )
						{
							return l;
						}
					}
				}
			}
		}
		
		if ( parent != null )
		{
			try
			{
				return parent.getContentLeafAboveOrBelowFromChild( this, bBelow, getLocalPointRelativeToAncestor( parent, localCursorPos ), bSkipWhitespace );
			}
			catch (IsNotInSubtreeException e)
			{
				throw new RuntimeException();
			}
		}
		else
		{
			return null;
		}
	}
	
	
	
	
	protected DPWidget getLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		if ( filter.testContainer( this ) )
		{
			return getChildLeafClosestToLocalPoint( localPos, filter );
		}
		else
		{
			return null;
		}
	}

	protected abstract DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter);
	
	protected DPWidget getLeafClosestToLocalPointFromChild(ChildEntry childEntry, Point2 localPos, WidgetFilter filter)
	{
		return childEntry.child.getLeafClosestToLocalPoint( childEntry.containerToChildXform.transform( localPos ), filter );
	}
	

	
	protected DPWidget getChildLeafClosestToLocalPointHorizontal(Point2 localPos, WidgetFilter filter)
	{
		if ( childEntries.size() == 0 )
		{
			return null;
		}
		else if ( childEntries.size() == 1 )
		{
			return getLeafClosestToLocalPointFromChild( childEntries.get( 0 ), localPos, filter );
		}
		else
		{
			ChildEntry start = null;
			int startIndex = -1;
			ChildEntry entryI = childEntries.get( 0 );
			for (int i = 0; i < childEntries.size() - 1; i++)
			{
				ChildEntry entryJ = childEntries.get( i + 1 );
				double iUpperX = entryI.pos.x + entryI.size.x;
				double jLowerX = entryJ.pos.x;
				
				double midx = ( iUpperX + jLowerX ) * 0.5;
				
				if ( localPos.x < midx )
				{
					startIndex = i;
					start = entryI;
					break;
				}
				
				entryI = entryJ;
			}
			
			if ( start == null )
			{
				startIndex = childEntries.size() - 1;
				start = childEntries.get( startIndex );
			}
			
			DPWidget c = getLeafClosestToLocalPointFromChild( start, localPos, filter );
			if ( c != null )
			{
				return c;
			}
			else
			{
				ChildEntry next = null;
				DPWidget nextC = null;
				for (int j = startIndex + 1; j < childEntries.size(); j++)
				{
					nextC = getLeafClosestToLocalPointFromChild( childEntries.get( j ), localPos, filter );
					if ( nextC != null )
					{
						next = childEntries.get( j );
						break;
					}
				}

				ChildEntry prev = null;
				DPWidget prevC = null;
				for (int j = startIndex - 1; j >= 0; j--)
				{
					prevC = getLeafClosestToLocalPointFromChild( childEntries.get( j ), localPos, filter );
					if ( prevC != null )
					{
						prev = childEntries.get( j );
						break;
					}
				}
				

				if ( prev == null  &&  next == null )
				{
					return null;
				}
				else if ( prev == null  &&  next != null )
				{
					return nextC;
				}
				else if ( prev != null  &&  next == null )
				{
					return prevC;
				}
				else
				{
					double distToPrev = localPos.x - ( prev.pos.x + prev.size.x );
					double distToNext = next.pos.x - localPos.x;
					
					return distToPrev > distToNext  ?  prevC  :  nextC;
				}
			}
		}
	}
	
	protected DPWidget getChildLeafClosestToLocalPointVertical(Point2 localPos, WidgetFilter filter)
	{
		if ( childEntries.size() == 0 )
		{
			return null;
		}
		else if ( childEntries.size() == 1 )
		{
			return getLeafClosestToLocalPointFromChild( childEntries.get( 0 ), localPos, filter );
		}
		else
		{
			ChildEntry start = null;
			int startIndex = -1;
			ChildEntry entryI = childEntries.get( 0 );
			for (int i = 0; i < childEntries.size() - 1; i++)
			{
				ChildEntry entryJ = childEntries.get( i + 1 );
				double iUpperY = entryI.pos.y + entryI.size.y;
				double jLowerY = entryJ.pos.y;
				
				double midY = ( iUpperY + jLowerY ) * 0.5;
				
				if ( localPos.y < midY )
				{
					startIndex = i;
					start = entryI;
					break;
				}
				
				entryI = entryJ;
			}
			
			if ( start == null )
			{
				startIndex = childEntries.size() - 1;
				start = childEntries.get( startIndex );
			}
			
			DPWidget c = getLeafClosestToLocalPointFromChild( start, localPos, filter );
			if ( c != null )
			{
				return c;
			}
			else
			{
				ChildEntry next = null;
				DPWidget nextC = null;
				for (int j = startIndex + 1; j < childEntries.size(); j++)
				{
					nextC = getLeafClosestToLocalPointFromChild( childEntries.get( j ), localPos, filter );
					if ( nextC != null )
					{
						next = childEntries.get( j );
						break;
					}
				}

				ChildEntry prev = null;
				DPWidget prevC = null;
				for (int j = startIndex - 1; j >= 0; j--)
				{
					prevC = getLeafClosestToLocalPointFromChild( childEntries.get( j ), localPos, filter );
					if ( prevC != null )
					{
						prev = childEntries.get( j );
						break;
					}
				}
				
				
				if ( prev == null  &&  next == null )
				{
					return null;
				}
				else if ( prev == null  &&  next != null )
				{
					return nextC;
				}
				else if ( prev != null  &&  next == null )
				{
					return prevC;
				}
				else
				{
					double distToPrev = localPos.y - ( prev.pos.y + prev.size.y );
					double distToNext = next.pos.y - localPos.y;
					
					return distToPrev > distToNext  ?  prevC  :  nextC;
				}
			}
		}
	}
	
	
	
	
	//
	//
	// STYLESHEET METHODS
	//
	//
	
	protected ContainerStyleSheet getStyleSheet()
	{
		return (ContainerStyleSheet)styleSheet;
	}
}
