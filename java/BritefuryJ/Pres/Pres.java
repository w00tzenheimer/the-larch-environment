//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import java.awt.datatransfer.DataFlavor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import BritefuryJ.AttributeTable.AttributeBase;
import BritefuryJ.Command.CommandSetSource;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.IncrementalView.IncrementalView;
import BritefuryJ.LSpace.Anchor;
import BritefuryJ.LSpace.ElementPainter;
import BritefuryJ.LSpace.ElementValueFunction;
import BritefuryJ.LSpace.FragmentContext;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.PresentationPopupWindow;
import BritefuryJ.LSpace.TreeEventListener;
import BritefuryJ.LSpace.Input.ObjectDndHandler;
import BritefuryJ.LSpace.Interactor.AbstractElementInteractor;
import BritefuryJ.LSpace.Interactor.ContextMenuElementInteractor;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.Projection.AbstractPerspective;
import BritefuryJ.Projection.Subject;
import BritefuryJ.Projection.TransientSubject;
import BritefuryJ.Shortcut.Shortcut;
import BritefuryJ.Shortcut.ShortcutElementAction;
import BritefuryJ.StyleSheet.StyleValues;

public abstract class Pres
{
	private static class PopupSubject extends TransientSubject
	{
		private Object focus;
		private AbstractPerspective perspective;
		
		
		public PopupSubject(Subject enclosingSubject, Object focus, AbstractPerspective perspective)
		{
			super( enclosingSubject );
			
			this.focus = focus;
			this.perspective = perspective;
		}

		@Override
		public Object getFocus()
		{
			return focus;
		}
		
		@Override
		public AbstractPerspective getPerspective()
		{
			return perspective;
		}

		@Override
		public String getTitle()
		{
			return "Popup";
		}
	}


	public LSElement present()
	{
		return present( PresentationContext.defaultCtx, StyleValues.getRootStyle() );
	}

	public abstract LSElement present(PresentationContext ctx, StyleValues style);
	
	
	
	public Align align(HAlignment hAlign, VAlignment vAlign)
	{
		return new Align( hAlign, vAlign, this );
	}
	

	public Align alignH(HAlignment hAlign)
	{
		return new Align( hAlign, this );
	}
	
	public Align alignV(VAlignment vAlign)
	{
		return new Align( vAlign, this );
	}
	

	public Align alignHPack()
	{
		return alignH( HAlignment.PACK );
	}

	public Align alignHLeft()
	{
		return alignH( HAlignment.LEFT );
	}

	public Align alignHCentre()
	{
		return alignH( HAlignment.CENTRE );
	}

	public Align alignHRight()
	{
		return alignH( HAlignment.RIGHT );
	}

	public Align alignHExpand()
	{
		return alignH( HAlignment.EXPAND );
	}
	
	
	public Align alignVRefY()
	{
		return alignV( VAlignment.REFY );
	}

	public Align alignVRefYExpand()
	{
		return alignV( VAlignment.REFY_EXPAND );
	}

	public Align alignVTop()
	{
		return alignV( VAlignment.TOP );
	}

	public Align alignVCentre()
	{
		return alignV( VAlignment.CENTRE );
	}

	public Align alignVBottom()
	{
		return alignV( VAlignment.BOTTOM );
	}

	public Align alignVExpand()
	{
		return alignV( VAlignment.EXPAND );
	}
	
	
	
	//
	// Padding methods
	//
	
	public Pres pad(double leftPad, double rightPad, double topPad, double bottomPad)
	{
		return new Pad( this, leftPad, rightPad, topPad, bottomPad );
	}
	
	public Pres pad(double xPad, double yPad)
	{
		return pad( xPad, xPad, yPad, yPad );
	}
	
	public Pres padX(double xPad)
	{
		return pad( xPad, xPad, 0.0, 0.0 );
	}
	
	public Pres padX(double leftPad, double rightPad)
	{
		return pad( leftPad, rightPad, 0.0, 0.0 );
	}
	
	public Pres padY(double yPad)
	{
		return pad( 0.0, 0.0, yPad, yPad );
	}
	
	public Pres padY(double topPad, double bottomPad)
	{
		return pad( 0.0, 0.0, topPad, bottomPad );
	}



	//
	// Inherited state
	//

	public Pres withInheritedStateAttr(String fieldName, Object value) {
		HashMap<String, Object> changed = new HashMap<String, Object>();
		changed.put(fieldName, value);
		return new ApplyInheritedStateAttrs(changed, null, this);
	}

	public Pres withoutInheritedStateAttr(String fieldName) {
		HashSet<String> removed = new HashSet<String>();
		removed.add(fieldName);
		return new ApplyInheritedStateAttrs(null, removed, this);
	}



