package BritefuryJ.DocPresent.ElementTree;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementTree.Marker.ElementMarker;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;

public class ParagraphElement extends SequenceBranchElement
{
	private enum Mode { NONE, INDEPENDENT, INPARENT };
	
	private ParagraphStyleSheet styleSheet;
	private DPParagraph paragraph;
	private Mode mode;
	private ParagraphElement paragraphParent;
	private Vector<ParagraphElement> paraChildParagraphs;
	
	
	public ParagraphElement()
	{
		this( ParagraphStyleSheet.defaultStyleSheet );
	}

	public ParagraphElement(ParagraphStyleSheet styleSheet)
	{
		super( null );
		
		this.styleSheet = styleSheet;
		this.paragraph = null;
		this.mode = Mode.NONE;
		this.paragraphParent = null;
		this.paraChildParagraphs = null;
	}


	public DPParagraph getWidget()
	{
		refreshParagraph();
		return (DPParagraph)widget;
	}
	
	
	
	private void refreshParagraph()
	{
		if ( mode == Mode.NONE )
		{
			setMode( Mode.INDEPENDENT );
		}
	}
	
	private void setMode(Mode m)
	{
		mode = m;
		
		if ( mode == Mode.INDEPENDENT )
		{
			paragraph = new DPParagraph( styleSheet );
			widget = paragraph;
			paragraphParent = null;
			paraChildParagraphs = new Vector<ParagraphElement>();
			if ( tree != null )
			{
				tree.registerElement( this );
			}
		}
		else if ( mode == Mode.INPARENT )
		{
			if ( tree != null )
			{
				tree.unregisterElement( this );
			}
			paragraph = null;
			widget = null;
			paragraphParent = null;
			paraChildParagraphs = null;
		}
		else
		{
			if ( tree != null )
			{
				tree.unregisterElement( this );
			}
			paragraph = null;
			widget = null;
			paragraphParent = null;
			paraChildParagraphs = null;
		}
	}
	
	
	
	private void gatherParagraphContents(List<Element> childElementsOut, List<ParagraphElement> paragraphElementsOut, List<Element> childElements)
	{
		for (Element child: childElements)
		{
			if ( child.isParagraph() )
			{
				ParagraphElement p = (ParagraphElement)child;
				paragraphElementsOut.add( p );
				p.gatherParagraphContents( childElementsOut, paragraphElementsOut, p.children );
			}
			else
			{
				childElementsOut.add( child );
			}
		}
	}
	
	private void setParagraphParent(ParagraphElement p)
	{
		paragraphParent = p;
	}
	
	
	
	private void refreshContents()
	{
		if ( mode == Mode.INDEPENDENT )
		{
			// Gather the paragraph contents that are current, and the state that they will be in after completion
			Vector<Element> newChildren = new Vector<Element>();
			Vector<ParagraphElement> newParas = new Vector<ParagraphElement>();
			
			gatherParagraphContents( newChildren, newParas, children );
			
			
			// Generate the list of child widgets
			Vector<DPWidget> childWidgets = new Vector<DPWidget>();
			
			childWidgets.setSize( newChildren.size() );
			for (int i = 0; i < newChildren.size(); i++)
			{
				childWidgets.set( i, newChildren.get( i ).getWidget() );
			}
			

			
			// Work out what has been added, and what has been removed
			HashSet<ParagraphElement> addedParas, removedParas;
			
			addedParas = new HashSet<ParagraphElement>( newParas );
			removedParas = new HashSet<ParagraphElement>( paraChildParagraphs );
			addedParas.removeAll( paraChildParagraphs );
			removedParas.removeAll( newParas );
			
			
			for (ParagraphElement x: removedParas)
			{
				x.setParagraphParent( null );
				x.setMode( Mode.NONE );
			}
			
			paraChildParagraphs = newParas;
			
			getWidget().setChildren( childWidgets );
	
			for (ParagraphElement x: addedParas)
			{
				x.setMode( Mode.INPARENT );
				x.setParagraphParent( this );
			}
		}
	}
	
	
	private void onChildParagraphContentsChanged(ParagraphElement element)
	{
		refreshContents();
	}
	
	
	public void setChildren(List<Element> xs)
	{
		// Work out which children were added and removed
		HashSet<Element> added, removed;
		
		added = new HashSet<Element>( xs );
		removed = new HashSet<Element>( children );
		added.removeAll( children );
		removed.removeAll( xs );
		
		
		// Make the changes to the sub-tree
		for (Element x: removed)
		{
			x.setParent( null );
			x.setElementTree( null );
		}
		
		children.clear();
		children.addAll( xs );
		
		for (Element x: added)
		{
			x.setParent( this );
			x.setElementTree( tree );
		}


		
		if ( mode == Mode.NONE )
		{
			setMode( Mode.INDEPENDENT );
		}
		
		if ( mode == Mode.INDEPENDENT )
		{
			// Refresh the widget contents
			refreshContents();
		}
		else if ( mode == Mode.INPARENT )
		{
			paragraphParent.onChildParagraphContentsChanged( this );
		}
	}



	protected boolean isParagraph()
	{
		return true;
	}



	public DPWidget getWidgetAtContentStart()
	{
		if ( mode ==  Mode.INDEPENDENT )
		{
			return getWidget();
		}
		else
		{
			if ( children.size() > 0 )
			{
				return children.get( 0 ).getWidgetAtContentStart();
			}
			else
			{
				return null;
			}
		}
	}
	
	public int getContentOffsetOfChild(Element elem)
	{
		int offset = 0;
		for (Element c: children)
		{
			if ( c == elem )
			{
				return offset;
			}
			offset += c.getContentLength();
		}
		
		throw new DPContainer.CouldNotFindChildException();
	}

	
	public String getContent()
	{
		String result = "";
		
		for (Element child: children)
		{
			result += child.getContent();
		}
		
		return result;
	}
	
	public int getContentLength()
	{
		int result = 0;
		
		for (Element child: children)
		{
			result += child.getContentLength();
		}
		
		return result;
	}
	
	
	
	
	//
	//
	// MARKER METHODS
	//
	//
	
	public ElementMarker marker(int position, Marker.Bias bias)
	{
		return new ElementMarker( tree, getWidget().marker( position, bias ) );
	}
	
	public ElementMarker markerAtStart()
	{
		return new ElementMarker( tree, getWidget().markerAtStart() );
	}
	
	public ElementMarker markerAtEnd()
	{
		return new ElementMarker( tree, getWidget().markerAtEnd() );
	}
	
	
	public void moveMarker(ElementMarker m, int position, Marker.Bias bias)
	{
		getWidget().moveMarker( m.getWidgetMarker(), position, bias );
	}
	
	public void moveMarkerToStart(ElementMarker m)
	{
		getWidget().moveMarkerToStart( m.getWidgetMarker() );
	}
	
	public void moveMarkerToEnd(ElementMarker m)
	{
		getWidget().moveMarkerToEnd( m.getWidgetMarker() );
	}
	
	
	
	public boolean isMarkerAtStart(ElementMarker m)
	{
		return getWidget().isMarkerAtStart( m.getWidgetMarker() );
	}
	
	public boolean isMarkerAtEnd(ElementMarker m)
	{
		return getWidget().isMarkerAtEnd( m.getWidgetMarker() );
	}
}