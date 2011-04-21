//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.TransferHandler;

import BritefuryJ.CommandHistory.CommandHistoryController;
import BritefuryJ.CommandHistory.CommandHistoryListener;
import BritefuryJ.Controls.ScrolledViewport;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.PageController;
import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.Input.Keyboard.Keyboard;
import BritefuryJ.DocPresent.Input.Keyboard.KeyboardInteractor;
import BritefuryJ.DocPresent.PersistentState.PersistentState;
import BritefuryJ.DocPresent.PersistentState.PersistentStateStore;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.HiddenContent;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.StyleSheet.StyleValues;

public class Browser
{
	protected interface BrowserListener
	{
		public void onBrowserChangeTitle(Browser browser, String title);
	}
	
	
	private static final String COMMAND_BACK = "back";
	private static final String COMMAND_FORWARD = "forward";
	private static final String COMMAND_RELOAD = "reload";
	

	
	private JToolBar toolbar;

	private JTextField locationField;
	private JPanel panel;

	private PresentationComponent presComponent, commandBar;
	private ScrolledViewport.ScrolledViewportControl viewport;
	private BrowserHistory history;
	
	private PageLocationResolver resolver;
	private BrowserPage page;
	private BrowserListener listener;
	private CommandHistoryListener commandHistoryListener;
	
	
	
	
	public Browser(PageLocationResolver resolver, Location location, PageController pageController, boolean showCommandBar)
	{
		this.resolver = resolver;
		history = new BrowserHistory( location );
		
		viewport = makeViewport( new HiddenContent( "" ), history.getCurrentState().getViewportState() );
		
		presComponent = new PresentationComponent();
		presComponent.setPageController( pageController );
		presComponent.setChild( viewport.getElement() );
		
		ActionMap actionMap = presComponent.getActionMap();
		actionMap.put( TransferHandler.getCutAction().getValue( Action.NAME ), TransferHandler.getCutAction() );
		actionMap.put( TransferHandler.getCopyAction().getValue( Action.NAME ), TransferHandler.getCopyAction() );
		actionMap.put( TransferHandler.getPasteAction().getValue( Action.NAME ), TransferHandler.getPasteAction() );
		

		toolbar = new JToolBar();
		toolbar.setFloatable( false );
		toolbar.setRollover( true );
		initialiseToolbar( toolbar );
		
		
		locationField = new JTextField( location.getLocationString() );
		locationField.setMaximumSize( new Dimension( locationField.getMaximumSize().width, locationField.getMinimumSize().height ) );
		locationField.setBorder( BorderFactory.createLineBorder( Color.black, 1 ) );
		locationField.setDragEnabled( true );
		
		ActionListener locationActionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				onLocationField( locationField.getText() );
			}
		};
		
		locationField.addActionListener( locationActionListener );
		toolbar.add( locationField );
		
		
		JPanel header = new JPanel( new BorderLayout() );
		header.add( toolbar, BorderLayout.PAGE_START );
		
		
		panel = new JPanel( new BorderLayout() );
		panel.add( header, BorderLayout.PAGE_START );
		panel.add( presComponent, BorderLayout.CENTER );

		
		
		if ( showCommandBar )
		{
			commandBar = new PresentationComponent();
			commandBar.setPageController( pageController );
			commandBar.setChild( makeCommandElement( new Text( "" ) ) );
			
			JPanel commandPanel = new JPanel( new BorderLayout() );
			commandPanel.add( commandBar, BorderLayout.CENTER );
			commandPanel.setBorder( BorderFactory.createLineBorder( Color.black, 1 ) );
			
			JPanel footer = new JPanel( new BorderLayout( 5, 0 ) );
			footer.add( new JLabel( "Cmd:" ), BorderLayout.WEST );
			footer.add( commandPanel, BorderLayout.CENTER );
			footer.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );
			
			
			panel.add( footer, BorderLayout.PAGE_END );
			
			
			KeyboardInteractor presComponentSwitchInteractor = new KeyboardInteractor()
			{
				@Override
				public boolean keyPressed(Keyboard keyboard, KeyEvent event)
				{
					if ( event.getKeyCode() == KeyEvent.VK_ESCAPE )
					{
						return true;
					}
					return false;
				}

				@Override
				public boolean keyReleased(Keyboard keyboard, KeyEvent event)
				{
					if ( event.getKeyCode() == KeyEvent.VK_ESCAPE )
					{
						commandBar.grabFocus();
						return true;
					}
					return false;
				}

				@Override
				public boolean keyTyped(Keyboard keyboard, KeyEvent event)
				{
					return false;
				}
			};
			presComponentSwitchInteractor.addToKeyboard( presComponent.getRootElement().getKeyboard() );
		
		
			KeyboardInteractor commandBarSwitchInteractor = new KeyboardInteractor()
			{
				@Override
				public boolean keyPressed(Keyboard keyboard, KeyEvent event)
				{
					if ( event.getKeyCode() == KeyEvent.VK_ESCAPE )
					{
						return true;
					}
					return false;
				}

				@Override
				public boolean keyReleased(Keyboard keyboard, KeyEvent event)
				{
					if ( event.getKeyCode() == KeyEvent.VK_ESCAPE )
					{
						presComponent.grabFocus();
						return true;
					}
					return false;
				}

				@Override
				public boolean keyTyped(Keyboard keyboard, KeyEvent event)
				{
					return false;
				}
			};
			commandBarSwitchInteractor.addToKeyboard( commandBar.getRootElement().getKeyboard() );

		}
		
		resolve();
	}
	
	
	
	public JComponent getComponent()
	{
		return panel;
	}
	
	public String getTitle()
	{
		return page.getTitle();
	}
	
	public DPElement getRootElement()
	{
		return presComponent.getRootElement();
	}
	
	
	
	public Location getLocation()
	{
		return history.getCurrentState().getLocation();
	}
	
	public void goToLocation(Location location)
	{
		locationField.setText( location.getLocationString() );
		setLocation( location );
	}
	
	
	
	
	public void setListener(BrowserListener listener)
	{
		this.listener = listener;
	}
	
	
	
	public CommandHistoryController getCommandHistoryController()
	{
		if ( page != null )
		{
			return page.getCommandHistoryController();
		}
		else
		{
			return null;
		}
	}
	
	public void setCommandHistoryListener(CommandHistoryListener listener)
	{
		commandHistoryListener = listener;
		if ( page != null )
		{
			page.setCommandHistoryListener( listener );
		}
	}
	

	
	public void reset(Location location)
	{
		history.visit( location );
		history.clear();
		locationField.setText( location.getLocationString() );
		viewportReset();
		resolve();
	}
	
	
	public void viewportReset()
	{
		viewport.getViewportElement().resetXform();
	}

	public void viewportOneToOne()
	{
		viewport.getViewportElement().oneToOne();
	}

	
	
	
	
	
	
	protected void back()
	{
		if ( history.canGoBack() )
		{
			onPreHistoryChange();
			history.back();
			Location location = history.getCurrentState().getLocation();
			locationField.setText( location.getLocationString() );
			resolve();
		}
	}
	
	protected void forward()
	{
		if ( history.canGoForward() )
		{
			onPreHistoryChange();
			history.forward();
			Location location = history.getCurrentState().getLocation();
			locationField.setText( location.getLocationString() );
			resolve();
		}
	}
	
	protected void reload()
	{
		resolve();
	}

	
	
	private ScrolledViewport.ScrolledViewportControl makeViewport(Object child, PersistentState state)
	{
		Pres childPres = Pres.coerce( child );
		ScrolledViewport vp = new ScrolledViewport( childPres, 0.0, 0.0, state );
		return (ScrolledViewport.ScrolledViewportControl)vp.createControl( PresentationContext.defaultCtx, StyleValues.instance );
	}
	
	private DPElement makeCommandElement(Object child)
	{
		Pres childPres = Pres.coerce( child );
		return childPres.present();
	}
	
	
	private void resolve()
	{
		// Get the location to resolve
		Location location = history.getCurrentState().getLocation();
		
		PersistentStateStore stateStore = history.getCurrentState().getPagePersistentState();
		BrowserPage p = resolver.resolveLocationAsPage( location, stateStore );

		viewport = makeViewport( p.getContentsElement().alignHExpand(), history.getCurrentState().getViewportState() );
		presComponent.getRootElement().setChild( viewport.getElement().alignHExpand().alignVExpand() );
		
		// Set the page
		setPage( p );
	}
	
	private void setPage(BrowserPage p)
	{
		if ( p != page )
		{
			if ( page != null )
			{
				page.setCommandHistoryListener( null );
			}
			
			page = p;
			
			if ( page != null  &&  commandHistoryListener != null )
			{
				page.setCommandHistoryListener( commandHistoryListener );
			}
			
			if ( commandHistoryListener != null )
			{
				commandHistoryListener.onCommandHistoryChanged( getCommandHistoryController() );
			}
			
			
			if ( listener != null )
			{
				listener.onBrowserChangeTitle( this, getTitle() );
			}
		}
	}
	
	
	private void setLocation(Location location)
	{
		onPreHistoryChange();
		history.visit( location );
		resolve();
	}
	
	
	private void onLocationField(String location)
	{
		setLocation( new Location( location ) );
	}
	
	
	private void onPreHistoryChange()
	{
		PersistentStateStore store = page.storePersistentState();
		history.getCurrentState().setPagePersistentState( store );
	}



	
	private void initialiseToolbar(JToolBar toolbar)
	{
		ActionListener backListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				back();
			}
		};

		ActionListener forwardListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				forward();
			}
		};
		
		ActionListener reloadListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				reload();
			}
		};
		
		
		toolbar.add( makeToolButton( "back arrow.png", COMMAND_BACK, "Go back", "Back", backListener ) );
		toolbar.add( makeToolButton( "forward arrow.png", COMMAND_FORWARD, "Go forward", "Forward", forwardListener ) );
		toolbar.add( makeToolButton( "reload.png", COMMAND_RELOAD, "Reload page", "Reload", reloadListener ) );
	}
	
	private JButton makeToolButton(String imageFilename, String actionCommand, String tooltipText, String altText, ActionListener listener)
	{
		String imagePath = "icons/" + imageFilename;
		
		JButton button = new JButton();
		button.setActionCommand( actionCommand );
		button.setToolTipText( tooltipText );
		button.addActionListener( listener );
		button.setFocusable( false );
		
		ImageIcon icon = new ImageIcon( imagePath, altText );
		if ( icon.getImageLoadStatus() != MediaTracker.ABORTED  &&  icon.getImageLoadStatus() != MediaTracker.ERRORED )
		{
			button.setIcon( icon );
		}
		else
		{
			button.setText( altText );
			System.err.println( "Could not load image " + imagePath );
		}
		
		return button;
	}
}
