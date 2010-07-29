//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Paint;

import BritefuryJ.AttributeTable.AttributeTable2;
import BritefuryJ.AttributeTable.DerivedValueTable;
import BritefuryJ.AttributeTable.InheritedAttribute;
import BritefuryJ.AttributeTable.InheritedAttributeNonNull;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.FilledBorder;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.Painter.FilledOutlinePainter;
import BritefuryJ.DocPresent.Painter.OutlinePainter;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.DocPresent.StyleSheet.StyleSheetValues;

public class Controls
{
	public static final InheritedAttributeNonNull buttonBorder = new InheritedAttributeNonNull( "controls", "buttonBorder", Border.class,
			new SolidBorder( 1.0, 3.0, 10.0, 10.0, new Color( 0.55f, 0.525f, 0.5f ), new Color( 0.85f, 0.85f, 0.85f ) ) );
	public static final InheritedAttributeNonNull buttonHighlightBorder = new InheritedAttributeNonNull( "controls", "buttonBorder", Border.class,
			new SolidBorder( 1.0, 3.0, 10.0, 10.0, new Color( 0.0f, 0.5f, 0.5f ), new Color( 0.925f, 0.925f, 0.925f ) ) );
	
	
	public static final InheritedAttributeNonNull hyperlinkAttrs = new InheritedAttributeNonNull( "controls", "hyperlinkAttrs", StyleSheet2.class,
			StyleSheet2.instance.withAttr( Primitive.editable, false ).withAttr( Primitive.fontFace, "Sans serif" ).withAttr( Primitive.foreground, Color.blue )
			.withAttr( Primitive.hoverForeground, Color.red ).withAttr( Primitive.cursor, new Cursor( Cursor.HAND_CURSOR ) ) );

	
	public static final InheritedAttributeNonNull checkboxHoverBackground = new InheritedAttributeNonNull( "controls", "checkboxHoverBackground", Painter.class,
			new OutlinePainter( new Color( 0.5f, 0.625f, 0.75f ) ) );
	public static final InheritedAttributeNonNull checkboxCheckBorder = new InheritedAttributeNonNull( "controls", "checkboxCheckBorder", Border.class,
			new FilledBorder( 3.0, 3.0, 3.0, 3.0, 5.0, 5.0, new Color( 0.75f, 0.75f, 0.75f ) ) );
	public static final InheritedAttributeNonNull checkboxCheckForeground = new InheritedAttributeNonNull( "controls", "checkboxCheckForeground", Paint.class, new Color( 0.0f, 0.2f, 0.4f ) );
	public static final InheritedAttributeNonNull checkboxCheckSize = new InheritedAttributeNonNull( "controls", "checkboxCheckSize", Double.class, 10.0 );
	public static final InheritedAttributeNonNull checkboxSpacing = new InheritedAttributeNonNull( "controls", "checkboxSpacing", Double.class, 8.0 );

	
	public static final InheritedAttributeNonNull optionMenuBorder = new InheritedAttributeNonNull( "controls", "optionMenuBorder", Border.class,
			new SolidBorder( 1.0, 3.0, 5.0, 5.0, Color.BLACK, new Color( 0.9f, 0.95f, 0.9f ) ) );
	public static final InheritedAttributeNonNull optionMenuHoverBorder = new InheritedAttributeNonNull( "controls", "optionMenuHoverBorder", Border.class,
			new SolidBorder( 1.0, 3.0, 5.0, 5.0, new Color( 0.0f, 0.3f, 0.0f ), new Color( 0.95f, 1.0f, 0.95f ) ) );
	public static final InheritedAttributeNonNull optionMenuContentsSpacing = new InheritedAttributeNonNull( "controls", "optionMenuContentsSpacing", Double.class, 5.0 );
	public static final InheritedAttributeNonNull optionMenuArrowSize = new InheritedAttributeNonNull( "controls", "optionMenuArrowSize", Double.class, 16.0 );
	public static final InheritedAttributeNonNull optionMenuArrowPainter = new InheritedAttributeNonNull( "controls", "optionMenuArrowPainter", Painter.class,
			new FilledOutlinePainter( new Color( 0.7f, 0.85f, 0.7f ), new Color( 0.0f, 0.25f, 0.0f ), new BasicStroke( 1.0f ) ) );

	
	public static final InheritedAttributeNonNull textEntryBorder = new InheritedAttributeNonNull( "controls", "textEntryBorder", Border.class,
			new SolidBorder( 1.0, 3.0, 5.0, 5.0, new Color( 0.0f, 0.3f, 0.0f ), new Color( 0.9f, 0.95f, 0.9f ) ) );
	public static final InheritedAttributeNonNull textEntryInvalidBorder = new InheritedAttributeNonNull( "controls", "textEntryInvalidBorder", Border.class,
			new SolidBorder( 1.0, 3.0, 5.0, 5.0, new Color( 0.6f, 0.0f, 0.0f ), new Color( 1.0f, 0.85f, 0.85f ) ) );

	
	public static final InheritedAttributeNonNull textAreaAttrs = new InheritedAttributeNonNull( "controls", "textAreaAttrs", StyleSheet2.class,
			StyleSheet2.instance.withAttr( Primitive.border, new SolidBorder( 2.0, 5.0, 3.0, 3.0, new Color( 0.3f, 0.3f, 0.3f ), null ) ) );

	
	public static final InheritedAttributeNonNull spinEntryArrowSize = new InheritedAttributeNonNull( "controls", "spinEntryArrowSize", Double.class, 16.0 );
	public static final InheritedAttributeNonNull spinEntryArrowAttrs = new InheritedAttributeNonNull( "controls", "spinEntryArrowAttrs", StyleSheet2.class,
			StyleSheet2.instance.withAttr( Primitive.vboxSpacing, 2.0 ).
			withAttr( Primitive.shapePainter, new FilledOutlinePainter( new Color( 0.7f, 0.85f, 0.7f ), new Color( 0.0f, 0.25f, 0.0f ), new BasicStroke( 1.0f ) ) ).
			withAttr( Primitive.hoverShapePainter, new FilledOutlinePainter( new Color( 0.85f, 1.0f, 0.85f ), new Color( 0.0f, 0.45f, 0.0f ), new BasicStroke( 1.0f ) ) ) );
	public static final InheritedAttributeNonNull spinEntryHSpacing = new InheritedAttributeNonNull( "controls", "spinEntryHSpacing", Double.class, 2.0 );
	
	
	public static final InheritedAttributeNonNull scrollBarArrowPainter = new InheritedAttributeNonNull( "controls", "scrollBarArrowPainter", Painter.class,
			new FilledOutlinePainter( new Color( 0.7f, 0.85f, 1.0f ), new Color( 0.0f, 0.5f, 1.0f ), new BasicStroke( 1.0f ) ) );
	public static final InheritedAttributeNonNull scrollBarDragBoxPainter = new InheritedAttributeNonNull( "controls", "scrollBarDragBoxPainter", Painter.class,
			new FilledOutlinePainter( new Color( 0.7f, 0.85f, 1.0f ), new Color( 0.0f, 0.5f, 1.0f ), new BasicStroke( 1.0f ) ) );
	public static final InheritedAttributeNonNull scrollBarDragBarBackgroundPainter = new InheritedAttributeNonNull( "controls", "scrollBarDragBarBackgroundPainter", Painter.class,
			new FilledOutlinePainter( new Color( 0.9f, 0.9f, 0.9f ), new Color( 0.75f, 0.75f, 0.75f ), new BasicStroke( 1.0f ) ) );
	public static final InheritedAttribute scrollBarDragBackgroundHoverPainter = new InheritedAttribute( "controls", "scrollBarDragBackgroundHoverPainter", Painter.class, null );
	public static final InheritedAttributeNonNull scrollBarArrowPadding = new InheritedAttributeNonNull( "controls", "scrollBarArrowPainter", Double.class, 0.0 );
	public static final InheritedAttributeNonNull scrollBarArrowSpacing = new InheritedAttributeNonNull( "controls", "scrollBarArrowPainter", Double.class, 2.0 );
	public static final InheritedAttributeNonNull scrollBarArrowDragboxPadding = new InheritedAttributeNonNull( "controls", "scrollBarArrowPainter", Double.class, 3.0 );
	public static final InheritedAttributeNonNull scrollBarArrowDragboxRounding = new InheritedAttributeNonNull( "controls", "scrollBarArrowPainter", Double.class, 4.0 );
	public static final InheritedAttributeNonNull scrollBarSize = new InheritedAttributeNonNull( "controls", "scrollBarArrowPainter", Double.class, 20.0 );

	
	public static final InheritedAttributeNonNull bClosePopupOnActivate = new InheritedAttributeNonNull( "controls", "bClosePopupOnActivate", Boolean.class, false );
	
