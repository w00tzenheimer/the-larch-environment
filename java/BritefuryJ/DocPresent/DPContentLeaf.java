//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.LeafElement;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.ContentLeafStyleSheet;
import BritefuryJ.Math.Point2;

public abstract class DPContentLeaf extends DPWidget
{
	public static class CannotCreateMarkerWithEmptyContent extends RuntimeException
	{
		static final long serialVersionUID = 0L;
		
		public CannotCreateMarkerWithEmptyContent()
		{
		}
	}
	
	

	
	private WeakHashMap<Marker, Object> markers;
	protected String textRepresentation;
	
	
	
	DPContentLeaf(String textRepresentation)
	{
		this( ContentLeafStyleSheet.defaultStyleSheet, textRepresentation );
	}
	
	DPContentLeaf(ContentLeafStyleSheet styleSheet, String textRepresentation)
	{
		super( styleSheet );
		
		this.textRepresentation = textRepresentation;
	}
	
	
	
	
	//
	// Marker range methods
	//
	
	public abstract int getMarkerRange();
	
	protected void markerRangeChanged(int oldLength, int newLength)
	{
		if ( newLength > oldLength )
		{
			markerInsert( oldLength, newLength - oldLength );
		}
		else if ( newLength < oldLength )
		{
			markerRemove( newLength, oldLength - newLength );
		}
	}
	
	public abstract int getMarkerPositonForPoint(Point2 localPos);
	
	
	
	
	
	//
	//
	// CARET METHODS
	//
	//
	
	public abstract void drawCaret(Graphics2D graphics, Caret c);
	public abstract void drawCaretAtStart(Graphics2D graphics);
	public abstract void drawCaretAtEnd(Graphics2D graphics);
	
	
	protected void onCaretEnter(Caret c)
	{
	}
	
	protected void onCaretLeave(Caret c)
	{
	}
	
	
	protected void handleCaretEnter(Caret c)
	{
		onCaretEnter( c );
		if ( element != null )
		{
			((LeafElement)element).onCaretEnter();
		}
	}
	
	protected void handleCaretLeave(Caret c)
	{
		if ( element != null )
		{
			((LeafElement)element).onCaretLeave();
		}
		onCaretLeave( c );
	}
	
	
	
	
	//
	//
	// SELECTION METHODS
	//
	//
	
	public abstract void drawSelection(Graphics2D graphics, Marker from, Marker to);
	

	protected void drawSubtreeSelection(Graphics2D graphics, Marker startMarker, List<DPWidget> startPath, Marker endMarker, List<DPWidget> endPath)
	{
		drawSelection( graphics, startMarker, endMarker );
	}
	
	

	
	
	//
	//
	// MARKER METHODS
	//
	//	
	
	public Marker marker(int position, Marker.Bias bias)
	{
		return new Marker( this, position, bias );
	}
	
	public Marker markerAtStart()
	{
		return marker( 0, Marker.Bias.START );
	}
	
	public Marker markerAtStartPlusOne()
	{
		return marker( Math.min( 1, getMarkerRange() ), Marker.Bias.START );
	}
	
	public Marker markerAtEnd()
	{
		return marker( Math.max( getMarkerRange() - 1, 0 ), Marker.Bias.END );
	}
	
	public Marker markerAtEndMinusOne()
	{
		return marker( Math.max( getMarkerRange() - 1, 0 ), Marker.Bias.START );
	}
	
	
	public Marker markerAtPoint(Point2 localPos)
	{
		int markerPos = getMarkerPositonForPoint( localPos );
		return marker( markerPos, Marker.Bias.START );
	}



	public void moveMarker(Marker m, int position, Marker.Bias bias)
	{
		m.set( this, position, bias );
	}
	
	public void moveMarkerToStart(Marker m)
	{
		moveMarker( m, 0, Marker.Bias.START );
	}
	
	public void moveMarkerToStartPlusOne(Marker m)
	{
		moveMarker( m, Math.min( 1, getMarkerRange() ), Marker.Bias.START );
	}
	
