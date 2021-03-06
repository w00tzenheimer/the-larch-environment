//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

/*
 * Layout spaces:
 * 
 * Local space - local to element
 * Allocation space - space in which children are positioned
 * 
 * 
 * Transformations:
 * Child local space to allocation space - the position allocated to the child by this element
 * Allocation space to local space - normally identity, but the viewport transformation for viewport elements
 * Child local space to local space - the concatenation of child local space to allocation space, and allocation space to local space
 */

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.DefaultPerspective;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.Graphics.FilledBorder;
import BritefuryJ.Graphics.FilledOutlinePainter;
import BritefuryJ.Graphics.Painter;
import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.IncrementalView.ViewFragmentFunction;
import BritefuryJ.LSpace.Event.AbstractPointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Event.PointerEvent;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.LSpace.Input.*;
import BritefuryJ.LSpace.Interactor.*;
import BritefuryJ.LSpace.Layout.*;
import BritefuryJ.LSpace.LayoutTree.ArrangedSequenceLayoutNode;
import BritefuryJ.LSpace.LayoutTree.LayoutNode;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.StyleParams.ElementStyleParams;
import BritefuryJ.LSpace.TextFocus.Caret;
import BritefuryJ.LSpace.Util.TextVisual;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;
import BritefuryJ.ObjectPresentation.PresentationStateListenerList;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.ObjectPres.HorizontalField;
import BritefuryJ.Pres.ObjectPres.ObjectBoxWithFields;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.*;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Projection.Perspective;
import BritefuryJ.Shortcut.Shortcut;
import BritefuryJ.Shortcut.ShortcutElementAction;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.Util.RichString.RichString;
import BritefuryJ.Util.RichString.RichStringBuilder;

import java.awt.*;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;


abstract public class LSElement implements Presentable
{
	protected static double NON_TYPESET_CHILD_BASELINE_OFFSET = -5.0;
	
	
	
	//
	//
	// FILTERS
	//
	//
	
	public static class SubtreeElementFilter implements ElementFilter
	{
		private LSElement subtreeRoot;
		
		
		public SubtreeElementFilter(LSElement subtreeRoot)
		{
			this.subtreeRoot = subtreeRoot;
		}


		@Override
		public boolean testElement(LSElement element)
		{
			return element.isInSubtreeRootedAt( subtreeRoot );
		}
	}

	public static class PropertyElementFilter implements ElementFilter {
		private Object propertyKey;


		public PropertyElementFilter(Object propertyKey) {
			this.propertyKey = propertyKey;
		}


		@Override
		public boolean testElement(LSElement element)
		{
			return element.getProperty(propertyKey) != null;
		}
	}
	
	
	
	//
	//
	// TREE TRAVERSAL FUNCTIONS
	//
	//
	
	public static final TreeTraversal.BranchChildrenFn internalBranchChildrenFn = new TreeTraversal.BranchChildrenFn()
	{
		@Override
		public List<LSElement> getChildrenOf(LSContainer element)
		{
			return element.getInternalChildren();
		}
	};

	public static final TreeTraversal.BranchChildrenFn branchChildrenFn = new TreeTraversal.BranchChildrenFn()
	{
		@Override
		public List<LSElement> getChildrenOf(LSContainer element)
		{
			return element.getChildren();
		}
	};





	//
	//
	// PRESENTABLE
	//
	//
	
	private static class TreeExplorerViewFragmentFn implements ViewFragmentFunction
	{
		public Pres createViewFragment(Object x, FragmentView ctx, SimpleAttributeTable inheritedState)
		{
			LSElement element = (LSElement)x;
			return element.exploreTreePresent( ctx, inheritedState );
		}
	}
	
	protected static Perspective treeExplorerPerspective = new Perspective( new TreeExplorerViewFragmentFn() );
	