	public static final InheritedAttributeNonNull menuItemHoverBackground = new InheritedAttributeNonNull( "controls", "menuItemHoverBackground", Painter.class,
			new FillPainter( new Color( 0.6f, 0.7f, 0.85f ) ) );
	public static final InheritedAttributeNonNull menuItemXPadding = new InheritedAttributeNonNull( "controls", "menuItemXPadding", Double.class, 5.0 );
	public static final InheritedAttributeNonNull menuItemYPadding = new InheritedAttributeNonNull( "controls", "menuItemYPadding", Double.class, 5.0 );
	public static final InheritedAttributeNonNull popupMenuAttrs = new InheritedAttributeNonNull( "controls", "popupMenuAttrs", StyleSheet2.class,
			StyleSheet2.instance.withAttr( Primitive.border, new SolidBorder( 1.0, 2.0, Color.black, null ) ).withAttr( Primitive.hboxSpacing, 10.0 ) );

	
	public static final InheritedAttributeNonNull tooltipBorder = new InheritedAttributeNonNull( "controls", "tooltipBorder", Border.class,
			new SolidBorder( 1.0, 2.0, 2.0, 2.0, Color.BLACK, new Color( 1.0f, 1.0f, 0.9f ) ) );
	
	
	
	
	
	public static StyleSheetValues useButtonAttrs(StyleSheetValues style)
	{
		return style.useAttr( buttonBorder ).useAttr( buttonHighlightBorder );
	}
	