	public void moveMarkerToEnd(Marker m)
	{
		moveMarker( m, Math.max( getMarkerRange() - 1, 0 ), Marker.Bias.END );
	}
	
	public void moveMarkerToEndMinusOne(Marker m)
	{
		moveMarker( m, Math.max( getMarkerRange() - 1, 0 ), Marker.Bias.START );
	}
	
	public void moveMarkerToPoint(Marker m, Point2 localPos)
	{
		int markerPos = getMarkerPositonForPoint( localPos );
		moveMarker( m, markerPos, Marker.Bias.START );
	}

	
	
	public boolean isMarkerAtStart(Marker m)
	{
		if ( m.getWidget() == this )
		{
			return m.getIndex() == 0;
		}
		else
		{
			return false;
		}
	}
	
	public boolean isMarkerAtEnd(Marker m)
	{
		if ( m.getWidget() == this )
		{
			return m.getIndex() == getMarkerRange();
		}
		else
		{
			return false;
		}
	}
	
	
	
	
	
	public void registerMarker(Marker m)
	{
		if ( markers == null )
		{
			markers = new WeakHashMap<Marker, Object>();
		}
		markers.put( m, null );
	}
	
	public void unregisterMarker(Marker m)
	{
		if ( markers != null )
		{
			markers.remove( m );
		
			if ( markers.isEmpty() )
			{
				markers = null;
			}
		}
	}
	
	
	
	
	public void markerInsert(int position, int length)
	{
		if ( markers != null )
		{
			for (Marker m: markers.keySet())
			{
				if ( m.getIndex() > position )
				{
					m.setPosition( m.getPosition() + length );
				}
				else if ( m.getIndex() == position )
				{
					m.setPositionAndBias( position + length - 1, Marker.Bias.END );
				}
			}
		}
	}
	
	public void markerRemove(int position, int length)
	{
		if ( markers != null )
		{
			int end = position + length;
	
			for (Marker m: markers.keySet())
			{
				if ( m.getIndex() >= position )
				{
					if ( m.getIndex() > end )
					{
						m.setPosition( m.getPosition() - length );
					}
					else
					{
						m.setPositionAndBias( position, Marker.Bias.START );
					}
				}
			}
		}
	}

	
	

	// MARKER MOVEMENT METHODS
	
	protected void moveMarkerLeft(Marker marker, boolean bSkipWhitespace)
	{
		if ( isMarkerAtStart( marker ) )
		{
			DPContentLeaf left = getContentLeafToLeft();
			boolean bSkippedWhitespace = false;
			

			if ( bSkipWhitespace )
			{
				while ( left != null  &&  left.isWhitespace() )
				{
					left = left.getContentLeafToLeft();
					bSkippedWhitespace = true;
				}
			}

			if ( left != null )
			{
				if ( bSkippedWhitespace )
				{
					left.moveMarkerToEnd( marker );
				}
				else
				{
					left.moveMarkerToEndMinusOne( marker );
				}
			}
		}
		else
		{
			moveMarker( marker, marker.getIndex() - 1, Marker.Bias.START );
		}
	}



	protected void moveMarkerRight(Marker marker, boolean bSkipWhitespace)
	{
		if ( isMarkerAtEnd( marker ) )
		{
			DPContentLeaf right = getContentLeafToRight();
			boolean bSkippedWhitespace = false;
			

			if ( bSkipWhitespace )
			{
				while ( right != null  &&  right.isWhitespace() )
				{
					right = right.getContentLeafToRight();
					bSkippedWhitespace = true;
				}
			}

			if ( right != null )
			{
				if ( bSkippedWhitespace )
				{
					right.moveMarkerToStart( marker );
				}
				else
				{
					right.moveMarkerToStartPlusOne( marker );
				}
			}
		}
		else
		{
			moveMarker( marker, marker.getIndex(), Marker.Bias.END );
		}
	}
	
