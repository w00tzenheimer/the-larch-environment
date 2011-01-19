//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres;

import java.awt.datatransfer.DataFlavor;
import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementPainter;
import BritefuryJ.DocPresent.ElementValueFunction;
import BritefuryJ.DocPresent.FragmentContext;
import BritefuryJ.DocPresent.TreeEventListener;
import BritefuryJ.DocPresent.Input.ObjectDndHandler;
import BritefuryJ.DocPresent.Interactor.AbstractElementInteractor;
import BritefuryJ.DocPresent.Interactor.ContextMenuElementInteractor;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.Math.Point2;
import BritefuryJ.StyleSheet.StyleValues;

public abstract class Pres
{
	public DPElement present()
	{
		return present( new PresentationContext(), StyleValues.instance );
	}

	public abstract DPElement present(PresentationContext ctx, StyleValues style);
	
	
	
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
	
	public AddDragSource withDragSource(Class<?> dataType, int sourceAspects, ObjectDndHandler.SourceDataFn sourceDataFn)
	{
		return new AddDragSource( this, dataType, sourceAspects, sourceDataFn );
	}
	
	
	public AddDropDest withDropDest(ObjectDndHandler.DropDest dest)
	{
		return new AddDropDest( this, dest );
	}
	
	public AddDropDest withDropDest(Class<?> dataType, ObjectDndHandler.CanDropFn canDropFn, ObjectDndHandler.DropFn dropFn)
	{
		return new AddDropDest( this, dataType, canDropFn, dropFn );
	}
	
	public AddDropDest withDropDest(Class<?> dataType, ObjectDndHandler.DropFn dropFn)
	{
		return new AddDropDest( this, dataType, dropFn );
	}
	
	
	public AddNonLocalDropDest withNonLocalDropDest(ObjectDndHandler.NonLocalDropDest dest)
	{
		return new AddNonLocalDropDest( this, dest );
	}
	
	public AddNonLocalDropDest withNonLocalDropDest(DataFlavor dataFlavor, ObjectDndHandler.DropFn dropFn)
	{
		return new AddNonLocalDropDest( this, dataFlavor, dropFn );
	}
	
	
	
	//
	// Interactor methods
	//
	
	public AddElementInteractor withElementInteractor(AbstractElementInteractor interactor)
	{
		return new AddElementInteractor( this, interactor );
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
	// Popup methods
	//
	
	public void popupToRightOf(DPElement element, PresentationContext ctx, StyleValues style, boolean bCloseOnLoseFocus, boolean bRequestFocus)
	{
		DPElement popupElement = present( ctx, style );
		popupElement.popupToRightOf( element, bCloseOnLoseFocus, bRequestFocus );
	}
	
	public void popupBelow(DPElement element, PresentationContext ctx, StyleValues style, boolean bCloseOnLoseFocus, boolean bRequestFocus)
	{
		DPElement popupElement = present( ctx, style );
		popupElement.popupBelow( element, bCloseOnLoseFocus, bRequestFocus );
	}
	
	public void popupOver(DPElement element, Point2 targetLocalPos, PresentationContext ctx, StyleValues style, boolean bCloseOnLoseFocus, boolean bRequestFocus)
	{
		DPElement popupElement = present( ctx, style );
		popupElement.popupOver( element, targetLocalPos, bCloseOnLoseFocus, bRequestFocus );
	}
	
	public void popupAtMousePosition(DPElement element, PresentationContext ctx, StyleValues style, boolean bCloseOnLoseFocus, boolean bRequestFocus)
	{
		DPElement popupElement = present( ctx, style );
		element.getRootElement().createPopupAtMousePosition( popupElement, bCloseOnLoseFocus, bRequestFocus );
	}
	

	
	
	
	public void popupToRightOf(DPElement element, boolean bCloseOnLoseFocus, boolean bRequestFocus)
	{
		FragmentContext ctx = element.getFragmentContext();
		if ( ctx != null )
		{
			popupToRightOf( element, ctx.createPresentationContext(), ctx.getStyleValues(), bCloseOnLoseFocus, bRequestFocus );
		}
		else
		{
			popupToRightOf( element, new PresentationContext(), StyleValues.instance, bCloseOnLoseFocus, bRequestFocus );
		}
	}
	
	public void popupBelow(DPElement element, boolean bCloseOnLoseFocus, boolean bRequestFocus)
	{
		FragmentContext ctx = element.getFragmentContext();
		if ( ctx != null )
		{
			popupBelow( element, ctx.createPresentationContext(), ctx.getStyleValues(), bCloseOnLoseFocus, bRequestFocus );
		}
		else
		{
			popupBelow( element, new PresentationContext(), StyleValues.instance, bCloseOnLoseFocus, bRequestFocus );
		}
	}
	
	public void popupOver(DPElement element, Point2 targetLocalPos, boolean bCloseOnLoseFocus, boolean bRequestFocus)
	{
		FragmentContext ctx = element.getFragmentContext();
		if ( ctx != null )
		{
			popupOver( element, targetLocalPos, ctx.createPresentationContext(), ctx.getStyleValues(), bCloseOnLoseFocus, bRequestFocus );
		}
		else
		{
			popupOver( element, targetLocalPos, new PresentationContext(), StyleValues.instance, bCloseOnLoseFocus, bRequestFocus );
		}
	}
	
	public void popupAtMousePosition(DPElement element, boolean bCloseOnLoseFocus, boolean bRequestFocus)
	{
		FragmentContext ctx = element.getFragmentContext();
		if ( ctx != null )
		{
			popupAtMousePosition( element, ctx.createPresentationContext(), ctx.getStyleValues(), bCloseOnLoseFocus, bRequestFocus );
		}
		else
		{
			popupAtMousePosition( element, new PresentationContext(), StyleValues.instance, bCloseOnLoseFocus, bRequestFocus );
		}
	}
	
	
	
	
	
	protected static DPElement[] mapPresent(PresentationContext ctx, StyleValues style, Pres children[])
	{
		DPElement result[] = new DPElement[children.length];
		for (int i = 0; i < children.length; i++)
		{
			result[i] = children[i].present( ctx, style );
		}
		return result;
	}
	
	protected static DPElement[] mapPresent(PresentationContext ctx, StyleValues style, List<Pres> children)
	{
		DPElement result[] = new DPElement[children.size()];
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
			return null;
		}
		else if ( x instanceof Pres )
		{
			return (Pres)x;
		}
		else if ( x instanceof DPElement )
		{
			return new PresentElement( (DPElement)x );
		}
		else
		{
			return new InnerFragment( x );
		}
	}

	public static Pres coerceNonNull(Object x)
	{
		if ( x == null )
		{
			return new InnerFragment( null );
		}
		else if ( x instanceof Pres )
		{
			return (Pres)x;
		}
		else if ( x instanceof DPElement )
		{
			return new PresentElement( (DPElement)x );
		}
		else
		{
			return new InnerFragment( x );
		}
	}

	public static Pres elementToPres(DPElement e)
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
}