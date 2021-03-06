//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.Graphics.Painter;
import BritefuryJ.LSpace.Anchor;
import BritefuryJ.LSpace.LSBin;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Event.AbstractPointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Interactor.ClickElementInteractor;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Arrow;
import BritefuryJ.Pres.Primitive.Bin;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class MenuItem extends ControlPres
{
	public enum SubmenuPopupDirection
	{
		RIGHT,
		DOWN
	}
	
	public interface MenuItemListener
	{
		public void onMenuItemClicked(MenuItemControl menuItem);
	}
	
	
	public static class MenuItemControl extends Control
	{
		private class SubMenuItemListener implements MenuItemListener
		{
			private Pres subMenu;
			private SubmenuPopupDirection direction;
			
			public SubMenuItemListener(Pres subMenu, SubmenuPopupDirection direction)
			{
				this.subMenu = subMenu;
				this.direction = direction;
			}
			
			
			@Override
			public void onMenuItemClicked(MenuItemControl menuItem)
			{
				if ( direction == SubmenuPopupDirection.RIGHT )
				{
					subMenu.chainPopup( menuItem.getElement(), style, Anchor.TOP_RIGHT, Anchor.TOP_LEFT, true, true );
				}
				else
				{
					subMenu.chainPopup( menuItem.getElement(), style, Anchor.BOTTOM_LEFT, Anchor.TOP_LEFT, true, true );
				}
			}
		}
		
	
		private class MenuItemInteractor implements ClickElementInteractor
		{
			public MenuItemInteractor()
			{	
			}
			
			@Override
			public boolean testClickEvent(LSElement element, AbstractPointerButtonEvent event)
			{
				return event.getButton() == 1;
			}

			@Override
			public boolean buttonClicked(LSElement element, PointerButtonClickedEvent event)
			{
				if ( element.isRealised() )
				{
					if ( bClosePopupOnActivate )
					{
						element.closeContainingPopupChain();
					}
					listener.onMenuItemClicked( MenuItemControl.this );
					return true;
				}
				
				return false;
			}
		}
	
		
		
		private LSBin element;
		private MenuItemListener listener;
		private boolean bClosePopupOnActivate;
		
		
		protected MenuItemControl(PresentationContext ctx, StyleValues style, LSBin element, MenuItemListener listener, boolean bClosePopupOnActivate)
		{
			super( ctx, style );
			this.element = element;
			this.listener = listener;
			this.element.addElementInteractor( new MenuItemInteractor() );
			this.bClosePopupOnActivate = bClosePopupOnActivate;
		}
		
		protected MenuItemControl(PresentationContext ctx, StyleValues style, LSBin element, Pres subMenu, SubmenuPopupDirection direction, boolean bClosePopupOnActivate)
		{
			super( ctx, style );
			this.element = element;
			this.listener = new SubMenuItemListener( subMenu, direction );
			this.element.addElementInteractor( new MenuItemInteractor() );
			this.bClosePopupOnActivate = bClosePopupOnActivate;
		}
		
		
		public LSElement getElement()
		{
			return element;
		}
	}

	private Pres child;
	private MenuItemListener listener;
	private Pres subMenu;
	private SubmenuPopupDirection direction;
	
	
	public MenuItem(Object child, MenuItemListener listener)
	{
		this.child = coerce( child );
		this.listener = listener;
	}
	
	public MenuItem(Object child, Object subMenu, SubmenuPopupDirection direction)
	{
		this.child = coerce( child );
		this.subMenu = Pres.coerce(subMenu);
		this.direction = direction;
	}
	
	
	public static MenuItem menuItemWithLabel(String labelText, MenuItemListener listener)
	{
		return new MenuItem( new Label( labelText ), listener );
	}
	
	public static MenuItem menuItemWithLabel(String labelText, Object subMenu, SubmenuPopupDirection direction)
	{
		return new MenuItem( new Label( labelText ), subMenu, direction );
	}
	
	

	
	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		StyleValues usedStyle = Controls.useMenuItemAttrs( style );
		Pres childElem;
		if ( subMenu != null )
		{
			double spacing = style.get( Controls.menuItemSubmenuArrowSpacing, Double.class );
			double arrowSize = style.get( Controls.menuItemSubmenuArrowSize, Double.class );
			Painter arrowPainter = style.get( Controls.menuItemSubmenuArrowPainter, Painter.class );
			Arrow.Direction arrowDirection;
			if ( direction == SubmenuPopupDirection.RIGHT )
			{
				arrowDirection = Arrow.Direction.RIGHT;
			}
			else if ( direction == SubmenuPopupDirection.DOWN )
			{
				arrowDirection = Arrow.Direction.DOWN;
			}
			else
			{
				throw new RuntimeException( "Invalid submenu popup direction" );
			}
			Pres childWithArrow = StyleSheet.style( Primitive.rowSpacing.as( spacing ) ).applyTo(
				    new Row( new Pres[] { child.alignHExpand().alignVRefYExpand(),
						StyleSheet.style( Primitive.shapePainter.as( arrowPainter ) ).applyTo( new Arrow( arrowDirection, arrowSize ).alignHPack().alignVCentre() ) } ) ).alignHExpand();
			childElem = presentAsCombinator( ctx, usedStyle, childWithArrow );
		}
		else
		{
			childElem = presentAsCombinator( ctx, usedStyle, child );
		}
		
		Painter hoverBackground = style.get( Controls.menuItemHoverBackground, Painter.class );
		double padX = style.get( Controls.menuItemXPadding, Double.class );
		double padY = style.get( Controls.menuItemYPadding, Double.class );
		boolean bClosePopupOnActivate = style.get( Controls.bClosePopupOnActivate, Boolean.class );

		StyleValues menuItemStyle = style.withAttr( Primitive.hoverBackground, hoverBackground );
		Pres menuItem = new Bin( childElem.pad( padX, padY ) ).alignHExpand();

		LSBin element = (LSBin)menuItem.present( ctx, menuItemStyle );
		
		
		if ( subMenu != null )
		{
			return new MenuItemControl( ctx, style, element, subMenu, direction, false );
		}
		else
		{
			return new MenuItemControl( ctx, style, element, listener, bClosePopupOnActivate );
		}
	}
}
