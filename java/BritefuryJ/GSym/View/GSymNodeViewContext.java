//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import org.python.core.PyObject;

import BritefuryJ.Cell.CellInterface;
import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPButton;
import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPEmpty;
import BritefuryJ.DocPresent.DPFraction;
import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPLineBreak;
import BritefuryJ.DocPresent.DPLink;
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPScript;
import BritefuryJ.DocPresent.DPSegment;
import BritefuryJ.DocPresent.DPSpan;
import BritefuryJ.DocPresent.DPStaticText;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWhitespace;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementContext;
import BritefuryJ.DocPresent.ElementFactory;
import BritefuryJ.DocPresent.ElementKeyboardListener;
import BritefuryJ.DocPresent.ElementLinearRepresentationListener;
import BritefuryJ.DocPresent.PyElementFactory;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.StyleSheets.ButtonStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.FractionStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.LinkStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.ScriptStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.StaticTextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;
import BritefuryJ.DocTree.DocTreeNode;
import BritefuryJ.DocView.DVNode;
import BritefuryJ.GSym.View.ListView.ListViewLayout;
import BritefuryJ.GSym.View.ListView.PySeparatorElementFactory;
import BritefuryJ.GSym.View.ListView.SeparatorElementFactory;
import BritefuryJ.Parser.ItemStream.ItemStream;

public class GSymNodeViewContext implements ElementContext, DVNode.NodeContext
{
	protected GSymViewContext viewInstance;
	protected DVNode viewNode;
	
	
	public GSymNodeViewContext(GSymViewContext viewInstance, DVNode viewNode)
	{
		this.viewInstance = viewInstance;
		this.viewNode = viewNode;
		this.viewNode.setContext( this, this );
	}
	
	
	
	
	
	
	private void registerViewNodeRelationship(DVNode childNode)
	{
		viewNode.registerChild( childNode );
	}
	
	
	
