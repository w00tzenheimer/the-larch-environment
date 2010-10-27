//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Arrow;
import BritefuryJ.DocPresent.Combinators.Primitive.Bin;
import BritefuryJ.DocPresent.Combinators.Primitive.Column;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Row;
import BritefuryJ.DocPresent.Event.AbstractPointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerButtonClickedEvent;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Interactor.ClickElementInteractor;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class DropDownExpander extends Expander
{
	protected static class DropDownExpanderInteractor implements ClickElementInteractor
	{
		ExpanderControl control;
		
		protected DropDownExpanderInteractor()
		{
		}
		
		
		@Override
		public boolean testClickEvent(PointerInputElement element, AbstractPointerButtonEvent event)
		{
			return event.getButton() == 1;
		}

		@Override
		public boolean buttonClicked(PointerInputElement element, PointerButtonClickedEvent event)
		{
			control.toggle();
			return true;
		}
	}



	private Pres contents;
	private Pres header;

	
	public DropDownExpander(Object header, Object contents, boolean initialState, ExpanderListener listener)
	{
		super( initialState, listener );
		
		this.header = coerce( header );
		this.contents = coerce( contents );
	}
	
	public DropDownExpander(Object header, Object contents, boolean initialState)
	{
		this( header, contents, initialState, null );
	}
	
	public DropDownExpander(Object header, Object contents, ExpanderListener listener)
	{
		this( header, contents, false, listener );
	}
	
	public DropDownExpander(Object header, Object contents)
	{
		this( header, contents, false, null );
	}



	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		StyleValues usedStyle = Controls.useDropDownExpanderAttrs( style );
		
		StyleSheet arrowStyle = StyleSheet.instance.withAttr( Primitive.shapePainter, style.get( Controls.dropDownExpanderHeaderArrowPainter, Painter.class ) );
		double arrowSize = style.get( Controls.dropDownExpanderHeaderArrowSize, Double.class );
		double padding = style.get( Controls.dropDownExpanderPadding, Double.class );
		Pres expandedArrow = arrowStyle.applyTo( new Arrow( Arrow.Direction.DOWN, arrowSize ) );
		Pres contractedArrow = arrowStyle.applyTo( new Arrow( Arrow.Direction.RIGHT, arrowSize ) );
		
		DropDownExpanderInteractor headerInteractor = new DropDownExpanderInteractor();
		
		StyleSheet headerStyle = StyleSheet.instance.withAttr( Primitive.rowSpacing, style.get( Controls.dropDownExpanderHeaderContentsSpacing, Double.class ) );
		
		Pres expandedHeader = headerStyle.applyTo( new Row( new Pres[] { expandedArrow.alignVCentre(), header.alignHExpand() } ) ).withElementInteractor( headerInteractor );
		Pres contractedHeader = headerStyle.applyTo( new Row( new Pres[] { contractedArrow.alignVCentre(), header.alignHExpand() } ) ).withElementInteractor( headerInteractor );
		
		
		Pres expanded = new Column( new Pres[] { expandedHeader, contents.padX( padding ) } );
		Pres contracted = contractedHeader;
		
		Pres expander = new Bin( initialState  ?  expanded  :  contracted );
		DPBin expanderElement = (DPBin)expander.present( ctx, usedStyle );
		
		ExpanderControl control = new ExpanderControl( ctx, usedStyle, expanderElement, expanded, contracted, initialState, listener );
		headerInteractor.control = control;
		return control;
	}
}