//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JWindow;

import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.LSpace.Util.WindowTransparency;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public class PresentationPopupWindow
{
	protected JWindow popupWindow;
	protected PresentationComponent popupComponent;
	protected PopupChain chain;
	private boolean open, chainStart;
	private Point2 screenPosition;
	private Vector2 screenSize;
	
	protected PresentationPopupWindow(PopupChain popupChain, Window ownerWindow, PresentationComponent parentComponent, LSElement popupContents, int screenX, int screenY,
			Anchor popupAnchor, boolean closeOnLoseFocus, boolean requestFocus, boolean chainStart)
	{
		chain = popupChain;
		chain.addPopup( this );
		open = true;
		this.chainStart = chainStart;
		
		// Create the popup window
		popupWindow = new JWindow( ownerWindow );
		if ( requestFocus )
		{
			popupWindow.setAlwaysOnTop( true );
			popupWindow.setFocusable( true );
		}
		popupWindow.getContentPane().setBackground( Color.WHITE );
		
		// Create a presentation component for the popup contents, and add them
		popupComponent = new PopupPresentationComponent( this );
		popupComponent.setPageController( parentComponent.getPageController() );
		popupComponent.getRootElement().setChild( popupContents.layoutWrap( HAlignment.EXPAND, VAlignment.EXPAND ) );
		
		popupWindow.add( popupComponent );
		
		
		popupWindow.pack();
		Dimension sz = popupWindow.getSize();
		Point2 windowAnchor = new Point2( sz.getWidth() * popupAnchor.getPropX(), sz.getHeight() * popupAnchor.getPropY() );
		screenX -= (int)( windowAnchor.x + 0.5 );
		screenY -= (int)( windowAnchor.y + 0.5 );
		
		// Ensure >= 0
		screenX = Math.max( screenX, 0 );
		screenY = Math.max( screenY, 0 );
		
		// Ensure that it is not offscreen due to width/height
		Dimension screenSz = Toolkit.getDefaultToolkit().getScreenSize();
		screenX = Math.min( screenX, (int)( screenSz.getWidth() - sz.getWidth() ) );
		screenY = Math.min( screenY, (int)( screenSz.getHeight() - sz.getHeight() ) );

		popupWindow.setLocation( screenX, screenY );
		
		screenPosition = new Point2( screenX, screenY );
		screenSize = new Vector2( sz.getWidth(), sz.getHeight() );

		
		popupWindow.setVisible( true );
		if ( requestFocus )
		{
			popupWindow.requestFocus();
		}
		
		
		WindowTransparency.setWindowOpaque( popupWindow, false );

	
		WindowFocusListener focusListener = new WindowFocusListener()
		{
			public void windowGainedFocus(WindowEvent arg0)
			{
			}

			public void windowLostFocus(WindowEvent arg0)
			{
				// If the popup has no child
				if ( open )
				{
					if ( !chain.popupHasChild( PresentationPopupWindow.this ) )
					{
						chain.closeChainNotContainingPointers();
					}
				}
			}
		};
		
		if ( closeOnLoseFocus )
		{
			popupWindow.addWindowFocusListener( focusListener );
		}
	}
	
	
	public void closePopup()
	{
		open = false;
		popupWindow.setVisible( false );
	}
	
	
	public void closeContainingPopupChain()
	{
		chain.closeChain();
	}
	
	
	public PresentationComponent getPresentationComponent()
	{
		return popupComponent;
	}
	
	
	public Point2 getPositionOnScreen()
	{
		return screenPosition;
	}
	
	public Vector2 getSizeOnScreen()
	{
		return screenSize;
	}
	
	
	public boolean isChainStart()
	{
		return chainStart;
	}
}