	public static PresentationContext useButtonAttrs(PresentationContext ctx)
	{
		return ctx.withStyle( useButtonAttrs( ctx.getStyle() ) );
	}

	
	public static StyleSheetValues hyperlinkStyle(StyleSheetValues style)
	{
		return style.withAttrs( style.get( hyperlinkAttrs, StyleSheet2.class ) );
	}
	
	public static StyleSheetValues useHyperlinkAttrs(StyleSheetValues style)
	{
		return style.useAttr( hyperlinkAttrs );
	}
	
	public static PresentationContext useHyperlinkAttrs(PresentationContext ctx)
	{
		return ctx.withStyle( useHyperlinkAttrs( ctx.getStyle() ) );
	}
	
	
	protected static DerivedValueTable<StyleSheet2> checkboxStyle = new DerivedValueTable<StyleSheet2>()
	{
		protected StyleSheet2 evaluate(AttributeTable2 attribs)
		{
			double spacing = attribs.get( checkboxSpacing, Double.class );
			Painter background = attribs.get( checkboxHoverBackground, Painter.class );
			return StyleSheet2.instance.withAttr( Primitive.hoverBackground, background ).withAttr( Primitive.hboxSpacing, spacing );
		}
	};

	public static StyleSheetValues useCheckboxAttrs(StyleSheetValues style)
	{
		return style.useAttr( checkboxHoverBackground ).useAttr( checkboxCheckBorder ).useAttr( checkboxCheckForeground ).useAttr( checkboxCheckSize ).useAttr( checkboxSpacing );
	}
	
	public static PresentationContext useCheckboxAttrs(PresentationContext ctx)
	{
		return ctx.withStyle( useCheckboxAttrs( ctx.getStyle() ) );
	}

	

	public static StyleSheetValues useOptionMenuAttrs(StyleSheetValues style)
	{
		return style.useAttr( optionMenuBorder ).useAttr( optionMenuHoverBorder ).useAttr( optionMenuContentsSpacing ).useAttr( optionMenuArrowSize ).useAttr( optionMenuArrowPainter );
	}
	
	public static PresentationContext useOptionMenuAttrs(PresentationContext ctx)
	{
		return ctx.withStyle( useOptionMenuAttrs( ctx.getStyle() ) );
	}

	
	
	public static StyleSheetValues useTextEntryAttrs(StyleSheetValues style)
	{
		return style.useAttr( textEntryBorder ).useAttr( textEntryInvalidBorder );
	}
	
	public static PresentationContext useTextEntryAttrs(PresentationContext ctx)
	{
		return ctx.withStyle( useTextEntryAttrs( ctx.getStyle() ) );
	}

	
	
	protected static DerivedValueTable<StyleSheet2> scrollBarDragBoxStyle = new DerivedValueTable<StyleSheet2>()
	{
		protected StyleSheet2 evaluate(AttributeTable2 attribs)
		{
			Painter backgroundPainter = attribs.get( scrollBarDragBarBackgroundPainter, Painter.class );
			Painter backgroundHoverPainter = attribs.get( scrollBarDragBackgroundHoverPainter, Painter.class );
			return StyleSheet2.instance.withAttr( Primitive.shapePainter, backgroundPainter ).withAttr( Primitive.hoverShapePainter, backgroundHoverPainter );
		}
	};


	
	public static StyleSheetValues useMenuItemAttrs(StyleSheetValues style)
	{
		return style.useAttr( checkboxHoverBackground ).useAttr( checkboxCheckBorder ).useAttr( checkboxCheckForeground ).useAttr( checkboxCheckSize ).useAttr( checkboxSpacing );
	}
	
	public static PresentationContext useMenuItemAttrs(PresentationContext ctx)
	{
		return ctx.withStyle( useMenuItemAttrs( ctx.getStyle() ) );
	}

	
	public static StyleSheetValues popupMenuStyle(StyleSheetValues style)
	{
		return style.withAttrs( style.get( popupMenuAttrs, StyleSheet2.class ) );
	}
	
	public static StyleSheetValues usePopupMenuAttrs(StyleSheetValues style)
	{
		return style.useAttr( popupMenuAttrs );
	}

	public static PresentationContext usePopupMenuAttrs(PresentationContext ctx)
	{
		return ctx.withStyle( usePopupMenuAttrs( ctx.getStyle() ) );
	}




	public static StyleSheetValues useTooltipAttrs(StyleSheetValues style)
	{
		return style.useAttr( tooltipBorder );
	}

	public static PresentationContext useTooltipAttrs(PresentationContext ctx)
	{
		return ctx.withStyle( useTooltipAttrs( ctx.getStyle() ) );
	}
}