	//
	// Element reference
	//
	
	public ElementRef elementRef()
	{
		return new ElementRef( this );
	}
	
	
	
	//
	// Drag and drop
	//
	
	public AddDragSource withDragSource(ObjectDndHandler.DragSource source)
	{
		return new AddDragSource( this, source );
	}
	
	public AddDragSource withDragSource(Class<?> dataType, int sourceAspects, ObjectDndHandler.SourceDataFn sourceDataFn, ObjectDndHandler.ExportDoneFn exportDoneFn)
	{
		return new AddDragSource( this, dataType, sourceAspects, sourceDataFn, exportDoneFn );
	}
	
	public AddDragSource withDragSource(Class<?> dataType, ObjectDndHandler.SourceDataFn sourceDataFn, ObjectDndHandler.ExportDoneFn exportDoneFn)
	{
		return new AddDragSource( this, dataType, sourceDataFn, exportDoneFn );
	}
	
	public AddDragSource withDragSource(Class<?> dataType, ObjectDndHandler.SourceDataFn sourceDataFn)
	{
		return new AddDragSource( this, dataType, sourceDataFn );
	}
	
	
	public AddDropDest withDropDest(ObjectDndHandler.DropDest dest)
	{
		return new AddDropDest( this, dest );
	}
	
	public AddDropDest withDropDest(Class<?> dataType, ObjectDndHandler.CanDropFn canDropFn, ObjectDndHandler.DropHighlightFn highlightFn, ObjectDndHandler.DropFn dropFn)
	{
		return new AddDropDest( this, dataType, canDropFn, highlightFn, dropFn );
	}
	
	public AddDropDest withDropDest(Class<?> dataType, ObjectDndHandler.DropFn dropFn)
	{
		return new AddDropDest( this, dataType, dropFn );
	}
	
	
	public AddNonLocalDropDest withNonLocalDropDest(ObjectDndHandler.NonLocalDropDest dest)
	{
		return new AddNonLocalDropDest( this, dest );
	}
	
	public AddNonLocalDropDest withNonLocalDropDest(DataFlavor dataFlavor, ObjectDndHandler.DropHighlightFn highlightFn, ObjectDndHandler.DropFn dropFn)
	{
		return new AddNonLocalDropDest( this, dataFlavor, highlightFn, dropFn );
	}
	
	public AddNonLocalDropDest withNonLocalDropDest(DataFlavor dataFlavor, ObjectDndHandler.DropFn dropFn)
	{
		return new AddNonLocalDropDest( this, dataFlavor, null, dropFn );
	}
	
	
	
	//
	// Interactor methods
	//
	
	public AddElementInteractor withElementInteractor(AbstractElementInteractor interactor)
	{
		return new AddElementInteractor( this, interactor );
	}
	
	
	
	//
	// Shortcut methods
	//
	
	public AddShortcuts withShortcut(Shortcut shortcut, ShortcutElementAction action)
	{
		return new AddShortcuts( this, shortcut, action );
	}
	
	
	
	//
	// Command set methods
	//
	
	public AddCommandSets withCommands(CommandSetSource commandSet)
	{
		return new AddCommandSets( this, commandSet );
	}
	
	
	
	//
	// Painter methods
	//
	
	public AddElementPainter withPainter(ElementPainter painter)
	{
		return new AddElementPainter( this, painter );
	}
	
	
	
	//
	// Tree event methods
	//
	
	public AddTreeEventListener withTreeEventListener(TreeEventListener listener)
	{
		return new AddTreeEventListener( this, listener );
	}
	
	
	
	//
	// Context menu factory methods
	//
	
	public AddElementInteractor withContextMenuInteractor(ContextMenuElementInteractor menuFactory)
	{
		return withElementInteractor( menuFactory );
	}
	
	
	
	
	//
	// Value methods
	//
	
	public SetFixedValue withFixedValue(Object value)
	{
		return new SetFixedValue( this, value );
	}
	
	public SetValueFunction withValueFunction(ElementValueFunction valueFn)
	{
		return new SetValueFunction( this, valueFn );
	}
	
	
	
	
	//
	//
	// Property methods
	//
	//
	
	public SetProperty withProperty(Object key, Object value)
	{
		return new SetProperty( key, value, this );
	}
	
	public RemoveProperty withoutProperty(Object key)
	{
		return new RemoveProperty( key, this );
	}
	
	
	
	
	//
	//
	// Custom action methods
	//
	//
	
	public CustomElementActionPres withCustomElementAction(CustomElementActionPres.CustomElementAction action)
	{
		return new CustomElementActionPres( action, this );
	}
	
	
	
	
	//
	// Debug name methods
	//
	