	protected void moveMarkerUp(Marker marker, boolean bSkipWhitespace)
	{
		Point2 cursorPos = getMarkerPosition( marker );
		DPContentLeaf above = getContentLeafAbove( cursorPos, bSkipWhitespace );
		if ( above != null )
		{
			Point2 cursorPosInAbove = getLocalPointRelativeTo( above, cursorPos );
			int contentPos = above.getMarkerPositonForPoint( cursorPosInAbove );
			above.moveMarker( marker, contentPos, Marker.Bias.START );
		}
	}
	
	protected void moveMarkerDown(Marker marker, boolean bSkipWhitespace)
	{
		Point2 cursorPos = getMarkerPosition( marker );
		DPContentLeaf below = getContentLeafBelow( cursorPos, bSkipWhitespace );
		if ( below != null )
		{
			Point2 cursorPosInBelow = getLocalPointRelativeTo( below, cursorPos );
			int contentPos = below.getMarkerPositonForPoint( cursorPosInBelow );
			below.moveMarker( marker, contentPos, Marker.Bias.START );
		}
	}
	
	protected void moveMarkerHome(Marker marker)
	{
		if ( element != null )
		{
			Element segment = null, homeElement = null;
			segment = element.getSegment();
			homeElement = segment != null  ?  segment.getFirstEditableEntryLeafInSubtree()  :  null;
			if ( segment != null  &&  element == homeElement  &&  isMarkerAtStart( marker ) )
			{
				segment = segment.getParent().getSegment();
				homeElement = segment != null  ?  segment.getFirstEditableEntryLeafInSubtree()  :  null;
			}
			
			if ( homeElement != null )
			{
				homeElement.getWidget().moveMarkerToStart( marker );
			}
		}
	}
	
	protected void moveMarkerEnd(Marker marker)
	{
		if ( element != null )
		{
			Element segment = null, endElement = null;
			segment = element.getSegment();
			endElement = segment != null  ?  segment.getLastEditableEntryLeafInSubtree()  :  null;
			if ( segment != null  &&  element == endElement  &&  isMarkerAtEnd( marker ) )
			{
				segment = segment.getParent().getSegment();
				endElement = segment != null  ?  segment.getLastEditableEntryLeafInSubtree()  :  null;
			}
			
			if ( endElement != null )
			{
				endElement.getWidget().moveMarkerToEnd( marker );
			}
		}
	}
	
	
	

	
	
	//
	//
	// ELEMENT TREE STRUCTURE METHODS
	//
	//
	
	public DPContentLeaf getFirstLeafInSubtree(WidgetFilter branchFilter, WidgetFilter leafFilter)
	{
		if ( leafFilter == null  ||  leafFilter.testElement( this ) )
		{
			return this;
		}
		else
		{
			return null;
		}
	}

	public DPContentLeaf getLastLeafInSubtree(WidgetFilter branchFilter, WidgetFilter leafFilter)
	{
		if ( leafFilter == null  ||  leafFilter.testElement( this ) )
		{
			return this;
		}
		else
		{
			return null;
		}
	}

	

	public DPContentLeaf getPreviousLeaf()
	{
		return getPreviousLeaf( null, null, null );
	}

	public DPContentLeaf getNextLeaf()
	{
		return getNextLeaf( null, null, null );
	}


	public DPContentLeaf getPreviousLeaf(WidgetFilter subtreeRootFilter, WidgetFilter branchFilter, WidgetFilter leafFilter)
	{
		if ( parent != null )
		{
			return parent.getPreviousLeafFromChild( this, subtreeRootFilter, branchFilter, leafFilter );
		}
		else
		{
			return null;
		}
	}
	
	public DPContentLeaf getNextLeaf(WidgetFilter subtreeRootFilter, WidgetFilter branchFilter, WidgetFilter leafFilter)
	{
		if ( parent != null )
		{
			return parent.getNextLeafFromChild( this, subtreeRootFilter, branchFilter, leafFilter );
		}
		else
		{
			return null;
		}
	}
	
	
	public DPContentLeaf getPreviousEditableLeaf(WidgetFilter subtreeRootFilter, WidgetFilter branchFilter)
	{
		return getPreviousLeaf( subtreeRootFilter, branchFilter, new DPContentLeafEditable.EditableLeafElementFilter() );
	}