	public static class ElementTreeExplorer implements Presentable
	{
		private LSElement element;
		
		
		protected ElementTreeExplorer(LSElement element)
		{
			this.element = element;
		}
		
		
		public LSElement getElement()
		{
			return element;
		}


		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return treeExplorerPerspective.applyTo( new InnerFragment( element ) ); 
		}
	}

	

	
	
	//
	//
	// EXCEPTIONS
	//
	//
	
	public static class IsNotInSubtreeException extends RuntimeException
	{
		static final long serialVersionUID = 0L;
	}
	
	
	public static class ChildHasNoLayoutException extends RuntimeException
	{
		static final long serialVersionUID = 0L;
	}
	
	
	public static class DndDisabledException extends RuntimeException
	{
		static final long serialVersionUID = 0L;
	}

	
	public static class DndOperationAlreadyInList extends RuntimeException
	{
		static final long serialVersionUID = 0L;
	}

	
	public static class DndOperationNotInList extends RuntimeException
	{
		static final long serialVersionUID = 0L;
	}
	
	
	
	//
	//
	// TREE EVENTS
	//
	//
	
	private static class TreeEvent
	{
		private LSElement sourceElement;
		private Object value;
		
		
		public TreeEvent(LSElement sourceElement, Object value)
		{
			this.sourceElement = sourceElement;
			this.value = value;
		}
	}
	
	//
	//
	// INTERACTION FIELDS
	//
	//
	
	private static class InteractionFields
	{
		private ObjectDndHandler dndHandler;
		
		private ArrayList<AbstractElementInteractor> elementInteractors;
		private ArrayList<ElementPainter> painters;
		private ArrayList<TreeEventListener> treeEventListeners;
		
		
		
		public InteractionFields()
		{
		}
		
		
		public void addElementInteractor(AbstractElementInteractor interactor)
		{
			elementInteractors = addElementToList( elementInteractors, interactor );
		}
		
		public void removeElementInteractor(AbstractElementInteractor interactor)
		{
			elementInteractors = removeElementFromList( elementInteractors, interactor );
		}
		
		
		
		public void addPainter(ElementPainter painter)
		{
			painters = addElementToList( painters, painter );
		}
		
		public void removePainter(ElementPainter painter)
		{
			painters = removeElementFromList( painters, painter );
		}
		
		
		
		public void addTreeEventListener(TreeEventListener listener)
		{
			treeEventListeners = addElementToList( treeEventListeners, listener );
		}
		
		public void removeTreeEventListener(TreeEventListener listener)
		{
			treeEventListeners = removeElementFromList( treeEventListeners, listener );
		}
		
		
		
		
		
		public boolean isIdentity()
		{
			return dndHandler == null  &&  treeEventListeners == null  &&  elementInteractors == null  &&  painters == null;
		}
		
		
		private static <T> ArrayList<T> addElementToList(ArrayList<T> ls, T x)
		{
			if ( ls == null )
			{
				ls = new ArrayList<T>();
			}
			ls.add( x );
			return ls;
		}
		
		private static <T> ArrayList<T> removeElementFromList(ArrayList<T> ls, T x)
		{
			if ( ls != null )
			{
				ls.remove( x );
				if ( ls.isEmpty() )
				{
					ls = null;
				}
			}
			return ls;
		}
	}
	
	
	
	
	public static class PropertyValue
	{
		private LSElement element;
		private Object key;
		private Object value;
		
		
		protected PropertyValue(LSElement element, Object key, Object value)
		{
			this.element = element;
			this.key = key;
			this.value = value;
		}
		
		
		public LSElement getElement()
		{
			return element;
		}
		
		public Object getKey()
		{
			return key;
		}
		
		public Object getValue()
		{
			return value;
		}
	}

	
	
	
	//
	//
	// FLAGS
	//
	//
	
	protected final static int FLAG_REALISED = 0x1;
	protected final static int FLAG_RESIZE_QUEUED = 0x2;
	protected final static int FLAG_ALLOCATION_UP_TO_DATE = 0x4;
	protected final static int FLAG_CARET_GRABBED = 0x8;
	protected final static int FLAG_HOVER = 0x10;
	protected final static int FLAG_ENSURE_VISIBLE_QUEUED = 0x20;
	protected final static int FLAG_HAS_FIXED_VALUE = 0x40;
	protected final static int FLAG_HAS_CACHED_VALUES = 0x80;
	protected final static int FLAG_HAS_WAITING_IMMEDIATE_EVENTS = 0x100;

	protected final static int _ALIGN_SHIFT = 9;
	protected final static int _ALIGN_MASK = ElementAlignment._ELEMENTALIGN_MASK  <<  _ALIGN_SHIFT;
	protected final static int _HALIGN_MASK = ElementAlignment._HALIGN_MASK  <<  _ALIGN_SHIFT;
	protected final static int _VALIGN_MASK = ElementAlignment._VALIGN_MASK  <<  _ALIGN_SHIFT;
	protected final static int FLAGS_ELEMENT_END = ElementAlignment._ELEMENTALIGN_END  <<  _ALIGN_SHIFT;
	
	
	//
	//
	// FIELDS
	//
	//
	
	protected int flags;
	
	protected ElementStyleParams styleParams;
	protected LSContainer parent;
	protected LSRootElement rootElement;
	
	protected LayoutNode layoutNode;
	
	private InteractionFields interactionFields;

	protected PresentationStateListenerList debugPresStateListeners = null;
	protected String debugName;
	
	protected ElementValueFunction valueFn;
	protected Object fixedValue;
	
	private HashMap<Object, PropertyValue> properties = null;

	//
	//
	// FIELDS AS DICTIONARY VALUES
	//
	//
	
	// These fields would be null/non-existant for the vast majority of elements, so store them in a global dictionary to
	// save space
	
	private static WeakHashMap<LSElement, ArrayList<Runnable>> waitingImmediateEventsByElement = new WeakHashMap<LSElement, ArrayList<Runnable>>();
	
	
	
	

	//
	//
	// METHODS
	//
	//
	
	
	//
	//
	// Constructors
	//
	//
	
	public LSElement()
	{
		this( ElementStyleParams.defaultStyleParams );
	}
	
	public LSElement(ElementStyleParams styleParams)
	{
		flags = 0;
		this.styleParams = styleParams;
		setAlignmentFlags( ElementAlignment.flagValue( styleParams.getHAlignment(), styleParams.getVAlignment() ) );
	}
	
	
	
	//
	//
	// Context
	//
	//
	
	public FragmentContext getFragmentContext()
	{
		LSElement w = this;
		while ( w != null )
		{
			FragmentContext c = w.getContextOfFragment();
			if ( c != null )
			{
				return c;
			}
			
			w = w.getParent();
		}
		
		return null;
	}
	
	// Override this in subclasses
	protected FragmentContext getContextOfFragment()
	{
		return null;
	}
	
	
	
	

	
	
	
	protected void setAlignmentFlags(int alignmentFlags)
	{
		flags = ( flags & ~_ALIGN_MASK )   |   ( alignmentFlags << _ALIGN_SHIFT );
	}
	
	protected void setHAlignmentFlags(int alignmentFlags)
	{
		flags = ( flags & ~_HALIGN_MASK )   |   ( alignmentFlags << _ALIGN_SHIFT );
	}
	
	protected void setVAlignmentFlags(int alignmentFlags)
	{
		flags = ( flags & ~_VALIGN_MASK )   |   ( alignmentFlags << _ALIGN_SHIFT );
	}
	
	public void setAlignment(HAlignment hAlign, VAlignment vAlign)
	{
		setAlignmentFlags( ElementAlignment.flagValue( hAlign, vAlign ) );
	}
	
	
	public int getAlignmentFlags()
	{
		return ( flags & _ALIGN_MASK )  >>  _ALIGN_SHIFT;
	}
	
	public HAlignment getHAlignment()
	{
		return ElementAlignment.getHAlignment( getAlignmentFlags() );
	}
	
	public VAlignment getVAlignment()
	{
		return ElementAlignment.getVAlignment( getAlignmentFlags() );
	}
	

	
	
	//
	// Layout wrap method - papers over the fact that some elements cannot have layout-less elements as children
	//
	
	public LSElement layoutWrap(HAlignment hAlign, VAlignment vAlign)
	{
		if ( getLayoutNode() != null )
		{
			return this;
		}
		else
		{
			LSRow row = new LSRow( new LSElement[] { this } );
			row.setAlignment( hAlign, vAlign );
			return row;
		}
	}



    public int computeLineBreakCost() {
        return -1;
    }
	
	

	
	
	//
	// Geometry methods
	//
	
	public Point2 getPositionInParentSpace()
	{
		return layoutNode != null  ?  layoutNode.getPositionInParentSpace()  :  new Point2();
	}
	
	public double getPositionInParentSpaceX()
	{
		return layoutNode != null  ?  layoutNode.getAllocPositionInParentSpaceX()  :  0.0;
	}
	
	public double getPositionInParentSpaceY()
	{
		return layoutNode != null  ?  layoutNode.getAllocPositionInParentSpaceY()  :  0.0;
	}
	
	public Point2 getPositionInParentAllocationSpace()
	{
		return layoutNode != null  ?  layoutNode.getPositionInParentAllocationSpace()  :  new Point2();
	}
	
	public double getActualWidth()
	{
		return layoutNode != null  ?  layoutNode.getActualWidth()  :  ( parent != null  ?  parent.getActualWidth()  :  0.0 );
	}
	
	public double getActualHeight()
	{
		return layoutNode != null  ?  layoutNode.getActualHeight()  :  ( parent != null  ?  parent.getActualHeight()  :  0.0 );
	}
	
	public Vector2 getActualSize()
	{
		return layoutNode != null  ?  layoutNode.getActualSize()  :  ( parent != null  ?  parent.getActualSize()  :  new Vector2() );
	}
	
	public double getActualWidthInParentSpace()
	{
		return layoutNode != null  ?  layoutNode.getActualWidthInParentSpace()  :  ( parent != null  ?  parent.getActualWidth()  :  0.0 );
	}
	
	public double getActualHeightInParentSpace()
	{
		return layoutNode != null  ?  layoutNode.getActualHeightInParentSpace()  :  ( parent != null  ?  parent.getActualHeight()  :  0.0 );
	}
	
	public Vector2 getActualSizeInParentSpace()
	{
		return layoutNode != null  ?  layoutNode.getActualSizeInParentSpace()  :  ( parent != null  ?  parent.getActualSize()  :  new Vector2());
	}
	
	protected AABox2 getVisibleBoxInLocalSpace()
	{
		return getLocalAABox();
	}
	
	public AABox2 getVisibleBoxRelativeToAncestor(LSElement ancestor)
	{
		Xform2 toElementXform = getLocalToAncestorXform( ancestor );
		return toElementXform.transform( getVisibleBoxInLocalSpace() );
	}
	
	public AABox2 getVisibleBoxRelativeToRoot()
	{
		return getVisibleBoxRelativeToAncestor( rootElement );
	}
	
	public AABox2 getVisibleBoxRelativeToScreen()
	{
		Point screenRoot = rootElement.getComponent().getLocationOnScreen();
		return getVisibleBoxRelativeToAncestor( rootElement ).offset( new Vector2( screenRoot.x, screenRoot.y ) );
	}
	

	public double getAllocWidth()
	{
		return layoutNode != null  ?  layoutNode.getAllocWidth()  :  ( parent != null  ?  parent.getAllocWidth()  :  0.0 );
	}
	
	public double getAllocHeight()
	{
		return layoutNode != null  ?  layoutNode.getAllocHeight()  :  ( parent != null  ?  parent.getAllocHeight()  :  0.0 );
	}
	
	public LAllocV getAllocV()
	{
		return layoutNode != null  ?  layoutNode.getAllocV()  :  ( parent != null  ?  parent.getAllocV()  :  new LAllocV( 0.0 ) );
	}
	
	public Vector2 getAllocSize()
	{
		return layoutNode != null  ?  layoutNode.getAllocSize()  :  ( parent != null  ?  parent.getAllocSize()  :  new Vector2() );
	}

	
	
	public AABox2 getLocalAABox()
	{
		return new AABox2( new Point2(), getActualSize() );
	}
	
	public AABox2 getAABoxRelativeToAncestor(LSElement ancestor)
	{
		Xform2 toElementXform = getLocalToAncestorXform( ancestor );
		return toElementXform.transform( getLocalAABox() );
	}
	
	public AABox2 getAABoxRelativeToRoot()
	{
		return getAABoxRelativeToAncestor( rootElement );
	}
	
	public AABox2 getAABoxRelativeToScreen()
	{
		Point screenRoot = rootElement.getComponent().getLocationOnScreen();
		return getAABoxRelativeToAncestor( rootElement ).offset( new Vector2( screenRoot.x, screenRoot.y ) );
	}
	
	
	public AABox2 getLocalVisibleBoundsClipBox()
	{
		return null;
	}
	
	public Point2 getLocalAnchor(Anchor a)
	{
		Vector2 s = getActualSize();
		return new Point2( s.x * a.getPropX(), s.y * a.getPropY() );
	}
	
	public AABox2 getAABoxInParentSpace()
	{
		return new AABox2( getPositionInParentSpace(), getActualSizeInParentSpace() );
	}
	
	
	public Shape[] getShapes()
	{
		Vector2 size = getActualSize();
		return new Shape[] { new Rectangle2D.Double( 0.0, 0.0, size.x, size.y ) };
	}
	

	
	public Xform2 getLocalToParentAllocationSpaceXform()
	{
		return parent != null  ?  parent.getAllocationSpaceToLocalSpaceXform( this )  :  Xform2.identity;
	}
	
	public Xform2 getLocalToParentXform()
	{
		return getLocalToParentAllocationSpaceXform().concat( new Xform2( getPositionInParentAllocationSpace().toVector2() ) );
	}
	
	public Xform2 getParentToLocalXform()
	{
		return getLocalToParentXform().inverse();
	}
	
	
	
	public AffineTransform getLocalToParentAffineTransform()
	{
		return getLocalToParentXform().toAffineTransform();
	}

	public AffineTransform getParentToLocalAffineTransform()
	{
		return getParentToLocalXform().toAffineTransform();
	}
	
	
	
	public Xform2 getLocalToRootXform(Xform2 x)
	{
		return getLocalToAncestorXform( null, x );
	}
	
	public Xform2 getLocalToRootXform()
	{
		return getLocalToRootXform( new Xform2() );
	}
	
	
	
	public Xform2 getRootToLocalXform(Xform2 x)
	{
		return getAncestorToLocalXform( null, x );
	}
	
	public Xform2 getRootToLocalXform()
	{
		return getRootToLocalXform( new Xform2() );
	}
	
	
	
	public Xform2 getLocalToAncestorXform(LSElement ancestor, Xform2 x)
	{
		LSElement node = this;
		
		while ( node != ancestor )
		{
			LSElement parentNode = node.parent;
			if ( parentNode != null )
			{
				x = x.concat( node.getLocalToParentXform() );
				node = parentNode;
			}
			else
			{
				if ( ancestor != null )
				{
					// Did not reach ancestor
					throw new IsNotInSubtreeException();
				}
				else
				{
					return x;
				}
			}
		}
		
		return x;
	}
	
	public Xform2 getLocalToAncestorXform(LSElement ancestor)
	{
		if ( ancestor == parent )
		{
			// Early out
			return getLocalToParentXform();
		}
		else
		{
			return getLocalToAncestorXform( ancestor, new Xform2() );
		}
	}
	
	
	
	public Xform2 getAncestorToLocalXform(LSElement ancestor, Xform2 x)
	{
		LSElement node = this;
		
		while ( node != ancestor )
		{
			LSElement parentNode = node.parent;
			if ( parentNode != null )
			{
				x = node.getParentToLocalXform().concat( x );
				node = parentNode;
			}
			else
			{
				if ( ancestor != null )
				{
					// Did not reach ancestor
					throw new IsNotInSubtreeException();
				}
				else
				{
					return x;
				}
			}
		}
		
		return x;
	}
	
	public Xform2 getAncestorToLocalXform(LSElement ancestor)
	{
		if ( ancestor == parent )
		{
			// Early out
			return getParentToLocalXform();
		}
		else
		{
			return getAncestorToLocalXform( ancestor, new Xform2() );
		}
	}
	
	
	
	public Xform2 getTransformRelativeTo(LSElement toElement, Xform2 x)
	{
		Xform2 myXform = getLocalToRootXform();
		Xform2 toElementXform = toElement.getLocalToRootXform();
		return myXform.concat( toElementXform.inverse() );
	}
	
	
	public Point2 getLocalPointRelativeToRoot(Point2 p)
	{
		return getLocalPointRelativeToAncestor( null, p );
	}
	
	public Point2 getLocalPointRelativeToScreen(Point2 p)
	{
		Point2 pInRoot = getLocalPointRelativeToRoot( p );
		Point screenRoot = rootElement.getComponent().getLocationOnScreen();
		return new Point2( pInRoot.x + screenRoot.x, pInRoot.y + screenRoot.y );
	}
	
	public Point2 getLocalPointRelativeToAncestor(LSElement ancestor, Point2 p)
	{
		LSElement node = this;
		
		while ( node != ancestor )
		{
			LSElement parentNode = node.parent;
			if ( parentNode != null )
			{
				p = node.getLocalToParentXform().transform( p );
				node = parentNode;
			}
			else
			{
				if ( ancestor != null )
				{
					// Did not reach ancestor
					throw new IsNotInSubtreeException();
				}
				else
				{
					return p;
				}
			}
		}
		
		return p;
	}
	
	public Point2 getLocalPointRelativeTo(LSElement toElement, Point2 p)
	{
		Point2 pointInRoot = getLocalPointRelativeToRoot( p );
		Xform2 toElementXform = toElement.getLocalToRootXform();
		return toElementXform.inverse().transform( pointInRoot );
	}
	
	
	
	public PointerEvent transformParentToLocalEvent(PointerEvent event)
	{
		return event.transformed( getParentToLocalXform() );
	}
	
	public PointerInterface transformParentToLocalPointer(PointerInterface pointer)
	{
		return pointer.transformed( getParentToLocalXform() );
	}
	
	public Point2 transformParentToLocalPoint(Point2 parentPos)
	{
		return getParentToLocalXform().transform( parentPos );
	}
	
	public boolean containsParentSpacePoint(Point2 parentPos)
	{
		return getAABoxInParentSpace().containsPoint( parentPos );
	}

	public boolean containsLocalSpacePoint(Point2 localPos)
	{
		return getLocalAABox().containsPoint( localPos );
	}
	

	
	
	public AffineTransform pushLocalToRootGraphicsTransform(Graphics2D graphics)
	{
		AffineTransform current = graphics.getTransform();
		getLocalToRootXform().apply( graphics );
		return current;
	}
	
	public void popGraphicsTransform(Graphics2D graphics, AffineTransform x)
	{
		graphics.setTransform( x );
	}
	
	
	
	protected void ensureRegionVisible(AABox2 box)
	{
		if ( parent != null )
		{
			parent.ensureRegionVisible( getLocalToParentXform().transform( box ) );
		}
	}
	
	protected boolean isLocalSpacePointVisible(Point2 point)
	{
		if ( parent != null )
		{
			return parent.isLocalSpacePointVisible( getLocalToParentXform().transform( point ) );
		}
		else
		{
			return true;
		}
	}
	
	public void ensureVisible()
	{
		if ( isRealised() )
		{
			ensureRegionVisible( getLocalAABox() );
		}
		else
		{
			setFlag( FLAG_ENSURE_VISIBLE_QUEUED );
		}
	}
	
	
	
	

	//
	//
	// Flag methods
	//
	//
	
	protected void clearFlag(int flag)
	{
		flags &= ~flag;
	}
	
	protected void setFlag(int flag)
	{
		flags |= flag;
	}
	
	protected void setFlagValue(int flag, boolean value)
	{
		if ( value )
		{
			flags |= flag;
		}
		else
		{
			flags &= ~flag;
		}
	}
	
	protected boolean testFlag(int flag)
	{
		return ( flags & flag )  !=  0;
	}
	
	
	protected void setFlagMaskValue(int mask, int value)
	{
		flags = ( flags & ~mask )  |  ( value & mask );
	}
	
	protected int getFlagMaskValue(int mask)
	{
		return flags & mask;
	}
	
	
	protected void clearFlagRealised()
	{
		clearFlag( FLAG_REALISED );
	}
	
	protected void setFlagRealised()
	{
		setFlag( FLAG_REALISED );
	}
	
	
	public boolean isResizeQueued()
	{
		return testFlag( FLAG_RESIZE_QUEUED );
	}
	
	public void clearFlagResizeQueued()
	{
		clearFlag( FLAG_RESIZE_QUEUED );
	}
	
	public void setFlagResizeQueued()
	{
		setFlag( FLAG_RESIZE_QUEUED );
	}
	
	
	public boolean isAllocationUpToDate()
	{
		return testFlag( FLAG_ALLOCATION_UP_TO_DATE );
	}
	
	public void clearFlagAllocationUpToDate()
	{
		clearFlag( FLAG_ALLOCATION_UP_TO_DATE );
	}
	
	public void setFlagAllocationUpToDate()
	{
		setFlag( FLAG_ALLOCATION_UP_TO_DATE );
	}
	
	

	
	
	//
	//
	// Pointers within bounds
	//
	//
	
	protected boolean arePointersWithinBounds()
	{
		if ( rootElement != null )
		{
			return rootElement.getInputTable().arePointersWithinBoundsOfElement( this );
		}
		else
		{
			return false;
		}
	}
	
	protected ArrayList<PointerInterface> getPointersWithinBounds()
	{
		if ( rootElement != null )
		{
			return rootElement.getInputTable().getPointersWithinBoundsOfElement( this );
		}
		else
		{
			return null;
		}
	}
	
	
	
	
	//
	//
	// Tree structure methods
	//
	//
	
	public boolean isRealised()
	{
		return testFlag( FLAG_REALISED );
	}
	
	public LSRootElement getRootElement()
	{
		return rootElement;
	}
	
	
	public LSContainer getParent()
	{
		return parent;
	}
	
	protected void setParent(LSContainer parent, LSRootElement root)
	{
		this.parent = parent;
		if ( root != rootElement )
		{
			setRootElement( root );
		}
		onParentChanged();
	}
	
	
	protected void unparent()
	{
		if ( parent != null )
		{
			parent.replaceChildWithEmpty( this );
		}
	}
	
	
	
	public void replaceWith(LSElement replacement)
	{
		if ( parent != null )
		{
			int alignmentFlags = getAlignmentFlags();
			replacement.setAlignmentFlags( alignmentFlags );
			parent.replaceChild( this, replacement );
		}
		else
		{
			throw new RuntimeException( "Could not replace element - element has no parent" );
		}
	}
	
	
	public int computeSubtreeSize()
	{
		return 1;
	}
	
	
	public boolean isInSubtreeRootedAt(LSElement r)
	{
		LSElement e = this;
		
		while ( e != null )
		{
			if ( e == r )
			{
				return true;
			}
			
			e = e.getParent();
		}
		
		return false;
	}
	
	
	public ArrayList<LSElement> getElementPathFromRoot()
	{
		return getElementPathFromAncestor( null );
	}
	
	public ArrayList<LSElement> getElementPathToRoot()
	{
		return getElementPathToAncestor( null );
	}
	
	public ArrayList<LSElement> getElementPathFromAncestor(LSContainer ancestor)
	{
		ArrayList<LSElement> path = getElementPathToAncestor( ancestor );
		Collections.reverse( path );
		return path;
	}
	
	public ArrayList<LSElement> getElementPathToAncestor(LSContainer ancestor)
	{
		ArrayList<LSElement> path = new ArrayList<LSElement>();
		
		LSElement element = this;
		while ( element != null )
		{
			path.add( element );
			if ( element == ancestor )
			{
				return path;
			}
			element = element.getParent();
		}

		if ( ancestor == null )
		{
			return path;
		}
		else
		{
			throw new RuntimeException( "Element not within subtree" );
		}
	}
	
	

	public LSContentLeaf getFirstLeafInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
	{
		return null;
	}

	public LSContentLeaf getFirstLeafInSubtree()
	{
		return getFirstLeafInSubtree( null, null );
	}

	public LSContentLeaf getFirstEditableLeafInSubtree()
	{
		return getFirstLeafInSubtree( null, LSContentLeafEditable.editableLeafElementFilter );
	}

	public LSContentLeaf getLastLeafInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
	{
		return null;
	}

	public LSContentLeaf getLastLeafInSubtree()
	{
		return getLastLeafInSubtree( null, null );
	}

	public LSContentLeaf getLastEditableLeafInSubtree()
	{
		return getLastLeafInSubtree( null, LSContentLeafEditable.editableLeafElementFilter );
	}

	
	
	public static void getPathsFromCommonAncestor(LSElement w0, List<LSElement> path0, LSElement w1, List<LSElement> path1)
	{
		if ( w0 == w1 )
		{
			path0.add( w0 );
			path1.add( w1 );
		}
		else
		{
			ArrayList<LSElement> p0 = w0.getElementPathFromRoot();
			ArrayList<LSElement> p1 = w1.getElementPathFromRoot();
			
			int minLength = Math.min( p0.size(), p1.size() );
			
			if ( p0.get( 0 ) != p1.get( 0 ) )
			{
				throw new RuntimeException( "Bad path" );
			}
			
			int numCommonElements = 0;
			
			for (int i = 0; i < minLength; i++)
			{
				numCommonElements = i;
				
				if ( p0.get( i ) != p1.get( i ) )
				{
					break;
				}
			}
			
			path0.addAll( p0.subList( numCommonElements - 1, p0.size() ) );
			path1.addAll( p1.subList( numCommonElements - 1, p1.size() ) );
		}
	}
	
	public static LSElement getCommonAncestor(LSElement w0, LSElement w1)
	{
		if ( w0 == w1 )
		{
			return w0;
		}
		else
		{
			ArrayList<LSElement> p0 = w0.getElementPathFromRoot();
			ArrayList<LSElement> p1 = w1.getElementPathFromRoot();
			
			int minLength = Math.min( p0.size(), p1.size() );
			
			if ( p0.get( 0 ) != p1.get( 0 ) )
			{
				throw new RuntimeException( "Bad path" );
			}
			
			LSElement commonAncestor = null;
			
			for (int i = 0; i < minLength; i++)
			{
				if ( p0.get( i ) != p1.get( i ) )
				{
					break;
				}

				commonAncestor = p0.get( i );
			}
			
			return commonAncestor;
		}
	}
	
	public static boolean areElementsInOrder(LSElement w0, LSElement w1)
	{
		ArrayList<LSElement> path0 = new ArrayList<LSElement>();
		ArrayList<LSElement> path1 = new ArrayList<LSElement>();
		LSElement.getPathsFromCommonAncestor( w0, path0, w1, path1 );
		
		if ( path0.size() > 1  &&  path1.size() > 1 )
		{
			LSContainer commonRoot = (LSContainer)path0.get( 0 );
			return commonRoot.areChildrenInOrder( path0.get( 1 ), path1.get( 1 ) );
		}
		else if ( path0.size() == 1  &&  path1.size() == 1 )
		{
			if ( w0 != w1 )
			{
				throw new RuntimeException( "Paths have length 1, but elements are different" );
			}
			return true;
		}
		else
		{
			throw new RuntimeException( "Paths should either both have length == 1, or both have length > 1" );
		}
	}
	
	
	protected void setRootElement(LSRootElement root)
	{
		if ( root != rootElement )
		{
			rootElement = root;
			if ( rootElement != null )
			{
				if ( testFlag( FLAG_HAS_WAITING_IMMEDIATE_EVENTS ) )
				{
					ArrayList<Runnable> waitingImmediateEvents = waitingImmediateEventsByElement.get( this );
					if ( waitingImmediateEvents != null )
					{
						for (Runnable event: waitingImmediateEvents)
						{
							rootElement.queueImmediateEvent( event );
						}
						waitingImmediateEventsByElement.remove( this );
					}
				}
			}
		}
	}
	
	
	
	//
	//
	// ELEMENT TREE SEARCHING
	//
	//
	
	protected List<LSElement> getSearchChildren()
	{
		return null;
	}
	
	

	
	//
	// Immediate event queue methods
	//
	
	public void queueImmediateEvent(Runnable event)
	{
		if ( rootElement != null )
		{
			rootElement.queueImmediateEvent( event );
		}
		else
		{
			ArrayList<Runnable> waitingImmediateEvents = waitingImmediateEventsByElement.get( this );
			if ( waitingImmediateEvents == null )
			{
				waitingImmediateEvents = new ArrayList<Runnable>();
				waitingImmediateEventsByElement.put( this, waitingImmediateEvents );
				setFlag( FLAG_HAS_WAITING_IMMEDIATE_EVENTS );
			}
			if ( !waitingImmediateEvents.contains( event ) )
			{
				waitingImmediateEvents.add( event );
			}
		}
			
	}

	public void dequeueImmediateEvent(Runnable event)
	{
		if ( rootElement != null )
		{
			rootElement.dequeueImmediateEvent( event );
		}
		else
		{
			ArrayList<Runnable> waitingImmediateEvents = waitingImmediateEventsByElement.get( this );
			if ( waitingImmediateEvents != null )
			{
				waitingImmediateEvents.remove( event );
				if ( waitingImmediateEvents.isEmpty() )
				{
					waitingImmediateEvents = null;
					waitingImmediateEventsByElement.remove( this );
					clearFlag( FLAG_HAS_WAITING_IMMEDIATE_EVENTS );
				}
			}
		}
			
	}
	
	
	
	
	//
	// General event methods
	//
	
	protected void onRealise()
	{
	}
	
	protected void onUnrealise(LSElement unrealiseRoot)
	{
	}
	
	protected void onParentChanged()
	{
	}
	
	
	protected void drawBackground(Graphics2D graphics)
	{
		ElementStyleParams styleParams = getStyleParams();
		
		Painter backgroundPainter;
		if ( testFlag( FLAG_HOVER ) )
		{
			Painter hoverBackground = styleParams.getHoverBackground();
			backgroundPainter = hoverBackground != null  ?  hoverBackground  :  styleParams.getBackground();
		}
		else
		{
			backgroundPainter = styleParams.getBackground();
		}
		if ( backgroundPainter != null )
		{
			backgroundPainter.drawShapes( graphics, getShapes() );
		}
	}
	
	protected void draw(Graphics2D graphics)
	{
	}
	
	
	protected void onSetScale(double scale)
	{
	}
	
	
	protected void queueResize()
	{
		LayoutNode layout = getValidLayoutNode();
		if ( layout != null )
		{
			layout.queueResize();
		}
	}
	
	
	protected void queueRedraw(AABox2 box)
	{
		if ( isRealised()  &&  parent != null )
		{
			parent.childRedrawRequest( this, box );
		}
	}
	
	public void queueFullRedraw()
	{
		queueRedraw( getLocalAABox() );
	}
	
	
	public boolean isRedrawRequiredOnHover()
	{
		return styleParams.getHoverBackground() != null;
	}
	
	public boolean isResizeRequiredOnHover()
	{
		return false;
	}
	
	
	
	
	
	




	protected void handleRealise()
	{
		setFlagRealised();
		onRealise();
		
		List<AbstractElementInteractor> interactors = getElementInteractorsCopy( RealiseElementInteractor.class );
		if ( interactors != null )
		{
			for (AbstractElementInteractor interactor: interactors )
			{
				RealiseElementInteractor realiseInt = (RealiseElementInteractor)interactor;
				try
				{
					realiseInt.elementRealised( this );
				}
				catch (Throwable e)
				{
					notifyExceptionDuringElementInteractor( realiseInt, "elementRealised", e );
				}
			}
		}
		
		if ( testFlag( FLAG_ENSURE_VISIBLE_QUEUED ) )
		{
			clearFlag( FLAG_ENSURE_VISIBLE_QUEUED );
			rootElement.queueEnsureVisible( this );
		}
	}
	
	protected void handleUnrealise(LSElement unrealiseRoot)
	{
		invalidateCachedValues();

		if ( testFlag( FLAG_CARET_GRABBED ) )
		{
			if ( rootElement != null )
			{
				rootElement.getCaret().ungrab( this );
			}
			clearFlag( FLAG_CARET_GRABBED );
		}
		
		if ( rootElement != null )
		{
			rootElement.elementUnrealised( this );
		}
		
		List<AbstractElementInteractor> interactors = getElementInteractorsCopy( RealiseElementInteractor.class );
		if ( interactors != null )
		{
			for (AbstractElementInteractor interactor: interactors)
			{
				RealiseElementInteractor realiseInt = (RealiseElementInteractor)interactor;
				try
				{
					realiseInt.elementUnrealised( this );
				}
				catch (Throwable e)
				{
					notifyExceptionDuringElementInteractor( realiseInt, "elementUnrealised", e );
				}
			}
		}

		onUnrealise( unrealiseRoot );
		clearFlagRealised();
	}
	
	
	//
	//
	// PAINTING METHODS
	//
	//
	
	protected Shape pushVisibleBoundsClip(Graphics2D graphics)
	{
		Shape clipShape = null;
		AABox2 localClip = getLocalVisibleBoundsClipBox();
		if ( localClip != null )
		{
			clipShape = graphics.getClip();
			graphics.clip( new Rectangle2D.Double( localClip.getLowerX(), localClip.getLowerY(), localClip.getWidth(), localClip.getHeight() ) );
		}
		return clipShape;
	}
	
	protected void popVisibleBoundsClip(Graphics2D graphics, Shape clipShape)
	{
		if ( getLocalVisibleBoundsClipBox() != null )
		{
			graphics.setClip( clipShape );
		}
	}

	public void clipToAllocBox(Graphics2D graphics)
	{
		graphics.clip( new Rectangle2D.Double( 0.0, 0.0, getActualWidth(), getActualHeight() ) );
	}
	
	protected void handleDrawSelfBackground(Graphics2D graphics, AABox2 areaBox)
	{
		drawBackground( graphics );
		List<ElementPainter> painters = getPainters();
		if ( painters != null )
		{
			for (ElementPainter painter: painters)
			{
				try
				{
					painter.drawBackground( this, graphics );
				}
				catch (Throwable e)
				{
					notifyExceptionDuringElementInteractor( painter, "drawBackground", e );
				}
			}
		}
	}
	
	protected void handleDrawSelf(Graphics2D graphics, AABox2 areaBox)
	{
		draw( graphics );
		List<ElementPainter> painters = getPainters();
		if ( painters != null )
		{
			for (ElementPainter painter: painters)
			{
				try
				{
					painter.draw( this, graphics );
				}
				catch (Throwable e)
				{
					notifyExceptionDuringElementInteractor( painter, "draw", e );
				}
			}
		}
	}
	
	
	protected void handleDrawBackground(Graphics2D graphics, AABox2 areaBox)
	{
		handleDrawSelfBackground( graphics, areaBox );
	}

		
	protected void handleDraw(Graphics2D graphics, AABox2 areaBox)
	{
		handleDrawSelf( graphics, areaBox );
	}

	
	public void drawToGraphics(Graphics2D graphics, AABox2 areaBox)
	{
		handleDrawBackground( graphics, areaBox );
		handleDraw( graphics, areaBox );
	}
	

	
	
	
	//
	//
	// Element tree traversal methods
	//
	//
	
	public LSElement getFirstChildAtLocalPoint(Point2 localPos)
	{
		return null;
	}
	
	public LSElement getLastChildAtLocalPoint(Point2 localPos)
	{
		return null;
	}
	

	
	
	//
	//
	// POINTER INPUT ELEMENT METHODS
	//
	//
	
	
	protected Cursor getCursor()
	{
		return styleParams.getCursor();
	}
	
	protected Cursor getAncestorCursor()
	{
		LSElement w = getParent();
		while ( w != null )
		{
			Cursor cursor = w.getCursor();
			if ( cursor != null )
			{
				return cursor;
			}
			
			w = w.getParent();
		}
		
		return null;
	}
	

	public void handlePointerEnter(PointerMotionEvent event)
	{
		handleHover();
			
		Cursor cursor = getCursor();
		if ( cursor != null  &&  rootElement != null )
		{
			rootElement.setPointerCursor( cursor );
		}
	}
	
	public void handlePointerLeave(PointerMotionEvent event)
	{
		handleHover();
		
		Cursor cursor = getCursor();
		if ( cursor != null  &&  rootElement != null )
		{
			Cursor ancestorCursor = getAncestorCursor();
			if ( ancestorCursor != null )
			{
				getRootElement().setPointerCursor( ancestorCursor );
			}
			else
			{
				getRootElement().setPointerCursorDefault();
			}
		}
	}
	
	private void handleHover()
	{
		boolean bPrevHover = testFlag( FLAG_HOVER );
		boolean bHover = arePointersWithinBounds();
		if ( bHover != bPrevHover )
		{
			setFlagValue( FLAG_HOVER, bHover );
			if ( isResizeRequiredOnHover() )
			{
				queueResize();
			}
			else if ( isRedrawRequiredOnHover() )
			{
				queueFullRedraw();
			}
		}
	}
	
	public boolean isHoverActive()
	{
		return testFlag( FLAG_HOVER );
	}
	

	
	
	//
	//
	// CARET METHODS
	//
	//
	
	protected void onCaretEnter(Caret c)
	{
	}
	
	protected void onCaretLeave(Caret c)
	{
	}
	
	
	public void handleCaretEnter(Caret c)
	{
		onCaretEnter( c );
		List<AbstractElementInteractor> interactors = getElementInteractorsCopy( CaretCrossingElementInteractor.class );
		if ( interactors != null )
		{
			for (AbstractElementInteractor interactor: interactors )
			{
				CaretCrossingElementInteractor caretCrossInt = (CaretCrossingElementInteractor)interactor;
				try
				{
					caretCrossInt.caretEnter( this, c );
				}
				catch (Throwable e)
				{
					notifyExceptionDuringElementInteractor( caretCrossInt, "caretEnter", e );
				}
			}
		}
	}
	
	public void handleCaretLeave(Caret c)
	{
		onCaretLeave( c );
		List<AbstractElementInteractor> interactors = getElementInteractorsCopy( CaretCrossingElementInteractor.class );
		if ( interactors != null )
		{
			for (AbstractElementInteractor interactor: interactors )
			{
				CaretCrossingElementInteractor caretCrossInt = (CaretCrossingElementInteractor)interactor;
				try
				{
					caretCrossInt.caretLeave( this, c );
				}
				catch (Throwable e)
				{
					notifyExceptionDuringElementInteractor( caretCrossInt, "caretLeave", e );
				}
			}
		}
	}
	
	
	
	public void grabCaret()
	{
		if ( isRealised() )
		{
			setFlag( FLAG_CARET_GRABBED );
			getRootElement().getCaret().grab( this );
		}
	}
	
	public void ungrabCaret()
	{
		if ( isRealised()  &&  testFlag( FLAG_CARET_GRABBED ) )
		{
			getRootElement().getCaret().ungrab( this );
			clearFlag( FLAG_CARET_GRABBED );
		}
	}

	
	
	//
	//
	// INTERACTION FIELDS METHODS
	//
	//
	
	private void ensureValidInteractionFields()
	{
		if ( interactionFields == null )
		{
			interactionFields = new InteractionFields();
		}
	}
	
	private void notifyInteractionFieldsModified()
	{
		if ( interactionFields != null  &&  interactionFields.isIdentity() )
		{
			interactionFields = null;
		}
	}
	
	
	
	
	//
	//
	// DRAG AND DROP METHODS
	//
	//
	
	public void addDragSource(ObjectDndHandler.DragSource dragSource)
	{
		ObjectDndHandler current = getValidInitialDndHandler();
		ensureValidInteractionFields();
		interactionFields.dndHandler = current.withDragSource( dragSource ); 
		notifyInteractionFieldsModified();
	}
	
	
	public void addDropDest(ObjectDndHandler.DropDest dropDest)
	{
		ObjectDndHandler current = getValidInitialDndHandler();
		ensureValidInteractionFields();
		interactionFields.dndHandler = current.withDropDest( dropDest );
		notifyInteractionFieldsModified();
	}
	
	
	public void addNonLocalDropDest(ObjectDndHandler.NonLocalDropDest dropDest)
	{
		ObjectDndHandler current = getValidInitialDndHandler();
		ensureValidInteractionFields();
		interactionFields.dndHandler = current.withNonLocalDropDest( dropDest );
		notifyInteractionFieldsModified();
	}
	

	private ObjectDndHandler getValidInitialDndHandler()
	{
		if ( interactionFields != null  &&  interactionFields.dndHandler != null )
		{
			return interactionFields.dndHandler;
		}
		else
		{
			return ObjectDndHandler.instance;
		}
	}
	
	
	
	public DndHandler getDndHandler()
	{
		return interactionFields != null  ?  interactionFields.dndHandler  :  null;
	}
	
	
	
	
	public static ArrayList<DndTarget> getDndTargets(LSElement element, Point2 localPos)
	{
		ArrayList<DndTarget> targets = new ArrayList<DndTarget>(); 
		getDndTargets( element, localPos, targets );
		return targets;
	}

	private static void getDndTargets(LSElement element, Point2 localPos, List<DndTarget> targets)
	{
		LSElement child = element.getFirstChildAtLocalPoint( localPos );
		if ( child != null )
		{
			getDndTargets( child, child.transformParentToLocalPoint( localPos ), targets );
		}
		
		DndHandler handler = element.getDndHandler();
		if ( handler != null )
		{
			targets.add( new DndTarget( element, handler, localPos ) );
		}
	}

	
	
	
	//
	//
	// ELEMENT INTERACTOR METHODS
	//
	//
	
	public void addElementInteractor(AbstractElementInteractor interactor)
	{
		ensureValidInteractionFields();
		interactionFields.addElementInteractor( interactor );
		notifyInteractionFieldsModified();
	}
	
	public void removeElementInteractor(AbstractElementInteractor interactor)
	{
		if ( interactionFields != null )
		{
			interactionFields.removeElementInteractor( interactor );
			notifyInteractionFieldsModified();
		}
	}
	
	public Iterable<AbstractElementInteractor> getElementInteractors()
	{
		return interactionFields != null  ?  interactionFields.elementInteractors  :  null;
	}
	
	public Iterable<AbstractElementInteractor> getElementInteractors(final Class<?> interactorClass)
	{
		final ArrayList<AbstractElementInteractor> interactors = interactionFields != null  ?  interactionFields.elementInteractors  :  null;
		
		if ( interactors != null )
		{
			Iterable<AbstractElementInteractor> iterable = new Iterable<AbstractElementInteractor>()
			{
				public Iterator<AbstractElementInteractor> iterator()
				{
					Iterator<AbstractElementInteractor> iter = new Iterator<AbstractElementInteractor>()
					{
						private int index = nextIndex( 0 );
						
						
						public boolean hasNext()
						{
							return index != -1;
						}
	
						public AbstractElementInteractor next()
						{
							if ( index == -1 )
							{
								throw new NoSuchElementException();
							}
							else
							{
								AbstractElementInteractor interactor = interactors.get( index );
								index = nextIndex( index + 1 );
								return interactor;
							}
						}
	
						public void remove()
						{
							throw new UnsupportedOperationException();
						}
						
						
						private int nextIndex(int current)
						{
							for (int i = current; i < interactors.size(); i++)
							{
								AbstractElementInteractor interactor = interactors.get( i );
								if ( interactorClass.isInstance( interactor ) )
								{
									return i;
								}
							}
							
							return -1;
						}
					};
					
					return iter;
				}
			};
			
			return iterable;
		}
		else
		{
			return null;
		}
	}
	
	public List<AbstractElementInteractor> getElementInteractorsCopy(final Class<?> interactorClass)
	{
		if ( interactionFields != null  &&  interactionFields.elementInteractors != null )
		{
			ArrayList<AbstractElementInteractor> interactors = new ArrayList<AbstractElementInteractor>();

			for (AbstractElementInteractor interactor: interactionFields.elementInteractors)
			{
				if ( interactorClass.isInstance( interactor ) )
				{
					interactors.add( interactor );
				}
			}

			return interactors;
		}

		return null;
	}


	
	//
	//
	// Keyboard shortcut methods
	//
	//
	
	public void addShortcut(Shortcut shortcut, ShortcutElementAction action)
	{
		ShortcutElementInteractor interactor = null;
		
		Iterable<AbstractElementInteractor> interactors = getElementInteractors();
		if ( interactors != null )
		{
			for (AbstractElementInteractor a: interactors)
			{
				if ( a instanceof ShortcutElementInteractor )
				{
					interactor = (ShortcutElementInteractor)a;
					break;
				}
			}
		}
		
		if ( interactor == null )
		{
			interactor = new ShortcutElementInteractor();
			addElementInteractor( interactor );
		}
		
		interactor.addShortcut( shortcut, action );
	}
	
	public void removeShortcut(Shortcut shortcut)
	{
		ShortcutElementInteractor interactor = null;
		
		Iterable<AbstractElementInteractor> interactors = getElementInteractors();
		if ( interactors != null )
		{
			for (AbstractElementInteractor a: interactors)
			{
				if ( a instanceof ShortcutElementInteractor )
				{
					interactor = (ShortcutElementInteractor)a;
					break;
				}
			}
		}
		
		if ( interactor != null )
		{
			interactor.removeShortcut( shortcut );
			if ( interactor.isEmpty() )
			{
				removeElementInteractor( interactor );
			}
		}
	}
	
	
	
	
	//
	//
	// PAINTER METHODS
	//
	//
	
	public void addPainter(ElementPainter interactor)
	{
		ensureValidInteractionFields();
		interactionFields.addPainter( interactor );
		notifyInteractionFieldsModified();
	}
	
	public void removePainter(ElementPainter interactor)
	{
		if ( interactionFields != null )
		{
			interactionFields.removePainter( interactor );
			notifyInteractionFieldsModified();
		}
	}
	
	public ArrayList<ElementPainter> getPainters()
	{
		return interactionFields != null  ?  interactionFields.painters  :  null;
	}
	
	
	
	
	//
	//
	// CONTEXT MENU INTERACTOR METHODS
	//
	//
	
	public void addContextMenuInteractor(ContextMenuElementInteractor interactor)
	{
		addElementInteractor( interactor );
	}
	
	public void removeContextMenuInteractor(ContextMenuElementInteractor interactor)
	{
		removeElementInteractor( interactor );
	}
	
	
	
	
	//
	//
	// LAYOUT METHODS
	//
	//
	
	
	public LayoutNode getLayoutNode()
	{
		return layoutNode;
	}
	
	public LayoutNode getValidLayoutNode()
	{
		if ( layoutNode != null )
		{
			return layoutNode;
		}
		else
		{
			LSContainer c = parent;
			while ( c != null  )
			{
				if ( c.layoutNode != null )
				{
					return c.layoutNode;
				}
				c = c.getParent();
			}
			
			return null;
		}
	}
	
	public LayoutNode getValidLayoutNodeOfClass(Class<? extends LayoutNode> layoutNodeClass)
	{
		if ( layoutNode != null )
		{
			return layoutNode;
		}
		else
		{
			LSContainer c = parent;
			while ( c != null  )
			{
				if ( c.layoutNode != null )
				{
					if ( layoutNodeClass.isInstance( c.layoutNode ) )
					{
						return c.layoutNode;
					}
				}
				c = c.getParent();
			}
			
			return null;
		}
	}
	
	

	

	
	
	//
	// Focus navigation methods
	//
	
	public Point2 getMarkerPosition(Marker marker)
	{
		if ( marker.getElement() != this )
		{
			throw new RuntimeException( "Marker is not within the bounds of this element" );
		}
		return new Point2( getActualWidth() * 0.5, getActualHeight() * 0.5 );
	}
	
	
	
	
	//
	//
	// CONTENT LEAF METHODS
	//
	//
	
	public LSContentLeaf getLeftContentLeaf()
	{
		if ( layoutNode != null )
		{
			return layoutNode.getLeftContentLeaf();
		}
		else
		{
			ArrangedSequenceLayoutNode branchLayout = (ArrangedSequenceLayoutNode)getValidLayoutNodeOfClass( ArrangedSequenceLayoutNode.class );
			return branchLayout.getLeftContentLeafWithinLayoutlessElement( this );
		}
	}
	
	public LSContentLeaf getRightContentLeaf()
	{
		if ( layoutNode != null )
		{
			return layoutNode.getRightContentLeaf();
		}
		else
		{
			ArrangedSequenceLayoutNode branchLayout = (ArrangedSequenceLayoutNode)getValidLayoutNodeOfClass( ArrangedSequenceLayoutNode.class );
			return branchLayout.getRightContentLeafWithinLayoutlessElement( this );
		}
	}
	
	public LSContentLeafEditable getLeftEditableContentLeaf()
	{
		if ( layoutNode != null )
		{
			return layoutNode.getLeftEditableContentLeaf();
		}
		else
		{
			ArrangedSequenceLayoutNode branchLayout = (ArrangedSequenceLayoutNode)getValidLayoutNodeOfClass( ArrangedSequenceLayoutNode.class );
			return branchLayout.getLeftEditableContentLeafWithinElement( this );
		}
	}
	
	public LSContentLeafEditable getRightEditableContentLeaf()
	{
		if ( layoutNode != null )
		{
			return layoutNode.getRightEditableContentLeaf();
		}
		else
		{
			ArrangedSequenceLayoutNode branchLayout = (ArrangedSequenceLayoutNode)getValidLayoutNodeOfClass( ArrangedSequenceLayoutNode.class );
			return branchLayout.getRightEditableContentLeafWithinLayoutlessElement( this );
		}
	}
	
	public LSContentLeaf getContentLeafToLeft()
	{
		if ( layoutNode != null )
		{
			return layoutNode.getContentLeafToLeft();
		}
		else
		{
			ArrangedSequenceLayoutNode branchLayout = (ArrangedSequenceLayoutNode)getValidLayoutNodeOfClass( ArrangedSequenceLayoutNode.class );
			return branchLayout.getContentLeafToLeftOfLayoutlessElement( this );
		}
	}
	
	public LSContentLeaf getContentLeafToRight()
	{
		if ( layoutNode != null )
		{
			return layoutNode.getContentLeafToRight();
		}
		else
		{
			ArrangedSequenceLayoutNode branchLayout = (ArrangedSequenceLayoutNode)getValidLayoutNodeOfClass( ArrangedSequenceLayoutNode.class );
			return branchLayout.getContentLeafToRightOfLayoutlessElement( this );
		}
	}
	
	public LSContentLeafEditable getTopOrBottomEditableContentLeaf(boolean bBottom, Point2 cursorPosInRootSpace)
	{
		if ( layoutNode != null )
		{
			return layoutNode.getTopOrBottomEditableContentLeaf( bBottom, cursorPosInRootSpace );
		}
		else
		{
			ArrangedSequenceLayoutNode branchLayout = (ArrangedSequenceLayoutNode)getValidLayoutNodeOfClass( ArrangedSequenceLayoutNode.class );
			return branchLayout.getTopOrBottomEditableContentLeafWithinLayoutlessElement( this, bBottom, cursorPosInRootSpace );
		}
	}



	public LSElement getLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
	{
		if ( layoutNode != null )
		{
			return layoutNode.getLeafClosestToLocalPoint( localPos, filter );
		}
		else
		{
			ArrangedSequenceLayoutNode branchLayout = (ArrangedSequenceLayoutNode)getValidLayoutNodeOfClass( ArrangedSequenceLayoutNode.class );
			return branchLayout.getChildLeafClosestToLocalPointWithinBranch( (LSContainer)this, localPos, filter );
		}
	}
	
	public LSElement getEditableLeafClosestToLocalPoint(Point2 localPos)
	{
		return getLeafClosestToLocalPoint( localPos, LSContentLeafEditable.editableRealisedLeafElementFilter );
	}

	public LSElement getSelectableLeafClosestToLocalPoint(Point2 localPos)
	{
		return getLeafClosestToLocalPoint( localPos, LSContentLeafEditable.selectableRealisedLeafElementFilter );
	}

	
	
	
	
	
	//
	//
	// TREE EVENT METHODS
	//
	//
	
	public void addTreeEventListener(TreeEventListener listener)
	{
		ensureValidInteractionFields();
		interactionFields.addTreeEventListener( listener );
		notifyInteractionFieldsModified();
	}
	
	public void removeTreeEventListener(TreeEventListener listener)
	{
		if ( interactionFields != null )
		{
			interactionFields.removeTreeEventListener( listener );
			notifyInteractionFieldsModified();
		}
	}
	
	public ArrayList<TreeEventListener> getTreeEventListeners()
	{
		return interactionFields != null  ?  interactionFields.treeEventListeners  :  null;
	}

	
	
	public boolean postTreeEventToParent(Object event)
	{
		return postTreeEventToParentUntil( event, null );
	}
	
	public boolean postTreeEventToParentUntil(Object event, LSElement stopBefore)
	{
		if ( parent != null )
		{
			return parent.treeEvent( new TreeEvent( this, event ), stopBefore );
		}
		else
		{
			return false;
		}
	}
	
	public boolean postTreeEvent(Object event)
	{
		return treeEvent( new TreeEvent( this, event ), null );
	}
	
	public boolean postTreeEventUntil(Object event, LSElement stopBefore)
	{
		return treeEvent( new TreeEvent( this, event ), stopBefore );
	}
	
	
	protected boolean treeEvent(TreeEvent event, LSElement stopBefore)
	{
		if ( this != stopBefore )
		{
			ArrayList<TreeEventListener> listeners = getTreeEventListeners();
			if ( listeners != null )
			{
				ArrayList<TreeEventListener> listenersCopy = new ArrayList<TreeEventListener>( listeners );
				for (TreeEventListener listener: listenersCopy)
				{
					if ( listener.onTreeEvent( this, event.sourceElement, event.value ) )
					{
						return true;
					}
				}
			}
			
			if ( parent != null )
			{
				return parent.treeEvent( event, stopBefore );
			}
		}
		
		return false;
	}
	
	
	
	
	//
	//
	// TEXT REPRESENTATION METHODS
	//
	//
	
	
	public LSContentLeaf getLeafAtTextRepresentationPosition(int position)
	{
		return getRootElement().getDefaultTextRepresentationManager().getLeafAtPositionInSubtree( this, position );
	}
	
	
	public int getTextRepresentationOffsetInSubtree(LSContainer subtreeRoot)
	{
		return getRootElement().getDefaultTextRepresentationManager().getPositionOfElementInSubtree( subtreeRoot, this );
	}
	
	
	public String getTextRepresentationFromStartToMarker(Marker marker)
	{
		return getRootElement().getDefaultTextRepresentationManager().getTextRepresentationFromStartToMarker( marker, this );
	}
	
	public String getTextRepresentationFromMarkerToEnd(Marker marker)
	{
		return getRootElement().getDefaultTextRepresentationManager().getTextRepresentationFromMarkerToEnd( marker, this );
	}



	
	protected void textRepresentationChanged(Object event)
	{
		onTextRepresentationModified();
		postTreeEvent( event );
	}
	
	protected void onTextRepresentationModified()
	{
		if ( parent != null )
		{
			parent.onTextRepresentationModified();
		}
		
		invalidateCachedValues();
	}
	
	public String getTextRepresentation()
	{
		String leafTextRep = getLeafTextRepresentation();
		if ( leafTextRep != null )
		{
			return leafTextRep;
		}
		else
		{
			return getRootElement().getDefaultTextRepresentationManager().getTextRepresentationOf( this );
		}
	}
	
	public int getTextRepresentationLength()
	{
		String leafTextRep = getLeafTextRepresentation();
		if ( leafTextRep != null )
		{
			return leafTextRep.length();
		}
		else
		{
			return getTextRepresentation().length();
		}
	}
	
	
	
	public String getLeafTextRepresentation()
	{
		return null;
	}
	
	
	
	
	
	//
	//
	// VALUE METHODS
	//
	//
	
	public Object getValue()
	{
		return getValue( valueFn );
	}
	
	public Object getValue(ElementValueFunction fn)
	{
		if ( hasFixedValue() )
		{
			return fixedValue;
		}
		else
		{
			if ( fn != null )
			{
				return fn.computeElementValue( this );
			}
			else
			{
				return getDefaultValue();
			}
		}
	}
	
	public abstract Object getDefaultValue();
	
	
	
	
	
	//
	//
	// SEQUENTIAL VISIT METHODS
	//
	//
	
	private static List<LSElement> emptyChildList = Arrays.asList();
	
	public List<LSElement> getChildrenInSequentialOrder()
	{
		return emptyChildList;
	}
	
	

	//
	//
	// RICH STRING METHODS
	//
	//
	
	public void addToRichString(RichStringBuilder builder)
	{
	}
	
	public RichString getRichString()
	{
		SequentialRichStringVisitor visitor = new SequentialRichStringVisitor();
		return getRichString( visitor );
	}
	
	public RichString getRichString(SequentialRichStringVisitor visitor)
	{
		return visitor.getRichString( this );
	}
	
	
	
	// Value function
	
	public void setValueFunction(ElementValueFunction fn)
	{
		valueFn = fn;
	}
	
	public ElementValueFunction getValueFunction()
	{
		return valueFn;
	}
	
	
	// Fixed value
	
	public void setFixedValue(Object value)
	{
		fixedValue = value;
		setFlag( FLAG_HAS_FIXED_VALUE );
	}
	
	public void clearFixedValue()
	{
		fixedValue = null;
		clearFlag( FLAG_HAS_FIXED_VALUE );
	}
	
	public void clearFixedValuesOnPathUpTo(LSElement subtreeRoot)
	{
		LSElement e = this;
		
		while ( e != subtreeRoot )
		{
			e.clearFixedValue();
			
			e = e.parent;
			
			if ( e == null )
			{
				throw new IsNotInSubtreeException();
			}
		}
	}

	public boolean hasFixedValue()
	{
		return testFlag( FLAG_HAS_FIXED_VALUE );
	}
	
	public Object getFixedValue()
	{
		if ( hasFixedValue() )
		{
			return fixedValue;
		}
		else
		{
			return null;
		}
	}
	
	
	
	
	
	//
	//
	// POPUP METHODS
	//
	//
	
	public PresentationPopupWindow popup(LSElement targetElement, Anchor targetAnchor, Anchor popupAnchor,
			boolean closeAutomatically, boolean requestFocus)
	{
		AABox2 visibleBox = targetElement.getVisibleBoxInLocalSpace();
		Point2 targerCorner = targetAnchor.getBoxCorner( visibleBox );
		return popupOver( targetElement, targerCorner, popupAnchor, closeAutomatically, requestFocus );
	}
	
	public PresentationPopupWindow popupOver(LSElement targetElement, Point2 targetLocalPos, Anchor popupAnchor,
			boolean closeAutomatically, boolean requestFocus)
	{
		if ( targetElement.isLocalSpacePointVisible( targetLocalPos ) )
		{
			Xform2 x = targetElement.getLocalToRootXform();
			Point2 rootPos = x.transform( targetLocalPos );
			return targetElement.getRootElement().createPopupPresentation( this, rootPos, popupAnchor, closeAutomatically, requestFocus );
		}
		else
		{
			return targetElement.getRootElement().createPopupAtMousePosition( this, popupAnchor, closeAutomatically, requestFocus );
		}
	}
	

	public PresentationPopupWindow chainPopup(LSElement targetElement, Anchor targetAnchor, Anchor popupAnchor,
			boolean closeAutomatically, boolean requestFocus)
	{
		AABox2 visibleBox = targetElement.getVisibleBoxInLocalSpace();
		Point2 targerCorner = targetAnchor.getBoxCorner( visibleBox );
		return chainPopupOver( targetElement, targerCorner, popupAnchor, closeAutomatically, requestFocus );
	}
	
	public PresentationPopupWindow chainPopupOver(LSElement targetElement, Point2 targetLocalPos, Anchor popupAnchor,
			boolean closeAutomatically, boolean requestFocus)
	{
		if ( targetElement.isLocalSpacePointVisible( targetLocalPos ) )
		{
			Xform2 x = targetElement.getLocalToRootXform();
			Point2 rootPos = x.transform( targetLocalPos );
			return targetElement.getRootElement().createChainPopupPresentation( this, rootPos, popupAnchor, closeAutomatically, requestFocus );
		}
		else
		{
			return targetElement.getRootElement().createChainPopupAtMousePosition( this, popupAnchor, closeAutomatically, requestFocus );
		}
	}
	

	public boolean isInsidePopup()
	{
		return getRootElement().getComponent().isPopup();
	}
	
	public void closeContainingPopupChain()
	{
		if ( isInsidePopup() )
		{
			PopupPresentationComponent comp = (PopupPresentationComponent)getRootElement().getComponent(); 
			comp.closeContainingPopupChain();
		}
	}
	

	
	
	//
	//
	// SEGMENT METHODS
	//
	//
	
	protected boolean isSegment()
	{
		return false;
	}
	
	public LSSegment getSegment()
	{
		return LSSegment.getSegmentOf( this );
	}
	
	
	
	//
	//
	// REGION METHODS
	//
	//
	
	protected boolean isRegion()
	{
		return false;
	}
	
	public LSRegion getRegion()
	{
		return LSRegion.regionOf( this );
	}
	
	
	
	//
	//
	// VALUE CACHE METHODS
	//
	//
	
	protected boolean hasCachedValues()
	{
		return testFlag( FLAG_HAS_CACHED_VALUES );
	}
	
	protected void setHasCachedValues()
	{
		setFlag( FLAG_HAS_CACHED_VALUES );
	}
	
	protected void clearHasCachedValues()
	{
		clearFlag( FLAG_HAS_CACHED_VALUES );
	}
	
	protected void invalidateCachedValues()
	{
		if ( hasCachedValues() )
		{
			getRootElement().getElementValueCacheManager().invalidateCachedValuesFor( this );
		}
	}
	
	
	
	
	//
	//
	// PROPERTIES
	//
	//
	
	public PropertyValue getProperty(Object key)
	{
		return properties != null  ?  properties.get( key )  :  null;
	}
	
	public PropertyValue getFirstProperty(Iterable<Object> keys)
	{
		if ( properties != null )
		{
			for (Object key: keys)
			{
				PropertyValue val = properties.get( key );
				if ( val != null )
				{
					return val;
				}
			}
		}
		
		return null;
	}
	
	public void setProperty(Object key, Object value)
	{
		if ( properties == null )
		{
			properties = new HashMap<Object, PropertyValue>();
		}
		properties.put( key, new PropertyValue( this, key, value ) );
	}
	
	public void removeProperty(Object key)
	{
		if ( properties != null )
		{
			properties.remove( key );
			if ( properties.isEmpty() )
			{
				properties = null;
			}
		}
	}
	
	
	
	public PropertyValue findPropertyInAncestors(Object key)
	{
		LSElement e = this;
		while ( e != null )
		{
			PropertyValue v = e.getProperty( key );
			if ( v != null )
			{
				return v;
			}
			e = e.parent;
		}
		
		return null;
	}
	
	public List<PropertyValue> gatherPropertyInAncestryTo(Object key, LSElement subtreeRoot)
	{
		ArrayList<PropertyValue> values = new ArrayList<PropertyValue>();
		LSElement e = this;
		while ( e != null  &&  e != subtreeRoot )
		{
			PropertyValue v = e.getProperty( key );
			if ( v != null )
			{
				values.add( v );
			}
			e = e.parent;
		}

		if ( e == null  &&  subtreeRoot != null )
		{
			throw new IsNotInSubtreeException();
		}

		return values;
	}

	public PropertyValue findFirstPropertyInAncestors(Iterable<Object> keys)
	{
		LSElement e = this;
		while ( e != null )
		{
			PropertyValue v = e.getFirstProperty( keys );
			if ( v != null )
			{
				return v;
			}
			e = e.parent;
		}
		
		return null;
	}

	public List<PropertyValue> gatherPropertyInSubtree(Object key, boolean descendToLeaves)
	{
		GatherPropertyVisitor v = new GatherPropertyVisitor(key, descendToLeaves);
		v.visitSubtree(this);
		return v.values;
	}


	private static class GatherPropertyVisitor extends ElementTreeVisitor {
		private Object key;
		private boolean descendToLeaves;
		private ArrayList<PropertyValue> values = new ArrayList<PropertyValue>();

		public GatherPropertyVisitor(Object key, boolean descendToLeaves) {
			this.key = key;
			this.descendToLeaves = descendToLeaves;
		}

		protected void preOrderVisitElement(LSElement e, boolean complete) {
			if (complete) {
				PropertyValue val = e.getProperty(key);
				if (val != null) {
					values.add(val);
				}
			}
		}

		protected void inOrderCompletelyVisitElement(LSElement e) {
		}

		protected void postOrderVisitElement(LSElement e, boolean complete) {
		}

		protected void inOrderVisitPartialContentLeafEditable(LSContentLeafEditable e, int startIndex, int endIndex) {
		}

		protected boolean shouldVisitChildrenOfElement(LSElement e, boolean completeVisit) {
			if (completeVisit) {
				return e.getProperty(key) == null || descendToLeaves;
			}
			else {
				return false;
			}
		}
	}





	//
	//
	// DEBUG NAME METHODS
	//
	//	
	
	public void setDebugName(String debugName)
	{
		this.debugName = debugName;
	}
	
	public String getDebugName()
	{
		return debugName;
	}

	
	
	
	//
	//
	// STYLE PARAMS METHODS
	//
	//
	
	public ElementStyleParams getStyleParams()
	{
		return styleParams;
	}
	
	
	
	//
	// Meta-element
	//

	protected static StyleSheet headerDebugTextStyle = StyleSheet.style( Primitive.fontBold.as( true ), Primitive.foreground.as( new Color( 0.0f, 0.5f, 0.5f ) ) );
	protected static StyleSheet headerDescriptionTextStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.0f, 0.0f, 0.75f ) ) );
	protected static StyleSheet metaHeaderRowStyle = StyleSheet.style( Primitive.rowSpacing.as( 10.0 ) );
	protected static StyleSheet metaHeaderEmptyBorderStyle = StyleSheet.style( Primitive.border.as( new FilledBorder() ) );
	private static SolidBorder inspectorBorder = new SolidBorder( 1.0, 1.0, new Color( 0.5f, 0.5f, 0.5f ), Color.WHITE );
	
	private static ElementHighlighter explorerHeadHoverHighlighter = new ElementHighlighter(
			new FilledOutlinePainter( new Color( 0.0f, 0.4f, 0.8f, 0.25f ), new Color( 0.0f, 0.4f, 0.8f, 0.5f ), new BasicStroke( 1.0f ) ) );


	
	private static void activateExplorerHighlight(LSElement explorerElement)
	{
		FragmentView fragment = (FragmentView)explorerElement.getFragmentContext();
		LSElement model = (LSElement)fragment.getModel();
		explorerHeadHoverHighlighter.highlight( model );
	}
	
	private static void deactivateExplorerHighlight(LSElement explorerElement)
	{
		FragmentView fragment = (FragmentView)explorerElement.getFragmentContext();
		LSElement model = (LSElement)fragment.getModel();
		explorerHeadHoverHighlighter.unhighlight( model );
	}
	
	
	
	private static MotionElementInteractor explorerHeaderHoverInteractor = new MotionElementInteractor()
	{
		@Override
		public void pointerEnter(LSElement element, PointerMotionEvent event)
		{
			if ( Modifier.maskKeyModifiers(event.getModifiers())  == 0 )
			{
				activateExplorerHighlight( element );
			}
		}

		@Override
		public void pointerLeave(LSElement element, PointerMotionEvent event)
		{
			deactivateExplorerHighlight( element );
		}
		
		@Override
		public void pointerMotion(LSElement element, PointerMotionEvent event)
		{
			// Suppress highlight if any modifier keys are pressed - this is helpful for capturing an element for SVG rendering. 
			if ( Modifier.maskKeyModifiers( event.getModifiers() )  != 0 )
			{
				deactivateExplorerHighlight( element );
			}
		}

		@Override
		public void pointerLeaveIntoChild(LSElement element, PointerMotionEvent event)
		{
		}

		@Override
		public void pointerEnterFromChild(LSElement element, PointerMotionEvent event)
		{
		}
	};

	private static ClickElementInteractor explorerHeaderInspectInteractor = new ClickElementInteractor()
	{
		@Override
		public boolean testClickEvent(LSElement element, AbstractPointerButtonEvent event)
		{
			return event.getButton() == 1;
		}

		@Override
		public boolean buttonClicked(LSElement element, PointerButtonClickedEvent event)
		{
			LSElement el = (LSElement)element;
			FragmentView fragment = (FragmentView)el.getFragmentContext();
			LSElement model = (LSElement)fragment.getModel();

			Pres p = inspectorBorder.surround( inspect( model ) );
			p.popupAtMousePosition( el, Anchor.TOP_LEFT, true, true );

			return true;
		}
		
		
		private Pres inspect(LSElement model)
		{
			ElementStyleParams styleParams = model.getStyleParams();
			
			ArrayList<Object> fields = new ArrayList<Object>();
			fields.add( new HorizontalField( "H-Align", new Label( model.getHAlignment().toString() ) ) );
			fields.add( new HorizontalField( "V-Align", new Label( model.getVAlignment().toString() ) ) );
			
			Pres state = new ObjectBoxWithFields( "State", fields );
			
			Pres inspector = new Column( new Object[] { styleParams, new Spacer( 2.0, 2.0 ), state } ).alignVRefY();
			
			return DefaultPerspective.instance.applyTo( inspector );
		}
	};


	protected StyleSheet getDebugPresentationHeaderBorderStyle()
	{
		return metaHeaderEmptyBorderStyle;
	}
	
	protected void createDebugPresentationHeaderContents(ArrayList<Object> elements)
	{
		if ( debugName != null )
		{
			elements.add( headerDebugTextStyle.applyTo( new Label( "<" + debugName + ">" ) ) );
		}

		String description = toString();
		description = description.replace( "BritefuryJ.DocPresent.", "" );
		elements.add( headerDescriptionTextStyle.applyTo( new Label( description ) ) );
	}
	
	public Pres createDebugPresentationHeader()
	{
		ArrayList<Object> elements = new ArrayList<Object>();
		createDebugPresentationHeaderContents( elements );
		Pres box = metaHeaderRowStyle.applyTo( new Row( elements ).alignHPack() );
		Border border = new Border( box );
		return getDebugPresentationHeaderBorderStyle().applyTo( border.withElementInteractor( explorerHeaderHoverInteractor ).withElementInteractor( explorerHeaderInspectInteractor ) );
	}
	
	public Pres createMetaElement(FragmentView ctx, SimpleAttributeTable state)
	{
		return createDebugPresentationHeader();
	}
	
	private Pres exploreTreePresent(FragmentView ctx, SimpleAttributeTable state)
	{
		debugPresStateListeners = PresentationStateListenerList.addListener( debugPresStateListeners, ctx );
		return createMetaElement( ctx, state );
	}
	
	public ElementTreeExplorer treeExplorer()
	{
		return new ElementTreeExplorer( this );
	}
	
	
	protected void onDebugPresentationStateChanged()
	{
		debugPresStateListeners = PresentationStateListenerList.onPresentationStateChanged( debugPresStateListeners, this );
	}



	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		if ( !isRealised() )
		{
			return Pres.elementToPres( this );
		}
		else
		{
			return alreadyInUseStyle.applyTo( new Border( new Label( "Element already in use (element is realised)." ) ) );
		}
	}
	
	
	
	//
	//
	// Error notification methods
	//
	//
	
	public void notifyExceptionDuringElementInteractor(Object interactor, String event, Throwable e)
	{
		getRootElement().notifyExceptionDuringElementInteractor( this, interactor, event, e );
	}


	private static final StyleSheet alreadyInUseStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.75f, 0.0f, 0.0f ) ), Primitive.border.as( new SolidBorder( 1.0, 3.0, 5.0, 5.0, new Color( 0.75f, 0.0f, 0.0f ), null ) ) );
}