	public DPWidget border(Border border, ContainerStyleSheet styleSheet, DPWidget child)
	{
		viewInstance.getView().profile_startElement();
		DPBorder element = new DPBorder( border, styleSheet );
		element.setChild( child );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget border(Border border, DPWidget child)
	{
		return border( border, ContainerStyleSheet.defaultStyleSheet, child );
	}
	
	public DPWidget indent(double indentation, DPWidget child)
	{
		viewInstance.getView().profile_startElement();
		Border border = viewInstance.indentationBorder( indentation );
		DPBorder element = new DPBorder( border );
		element.setChild( child );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	
	public DPWidget text(TextStyleSheet styleSheet, String txt)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = new DPText( styleSheet, txt );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget textWithContent(TextStyleSheet styleSheet, String txt, String content)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = new DPText( styleSheet, txt, content );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget hiddenStructuralObject(Object structuralRepresentation)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = new DPEmpty();
		element.setStructuralValueObject( structuralRepresentation );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget hiddenStructuralSequence(List<Object> structuralRepresentation)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = new DPEmpty();
		element.setStructuralValueSequence( structuralRepresentation );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	
	public DPWidget hiddenStructuralStream(ItemStream structuralRepresentation)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = new DPEmpty();
		element.setStructuralValueStream( structuralRepresentation );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget hiddenText(String txt)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = new DPEmpty( txt );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget whitespace(String txt, float width)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = new DPWhitespace( txt, width );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}

	public DPWidget whitespace(String txt)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = new DPWhitespace( txt, 0.0 );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	
	
	public DPWidget staticText(StaticTextStyleSheet styleSheet, String txt)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = new DPStaticText( styleSheet, txt );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	

	public DPWidget link(LinkStyleSheet styleSheet, String txt, String targetLocation)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = new DPLink( styleSheet, txt, targetLocation );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget link(LinkStyleSheet styleSheet, String txt, DPLink.LinkListener listener)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = new DPLink( styleSheet, txt, listener );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget link(LinkStyleSheet styleSheet, String txt, PyObject listener)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = new DPLink( styleSheet, txt, listener );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	

	
	public DPWidget button(ButtonStyleSheet styleSheet, DPButton.ButtonListener listener, DPWidget child)
	{
		viewInstance.getView().profile_startElement();
		DPButton element = new DPButton( styleSheet, listener );
		element.setChild( child );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget button(ButtonStyleSheet styleSheet, PyObject listener, DPWidget child)
	{
		viewInstance.getView().profile_startElement();
		DPButton element = new DPButton( styleSheet, listener );
		element.setChild( child );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	


	public DPWidget hbox(HBoxStyleSheet styleSheet, List<DPWidget> children)
	{
		viewInstance.getView().profile_startElement();
		DPHBox element = new DPHBox( styleSheet );
		element.setChildren( children );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget ahbox(List<DPWidget> children)
	{
		return hbox( ahboxStyleSheet, children );
	}
	
	public DPWidget vbox(VBoxStyleSheet styleSheet, List<DPWidget> children)
	{
		viewInstance.getView().profile_startElement();
		DPVBox element = new DPVBox( styleSheet );
		element.setChildren( children );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget paragraph(ParagraphStyleSheet styleSheet, List<DPWidget> children)
	{
		viewInstance.getView().profile_startElement();
		DPParagraph element = new DPParagraph( styleSheet );
		element.setChildren( children );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget span(List<DPWidget> children)
	{
		viewInstance.getView().profile_startElement();
		DPSpan element = new DPSpan();
		element.setChildren( children );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget lineBreak(ContainerStyleSheet styleSheet, int lineBreakPriority, DPWidget child)
	{
		viewInstance.getView().profile_startElement();
		DPLineBreak element = new DPLineBreak( styleSheet, lineBreakPriority );
		if ( child != null )
		{
			element.setChild( child );
		}
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget lineBreak(int lineBreakPriority, DPWidget child)
	{
		return lineBreak( ContainerStyleSheet.defaultStyleSheet, lineBreakPriority, child );
	}
	
	public DPWidget lineBreak(ContainerStyleSheet styleSheet, int lineBreakPriority)
	{
		return lineBreak( styleSheet, lineBreakPriority, null );
	}
	
	public DPWidget lineBreak(int lineBreakPriority)
	{
		return lineBreak( ContainerStyleSheet.defaultStyleSheet, lineBreakPriority, null );
	}
	
	public DPWidget segment(TextStyleSheet textStyleSheet, boolean bGuardBegin, boolean bGuardEnd, DPWidget child)
	{
		viewInstance.getView().profile_startElement();
		DPSegment element = new DPSegment( textStyleSheet, bGuardBegin, bGuardEnd );
		element.setChild( child );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget segment(boolean bGuardBegin, boolean bGuardEnd, DPWidget child)
	{
		viewInstance.getView().profile_startElement();
		DPSegment element = new DPSegment( TextStyleSheet.defaultStyleSheet, bGuardBegin, bGuardEnd );
		element.setChild( child );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	
	public DPWidget script(ScriptStyleSheet styleSheet, TextStyleSheet segmentTextStyleSheet, DPWidget mainChild, DPWidget leftSuperChild, DPWidget leftSubChild, DPWidget rightSuperChild, DPWidget rightSubChild)
	{
		viewInstance.getView().profile_startElement();
		DPScript element = new DPScript( styleSheet, segmentTextStyleSheet );
		element.setMainChild( mainChild );
		if ( leftSuperChild != null )
		{
			element.setLeftSuperscriptChild( leftSuperChild );
		}
		if ( leftSubChild != null )
		{
			element.setLeftSubscriptChild( leftSubChild );
		}
		if ( rightSuperChild != null )
		{
			element.setRightSuperscriptChild( rightSuperChild );
		}
		if ( rightSubChild != null )
		{
			element.setRightSubscriptChild( rightSubChild );
		}
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget script(ScriptStyleSheet styleSheet, DPWidget mainChild, DPWidget leftSuperChild, DPWidget leftSubChild, DPWidget rightSuperChild, DPWidget rightSubChild)
	{
		return script( styleSheet, TextStyleSheet.defaultStyleSheet, mainChild, leftSuperChild, leftSubChild, rightSuperChild, rightSubChild );
	}
	
	
	public DPWidget scriptLSuper(ScriptStyleSheet styleSheet, TextStyleSheet segmentTextStyleSheet, DPWidget mainChild, DPWidget scriptChild)
	{
		return script( styleSheet, segmentTextStyleSheet, mainChild, scriptChild, null, null, null );
	}
	
	public DPWidget scriptLSub(ScriptStyleSheet styleSheet, TextStyleSheet segmentTextStyleSheet, DPWidget mainChild, DPWidget scriptChild)
	{
		return script( styleSheet, segmentTextStyleSheet, mainChild, null, scriptChild, null, null );
	}
	
	public DPWidget scriptRSuper(ScriptStyleSheet styleSheet, TextStyleSheet segmentTextStyleSheet, DPWidget mainChild, DPWidget scriptChild)
	{
		return script( styleSheet, segmentTextStyleSheet, mainChild, null, null, scriptChild, null );
	}
	
	public DPWidget scriptRSub(ScriptStyleSheet styleSheet, TextStyleSheet segmentTextStyleSheet, DPWidget mainChild, DPWidget scriptChild)
	{
		return script( styleSheet, segmentTextStyleSheet, mainChild, null, null, null, scriptChild );
	}
	
	
	public DPWidget scriptLSuper(ScriptStyleSheet styleSheet, DPWidget mainChild, DPWidget scriptChild)
	{
		return script( styleSheet, mainChild, scriptChild, null, null, null );
	}
	
	public DPWidget scriptLSub(ScriptStyleSheet styleSheet, DPWidget mainChild, DPWidget scriptChild)
	{
		return script( styleSheet, mainChild, null, scriptChild, null, null );
	}
	
	public DPWidget scriptRSuper(ScriptStyleSheet styleSheet, DPWidget mainChild, DPWidget scriptChild)
	{
		return script( styleSheet, mainChild, null, null, scriptChild, null );
	}
	
	public DPWidget scriptRSub(ScriptStyleSheet styleSheet, DPWidget mainChild, DPWidget scriptChild)
	{
		return script( styleSheet, mainChild, null, null, null, scriptChild );
	}
	
	
	
	public DPWidget fraction(FractionStyleSheet styleSheet, TextStyleSheet segmentTextStyleSheet, DPWidget numerator, DPWidget denominator, String barContent)
	{
		viewInstance.getView().profile_startElement();
		DPFraction element = new DPFraction( styleSheet, segmentTextStyleSheet, barContent );
		element.setNumeratorChild( numerator );
		element.setDenominatorChild( denominator );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget fraction(FractionStyleSheet styleSheet, DPWidget numerator, DPWidget denominator, String barContent)
	{
		return fraction( styleSheet, TextStyleSheet.defaultStyleSheet, numerator, denominator, barContent );
	}
	

	public DPWidget listView(ListViewLayout layout, ElementFactory beginDelim, ElementFactory endDelim, SeparatorElementFactory separator, List<DPWidget> children)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = layout.createListElement( children, beginDelim, endDelim, separator );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	public DPWidget listView(ListViewLayout layout, PyObject beginDelim, PyObject endDelim, PyObject separator, List<DPWidget> children)
	{
		viewInstance.getView().profile_startElement();
		DPWidget element = layout.createListElement( children, PyElementFactory.pyToElementFactory( beginDelim ), PyElementFactory.pyToElementFactory( endDelim ), PySeparatorElementFactory.pyToSeparatorElementFactory( separator ) );
		element.setContext( this );
		viewInstance.getView().profile_stopElement();
		return element;
	}
	
	
	
	public DPWidget linearRepresentationListener(DPWidget child, ElementLinearRepresentationListener listener)
	{
		viewInstance.getView().profile_startElement();
		child.setLinearRepresentationListener( listener );
		viewInstance.getView().profile_stopElement();
		return child;
	}
	
	public List<DPWidget> linearRepresentationListener(List<DPWidget> children, ElementLinearRepresentationListener listener)
	{
		viewInstance.getView().profile_startElement();
		for (DPWidget child: children)
		{
			child.setLinearRepresentationListener( listener );
		}
		viewInstance.getView().profile_stopElement();
		return children;
	}
	
	
	public DPWidget keyboardListener(DPWidget child, ElementKeyboardListener listener)
	{
		viewInstance.getView().profile_startElement();
		child.setKeyboardListener( listener );
		viewInstance.getView().profile_stopElement();
		return child;
	}
	
	public List<DPWidget> keyboardListener(List<DPWidget> children, ElementKeyboardListener listener)
	{
		viewInstance.getView().profile_startElement();
		for (DPWidget child: children)
		{
			child.setKeyboardListener( listener );
		}
		viewInstance.getView().profile_stopElement();
		return children;
	}
	
	
	public DPWidget viewEval(DocTreeNode x)
	{
		return viewEvalFn( x, (GSymNodeViewFunction)null, null );
	}

	public DPWidget viewEval(DocTreeNode x, Object state)
	{
		return viewEvalFn( x, (GSymNodeViewFunction)null, state );
	}

	public DPWidget viewEvalFn(DocTreeNode x, GSymNodeViewFunction nodeViewFunction)
	{
		return viewEvalFn( x, nodeViewFunction, null );
	}

	public DPWidget viewEvalFn(DocTreeNode x, GSymNodeViewFunction nodeViewFunction, Object state)
	{
		viewInstance.getView().profile_startJava();
		
		if ( x == null )
		{
			throw new RuntimeException( "GSymNodeViewInstance.viewEvanFn(): cannot build view of null node" );
		}
		
		// A call to DocNode.buildNodeView builds the view, and puts it in the DocView's table
		DVNode viewNode = viewInstance.getView().buildNodeView( x, viewInstance.makeNodeElementFactory( nodeViewFunction, state ) );
		
		
		// Block access tracking to prevent the contents of this node being dependent upon the child node being refreshed,
		// and refresh the view node
		// Refreshing the child node will ensure that when its contents are inserted into outer elements, its full element tree
		// is up to date and available.
		// Blocking the access tracking prevents an inner node from causing all parent/grandparent/etc nodes from requiring a
		// refresh.
		WeakHashMap<CellInterface, Object> accessList = CellInterface.blockAccessTracking();
		viewNode.refresh();
		CellInterface.unblockAccessTracking( accessList );
		
		registerViewNodeRelationship( viewNode );
		
		viewInstance.getView().profile_stopJava();
		return viewNode.getElementNoRefresh();
	}
	
	public DPWidget viewEvalFn(DocTreeNode x, PyObject nodeViewFunction)
	{
		return viewEvalFn( x, new PyGSymNodeViewFunction( nodeViewFunction ), null );
	}

	public DPWidget viewEvalFn(DocTreeNode x, PyObject nodeViewFunction, Object state)
	{
		return viewEvalFn( x, new PyGSymNodeViewFunction( nodeViewFunction ), state );
	}
	
	
	
	
	public List<DPWidget> mapViewEval(List<DocTreeNode> xs)
	{
		return mapViewEvalFn( xs, (GSymNodeViewFunction)null, null );
	}

	public List<DPWidget> mapViewEval(List<DocTreeNode> xs, Object state)
	{
		return mapViewEvalFn( xs, (GSymNodeViewFunction)null, state );
	}

	public List<DPWidget> mapViewEvalFn(List<DocTreeNode> xs, GSymNodeViewFunction nodeViewFunction)
	{
		return mapViewEvalFn( xs, nodeViewFunction, null );
	}

	public List<DPWidget> mapViewEvalFn(List<DocTreeNode> xs, GSymNodeViewFunction nodeViewFunction, Object state)
	{
		ArrayList<DPWidget> children = new ArrayList<DPWidget>();
		children.ensureCapacity( xs.size() );
		for (DocTreeNode x: xs)
		{
			children.add( viewEvalFn( x, nodeViewFunction, state ) );
		}
		return children;
	}
	
	public List<DPWidget> mapViewEvalFn(List<DocTreeNode> xs, PyObject nodeViewFunction)
	{
		return mapViewEvalFn( xs, new PyGSymNodeViewFunction( nodeViewFunction ), null );
	}

	public List<DPWidget> mapViewEvalFn(List<DocTreeNode> xs, PyObject nodeViewFunction, Object state)
	{
		return mapViewEvalFn( xs, new PyGSymNodeViewFunction( nodeViewFunction ), state );
	}
	
	
	
	public DocTreeNode getTreeNode()
	{
		return viewNode.getTreeNode();
	}
	
	public Object getDocNode()
	{
		return viewNode.getDocNode();
	}
	
	
	
	public DPWidget getViewNodeElement()
	{
		return viewNode.getElementNoRefresh();
	}
	
	public DPWidget getViewNodeContentElement()
	{
		return viewNode.getInnerElementNoRefresh();
	}
	
	
	
	private GSymNodeViewContext getPreviousSiblingFromChildElement(GSymNodeViewContext parent, DPWidget fromChild)
	{
		if ( fromChild == null )
		{
			return null;
		}
		DPContainer parentElement = fromChild.getParent();
		if ( parentElement == parent.getViewNodeElement() )
		{
			return null;
		}
		
		List<DPWidget> children = parentElement.getChildren();
		int index = children.indexOf( fromChild );
		for (int i = index - 1; i >= 0; i--)
		{
			GSymNodeViewContext sibling = getLastChildFromParentElement( parent, children.get( i ) );
			if ( sibling != null )
			{
				return sibling;
			}
		}
		
		return getNextSiblingFromChildElement( parent, parentElement.getParent() );
	}
	
	private GSymNodeViewContext getLastChildFromParentElement(GSymNodeViewContext parent, DPWidget element)
	{
		if ( element.getContext() != parent )
		{
			// We have recursed down the element tree far enough when we encounter an element with a different context
			return (GSymNodeViewContext)element.getContext();
		}
		else if ( element instanceof DPContainer )
		{
			DPContainer branch = (DPContainer)element;
			List<DPWidget> children = branch.getChildren();
			for (int i = children.size() - 1; i >= 0; i--)
			{
				GSymNodeViewContext sibling = getLastChildFromParentElement( parent, children.get( i ) );
				if ( sibling != null )
				{
					return sibling;
				}
			}
		}
		return null;
	}
	

	
	private GSymNodeViewContext getNextSiblingFromChildElement(GSymNodeViewContext parent, DPWidget fromChild)
	{
		if ( fromChild == null )
		{
			return null;
		}
		DPContainer parentElement = fromChild.getParent();
		if ( parentElement == parent.getViewNodeElement() )
		{
			return null;
		}
		
		List<DPWidget> children = parentElement.getChildren();
		int index = children.indexOf( fromChild );
		for (int i = index + 1; i < children.size(); i++)
		{
			GSymNodeViewContext sibling = getFirstChildFromParentElement( parent, children.get( i ) );
			if ( sibling != null )
			{
				return sibling;
			}
		}
		
		return getNextSiblingFromChildElement( parent, parentElement.getParent() );
	}
	
	private GSymNodeViewContext getFirstChildFromParentElement(GSymNodeViewContext parent, DPWidget element)
	{
		if ( element.getContext() != parent )
		{
			// We have recursed down the element tree far enough when we encounter an element with a different context
			return (GSymNodeViewContext)element.getContext();
		}
		else if ( element instanceof DPContainer )
		{
			DPContainer branch = (DPContainer)element;
			for (DPWidget child: branch.getChildren())
			{
				GSymNodeViewContext sibling = getFirstChildFromParentElement( parent, child );
				if ( sibling != null )
				{
					return sibling;
				}
			}
		}
		return null;
	}
	

	
	public GSymNodeViewContext getParent()
	{
		DVNode parentViewNode = viewNode.getParent();
		return parentViewNode != null  ?  (GSymNodeViewContext)parentViewNode.getContext()  :  null;
	}
	

	public GSymNodeViewContext getPrevSibling()
	{
		return getPreviousSiblingFromChildElement( getParent(), getViewNodeElement() );
	}
	
	public GSymNodeViewContext getNextSibling()
	{
		return getNextSiblingFromChildElement( this, getViewNodeElement() );
	}
	
	
	
	public GSymNodeViewContext getFirstChild()
	{
		return getFirstChildFromParentElement( getParent(), getViewNodeElement() );
	}
	
	public GSymNodeViewContext getLastChild()
	{
		return getNextSiblingFromChildElement( this, getViewNodeElement() );
	}
	
	
	
	public ArrayList<GSymNodeViewContext> getNodeViewInstancePathFromRoot()
	{
		ArrayList<GSymNodeViewContext> path = new ArrayList<GSymNodeViewContext>();
		
		GSymNodeViewContext n = this;
		while ( n != null )
		{
			path.add( 0, n );
			n = n.getParent();
		}
		
		return path;
	}
	
	public ArrayList<GSymNodeViewContext> getNodeViewInstancePathFromSubtreeRoot(GSymNodeViewContext root)
	{
		ArrayList<GSymNodeViewContext> path = new ArrayList<GSymNodeViewContext>();
		
		GSymNodeViewContext n = this;
		while ( n != null )
		{
			path.add( 0, n );
			if ( n == root )
			{
				return path;
			}
			n = n.getParent();
		}

		return null;
	}
	
	
	public GSymViewContext getViewContext()
	{
		return viewInstance;
	}
	

	
	private static HBoxStyleSheet ahboxStyleSheet = new HBoxStyleSheet( 0.0 ); 
}