	public DPContentLeaf getNextEditableLeaf(WidgetFilter subtreeRootFilter, WidgetFilter branchFilter)
	{
		return getNextLeaf( subtreeRootFilter, branchFilter, new DPContentLeafEditable.EditableLeafElementFilter() );
	}


	public DPContentLeaf getPreviousEditableLeaf()
	{
		return getPreviousEditableLeaf( null, null );
	}

	public DPContentLeaf getNextEditableLeaf()
	{
		return getNextEditableLeaf( null, null );
	}


	public DPContentLeaf getPreviousEditableEntryLeaf(WidgetFilter subtreeRootFilter, WidgetFilter branchFilter)
	{
		return getPreviousLeaf( subtreeRootFilter, branchFilter, new DPContentLeafEditableEntry.EditableEntryLeafElementFilter() );
	}

	public DPContentLeaf getNextEditableEntryLeaf(WidgetFilter subtreeRootFilter, WidgetFilter branchFilter)
	{
		return getNextLeaf( subtreeRootFilter, branchFilter, new DPContentLeafEditableEntry.EditableEntryLeafElementFilter() );
	}


	public DPContentLeaf getPreviousEditableEntryLeaf()
	{
		return getPreviousEditableEntryLeaf( null, null );
	}

	public DPContentLeaf getNextEditableEntryLeaf()
	{
		return getNextEditableEntryLeaf( null, null );
	}

	
	
	

	
	//
	//
	// CONTENT LEAF METHODS
	//
	//
	
	public DPContentLeafEditable getEditableContentLeafToLeft()
	{
		DPContentLeaf leaf = getContentLeafToLeft();
		
		while ( leaf != null  &&  !leaf.isEditable() )
		{
			leaf = leaf.getContentLeafToLeft();
		}
		
		return (DPContentLeafEditable)leaf;
	}
	
	public DPContentLeafEditable getEditableContentLeafToRight()
	{
		DPContentLeaf leaf = getContentLeafToRight();
		
		while ( leaf != null  &&  !leaf.isEditable() )
		{
			leaf = leaf.getContentLeafToRight();
		}
		
		return (DPContentLeafEditable)leaf;
	}
	

	
	public DPContentLeafEditableEntry getEditableEntryContentLeafToLeft()
	{
		DPContentLeaf leaf = getContentLeafToLeft();
		
		while ( leaf != null  &&  !leaf.isEditableEntry() )
		{
			leaf = leaf.getContentLeafToLeft();
		}
		
		return (DPContentLeafEditableEntry)leaf;
	}
	
	public DPContentLeafEditableEntry getEditableEntryContentLeafToRight()
	{
		DPContentLeaf leaf = getContentLeafToRight();
		
		while ( leaf != null  &&  !leaf.isEditableEntry() )
		{
			leaf = leaf.getContentLeafToRight();
		}
		
		return (DPContentLeafEditableEntry)leaf;
	}
	
	
	
	protected DPContentLeaf getContentLeafAbove(Point2 localPos, boolean bSkipWhitespace)
	{
		return getContentLeafAboveOrBelow( localPos, false, bSkipWhitespace );
	}
	
	protected DPContentLeaf getContentLeafBelow(Point2 localPos, boolean bSkipWhitespace)
	{
		return getContentLeafAboveOrBelow( localPos, true, bSkipWhitespace );
	}
	
	protected DPContentLeaf getContentLeafAboveOrBelow(Point2 localPos, boolean bBelow, boolean bSkipWhitespace)
	{
		if ( parent != null )
		{
			return parent.getContentLeafAboveOrBelowFromChild( this, bBelow, getLocalPointRelativeToAncestor( parent, localPos ), bSkipWhitespace );
		}
		else
		{
			return null;
		}
	}
	
	
	public DPContentLeaf getLeftContentLeaf()
	{
		return this;
	}

	public DPContentLeaf getRightContentLeaf()
	{
		return this;
	}