	public SetDebugName setDebugName(String debugName)
	{
		return new SetDebugName( this, debugName );
	}
	
	
	
	
	//
	// Style sheet from attribute methods
	//
	
	public ApplyStyleSheetFromAttribute withStyleSheetFromAttr(AttributeBase attribute)
	{
		return new ApplyStyleSheetFromAttribute( attribute, this );
	}
	
	
	
	//
	// Popup methods
	//
	
	private LSElement createPopupElement(LSElement contextElement, StyleValues style)
	{
		PresentationContext presCtx = PresentationContext.defaultCtx;
		FragmentContext fragCtx = contextElement.getFragmentContext();
		IncrementalView parentView = null;
		Subject enclosingSubject = null;
		
		if ( fragCtx != null )
		{
			presCtx = fragCtx.createPresentationContext();
			if ( style == null )
			{
				//style = fragCtx.getStyleValues();
				style = StyleValues.getRootStyle();
			}
			
			if ( fragCtx instanceof FragmentView )
			{
				FragmentView fragView = (FragmentView)fragCtx;
				
				parentView = fragView.getView();
				enclosingSubject = fragView.getSubject();
			}
		}
		
		PopupSubject popupSubject = new PopupSubject( enclosingSubject, this, presCtx.getPerspective() );
		IncrementalView popupView = new IncrementalView( popupSubject, parentView );
		
		Pres viewPres = popupView.getViewPres();
		
		if ( style == null )
		{
			style = StyleValues.getRootStyle();
		}

		return viewPres.present( presCtx, style );
	}
	
	private LSElement createPopupElement(LSElement contextElement)
	{
		return createPopupElement( contextElement, null );
	}
	
	
	
	public PresentationPopupWindow popup(LSElement element, Anchor targetAnchor, Anchor popupAnchor, boolean closeAutomatically, boolean requestFocus)
	{
		LSElement popupElement = createPopupElement( element );
		return popupElement.popup( element, targetAnchor, popupAnchor, closeAutomatically, requestFocus );
	}
	
	public PresentationPopupWindow popup(LSElement element, StyleValues style, Anchor targetAnchor, Anchor popupAnchor, boolean closeAutomatically, boolean requestFocus)
	{
		LSElement popupElement = createPopupElement( element, style );
		return popupElement.popup( element, targetAnchor, popupAnchor, closeAutomatically, requestFocus );
	}
	
	public PresentationPopupWindow popupAtMousePosition(LSElement element, Anchor popupAnchor, boolean closeAutomatically, boolean requestFocus)
	{
		LSElement popupElement = createPopupElement( element );
		return element.getRootElement().createPopupAtMousePosition( popupElement, popupAnchor, closeAutomatically, requestFocus );
	}
	
	public PresentationPopupWindow popupAtMousePosition(LSElement element, StyleValues style, Anchor popupAnchor, boolean closeAutomatically, boolean requestFocus)
	{
		LSElement popupElement = createPopupElement( element, style );
		return element.getRootElement().createPopupAtMousePosition( popupElement, popupAnchor, closeAutomatically, requestFocus );
	}
	
	public PresentationPopupWindow popupAtMousePosition(LSElement element, boolean closeAutomatically, boolean requestFocus)
	{
		return popupAtMousePosition( element, Anchor.TOP_LEFT, closeAutomatically, requestFocus );
	}
	
	public PresentationPopupWindow popupAtMousePosition(LSElement element, StyleValues style, boolean closeAutomatically, boolean requestFocus)
	{
		return popupAtMousePosition( element, style, Anchor.TOP_LEFT, closeAutomatically, requestFocus );
	}
	
	
	public PresentationPopupWindow chainPopup(LSElement element, Anchor targetAnchor, Anchor popupAnchor, boolean closeAutomatically, boolean requestFocus)
	{
		LSElement popupElement = createPopupElement( element );
		return popupElement.chainPopup( element, targetAnchor, popupAnchor, closeAutomatically, requestFocus );
	}
	
	public PresentationPopupWindow chainPopup(LSElement element, StyleValues style, Anchor targetAnchor, Anchor popupAnchor, boolean closeAutomatically, boolean requestFocus)
	{
		LSElement popupElement = createPopupElement( element, style );
		return popupElement.chainPopup( element, targetAnchor, popupAnchor, closeAutomatically, requestFocus );
	}
	
	public PresentationPopupWindow chainPopupAtMousePosition(LSElement element, Anchor popupAnchor, boolean closeAutomatically, boolean requestFocus)
	{
		LSElement popupElement = createPopupElement( element );
		return element.getRootElement().createChainPopupAtMousePosition( popupElement, popupAnchor, closeAutomatically, requestFocus );
	}
	