	protected DPContentLeaf getTopOrBottomContentLeaf(boolean bBottom, Point2 cursorPosInRootSpace, boolean bSkipWhitespace)
	{
		if ( bSkipWhitespace && isWhitespace() )
		{
			return null;
		}
		else
		{
			return this;
		}
	}
	
	
	protected DPWidget getLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		if ( filter == null  ||  filter.testElement( this ) )
		{
			return this;
		}
		else
		{
			return null;
		}
	}


	
	
	
	//
	//
	// REALISE / UNREALISE
	//
	//

	protected void onRealise()
	{
		super.onRealise();
	}
	
	protected void onUnrealise(DPWidget unrealiseRoot)
	{
		super.onUnrealise( unrealiseRoot );
		
		if ( markers != null )
		{
			ArrayList<Marker> xs = new ArrayList<Marker>( markers.keySet() );
			
			if ( xs.size() > 0 )
			{
				DPContentLeaf left = unrealiseRoot.getContentLeafToLeft();
				
				while ( left != null  &&  !left.isRealised() )
				{
					left = left.getContentLeafToLeft();
				}
				
				if ( left != null )
				{
					for (Marker x: xs)
					{
						try
						{
							left.moveMarkerToEnd( x );
						}
						catch (Marker.InvalidMarkerPosition e)
						{
						}
					}
				}
				else
				{
					DPContentLeaf right = unrealiseRoot.getContentLeafToRight();
					
					while ( right != null  &&  !right.isRealised() )
					{
						right = right.getContentLeafToRight();
					}
					
					if ( right != null )
					{
						for (Marker x: xs)
						{
							try
							{
								right.moveMarkerToStart( x );
							}
							catch (Marker.InvalidMarkerPosition e)
							{
							}
						}
					}
					else
					{
						for (Marker x: xs)
						{
							unregisterMarker( x );
							x.clear();
						}
					}
				}
			}
		}
	}
	
	
	
	//
	//
	// TEXT REPRESENTATION METHODS
	//
	//
	public String getTextRepresentation()
	{
		return textRepresentation;
	}
	
	public int getTextRepresentationLength()
	{
		return textRepresentation.length();
	}
	
	protected void getTextRepresentationFromStartToPath(StringBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex)
	{
		builder.append( textRepresentation.substring( 0, marker.getIndex() ) );
	}

	protected void getTextRepresentationFromPathToEnd(StringBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex)
	{
		builder.append( textRepresentation.substring( marker.getIndex() ) );
	}

	public String getTextRepresentationBetweenMarkers(Marker startMarker, Marker endMarker)
	{
		if ( startMarker.getWidget() != this  ||  endMarker.getWidget() != this )
		{
			throw new RuntimeException();
		}
		return textRepresentation.substring( startMarker.getIndex(), endMarker.getIndex() );
	}

	protected void getTextRepresentationFromStartOfRootToMarker(StringBuilder builder, Marker marker, DPWidget root)
	{
		if ( this != root  &&  parent != null )
		{
			parent.getTextRepresentationFromStartOfRootToMarkerFromChild( builder, marker, root, this );
		}
		builder.append( textRepresentation.substring( 0, marker.getIndex() ) );
	}
	
	protected void getTextRepresentationFromMarkerToEndOfRoot(StringBuilder builder, Marker marker, DPWidget root)
	{
		builder.append( textRepresentation.substring( marker.getIndex() ) );
		if ( this != root  &&  parent != null )
		{
			parent.getTextRepresentationFromMarkerToEndOfRootFromChild( builder, marker, root, this );
		}
	}
	
	public DPContentLeaf getLeafAtTextRepresentationPosition(int position)
	{
		return this;
	}

	
	
	
	//
	// TYPE METHODS
	//

	public boolean isContentLeaf()
	{
		return true;
	}
	
	public boolean isWhitespace()
	{
		return false;
	}
	
	public boolean isEditable()
	{
		return false;
	}
	
	public boolean isEditableEntry()
	{
		return false;
	}
}