	public PresentationPopupWindow chainPopupAtMousePosition(LSElement element, StyleValues style, Anchor popupAnchor, boolean closeAutomatically, boolean requestFocus)
	{
		LSElement popupElement = createPopupElement( element, style );
		return element.getRootElement().createChainPopupAtMousePosition( popupElement, popupAnchor, closeAutomatically, requestFocus );
	}
	
	public PresentationPopupWindow chainPopupAtMousePosition(LSElement element, boolean closeAutomatically, boolean requestFocus)
	{
		return chainPopupAtMousePosition( element, Anchor.TOP_LEFT, closeAutomatically, requestFocus );
	}
	
	public PresentationPopupWindow chainPopupAtMousePosition(LSElement element, StyleValues style, boolean closeAutomatically, boolean requestFocus)
	{
		return chainPopupAtMousePosition( element, style, Anchor.TOP_LEFT, closeAutomatically, requestFocus );
	}
	
	
	
	
	
	protected static LSElement[] mapPresent(PresentationContext ctx, StyleValues style, Pres children[])
	{
		LSElement result[] = new LSElement[children.length];
		for (int i = 0; i < children.length; i++)
		{
			result[i] = children[i].present( ctx, style );
		}
		return result;
	}
	
	protected static LSElement[] mapPresent(PresentationContext ctx, StyleValues style, List<Pres> children)
	{
		LSElement result[] = new LSElement[children.size()];
		int i = 0;
		for (Pres child: children)
		{
			result[i++] = child.present( ctx, style );
		}
		return result;
	}
	
	
	protected static PresentElement presentAsCombinator(PresentationContext ctx, StyleValues style, Pres child)
	{
		return new PresentElement( child.present( ctx, style ) );
	}
	
	
	
	public static Pres coerce(Object x)
	{
		if ( x == null )
		{
			throw new IllegalArgumentException("Cannot convert null to a presentable value");
		}
		else if ( x instanceof Pres )
		{
			return (Pres)x;
		}
		else if ( x instanceof LSElement )
		{
			return new PresentElement( (LSElement)x );
		}
		else
		{
			return new InnerFragment( x );
		}
	}

	public static Pres coerceNullable(Object x)
	{
		if ( x == null )
		{
			return null;
		}
		else if ( x instanceof Pres )
		{
			return (Pres)x;
		}
		else if ( x instanceof LSElement )
		{
			return new PresentElement( (LSElement)x );
		}
		else
		{
			return new InnerFragment( x );
		}
	}

	public static Pres coercePresentingNull(Object x)
	{
		if ( x == null )
		{
			return new InnerFragment( null );
		}
		else if ( x instanceof Pres )
		{
			return (Pres)x;
		}
		else if ( x instanceof LSElement )
		{
			return new PresentElement( (LSElement)x );
		}
		else
		{
			return new InnerFragment( x );
		}
	}

	public static Pres elementToPres(LSElement e)
	{
		return new PresentElement( e );
	}


	public static Pres[] mapCoerce(Object children[])
	{
		Pres result[] = new Pres[children.length];
		for (int i = 0; i < children.length; i++)
		{
			result[i] = coerce( children[i] );
		}
		return result;
	}
	
	public static Pres[] mapCoerce(List<?> children)
	{
		Pres result[] = new Pres[children.size()];
		for (int i = 0; i < children.size(); i++)
		{
			result[i] = coerce( children.get( i ) );
		}
		return result;
	}


	public static Pres[] mapCoerceNullable(Object children[])
	{
		Pres result[] = new Pres[children.length];
		for (int i = 0; i < children.length; i++)
		{
			result[i] = coerceNullable( children[i] );
		}
		return result;
	}

	public static Pres[] mapCoerceNullable(List<?> children)
	{
		Pres result[] = new Pres[children.size()];
		for (int i = 0; i < children.size(); i++)
		{
			result[i] = coerceNullable( children.get( i ) );
		}
		return result;
	}


	public static Pres[] mapCoercePresentingNull(Object children[])
	{
		Pres result[] = new Pres[children.length];
		for (int i = 0; i < children.length; i++)
		{
			result[i] = coercePresentingNull(children[i]);
		}
		return result;
	}
	

	public static Pres[] mapCoercePresentingNull(List<?> children)
	{
		Pres result[] = new Pres[children.size()];
		for (int i = 0; i < children.size(); i++)
		{
			result[i] = coercePresentingNull(children.get(i));
		}
		return result;
	}
}
