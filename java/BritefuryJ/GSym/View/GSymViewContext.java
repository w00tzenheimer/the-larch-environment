//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View;

import java.util.HashMap;

import org.python.core.PyObject;

import BritefuryJ.CommandHistory.CommandHistory;
import BritefuryJ.DocModel.DMNode;
import BritefuryJ.DocPresent.DPFrame;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.EditHandler;
import BritefuryJ.DocPresent.ElementContext;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.EmptyBorder;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocView.DVNode;
import BritefuryJ.DocView.DocView;

public class GSymViewContext implements DocView.RefreshListener, ElementContext
{
	protected static class NodeContentsFactory implements DVNode.NodeElementFactory
	{
		private GSymViewContext viewInstance;
		private GSymNodeViewFunction nodeViewFunction;
		private Object state;
		
		
		public NodeContentsFactory(GSymViewContext viewInstance, GSymNodeViewFunction viewFunction, Object state)
		{
			assert viewFunction != null;
			
			this.viewInstance = viewInstance;
			this.nodeViewFunction = viewFunction;
			this.state = state;
		}


		public DPWidget createNodeElement(DVNode viewNode, DMNode treeNode)
		{
			// Create the node view instance
			GSymNodeViewContext nodeViewInstance = new GSymNodeViewContext( viewInstance, viewNode );
			
			// Build the contents
			//return nodeViewFunction.createElement( treeNode, nodeViewInstance, state );
			
			viewInstance.getView().profile_startPython();
			DPWidget e = nodeViewFunction.createElement( treeNode, nodeViewInstance, state );
			viewInstance.getView().profile_stopPython();
			return e;
		}
	}
	
	
	private static class NodeContentsFactoryKey
	{
		private GSymNodeViewFunction nodeViewFunction;
		private Object state;
		
		
		public NodeContentsFactoryKey(GSymNodeViewFunction nodeViewFunction, Object state)
		{
			this.nodeViewFunction = nodeViewFunction;
			this.state = state;
		}
		
		
		public int hashCode()
		{
			int stateHash = state != null  ?  state.hashCode()  :  0;
			int mult = 1000003;
			int x = 0x345678;
			x = ( x ^ nodeViewFunction.hashCode() ) * mult;
			mult += 82520 + 2;
			x = ( x ^ stateHash ) * mult;
			return x + 97351;
		}
		
		public boolean equals(Object x)
		{
			if ( x == this )
			{
				return true;
			}
			
			if ( x instanceof NodeContentsFactoryKey )
			{
				NodeContentsFactoryKey kx = (NodeContentsFactoryKey)x;
				if ( state == null  ||  kx.state == null )
				{
					return nodeViewFunction == kx.nodeViewFunction  &&  ( state != null ) == ( kx.state != null );
				}
				else
				{
					return nodeViewFunction == kx.nodeViewFunction  &&  state.equals( kx.state );
				}
			}
			else
			{
				return false;
			}
		}
	}
	
	
	
	private DMNode docRootNode;
	
	private GSymNodeViewFunction generalNodeViewFunction;
	
	private DocView view;
	
	private DPFrame frame;
	
	private HashMap<Double, Border> indentationBorders;
	private HashMap<NodeContentsFactoryKey, NodeContentsFactory> nodeContentsFactories;
	
	private CommandHistory commandHistory;
	
	
	public GSymViewContext(DMNode docRootNode, GSymNodeViewFunction generalNodeViewFunction, GSymNodeViewFunction rootNodeViewFunction,
			CommandHistory commandHistory)
	{
		this.docRootNode = docRootNode;
		this.commandHistory = commandHistory;
		
		this.generalNodeViewFunction = generalNodeViewFunction; 
		
		frame = new DPFrame( this );
		
		indentationBorders = new HashMap<Double, Border>();
		nodeContentsFactories = new HashMap<NodeContentsFactoryKey, NodeContentsFactory>();

		view = new DocView( docRootNode, makeNodeElementFactory( rootNodeViewFunction, null ) );
		view.setElementChangeListener( new NodeElementChangeListenerDiff() );
		view.setRefreshListener( this );
		frame.setChild( view.getRootViewElement().alignHExpand() );
	}
	
	
	public GSymViewContext(DMNode docRootNode, PyObject generalNodeViewFunction, PyObject rootNodeViewFunction, CommandHistory commandHistory)
	{
		this( docRootNode, new PyGSymNodeViewFunction( generalNodeViewFunction ), new PyGSymNodeViewFunction( generalNodeViewFunction ), commandHistory );
	}

	
	public GSymViewContext(DMNode docRootNode, GSymNodeViewFunction nodeViewFunction, CommandHistory commandHistory)
	{
		this( docRootNode, nodeViewFunction, nodeViewFunction, commandHistory );
	}

	public GSymViewContext(DMNode docRootNode, PyObject nodeViewFunction, CommandHistory commandHistory)
	{
		this( docRootNode, new PyGSymNodeViewFunction( nodeViewFunction ), commandHistory );
	}

	
	
	protected Border indentationBorder(double indentation)
	{
		Border border = indentationBorders.get( indentation );
		
		if ( border == null )
		{
			border = new EmptyBorder( indentation, 0.0, 0.0, 0.0 );
			indentationBorders.put( indentation, border );
		}
		
		return border;
	}
	
	
	protected DVNode.NodeElementFactory makeNodeElementFactory(GSymNodeViewFunction nodeViewFunction, Object state)
	{
		// Memoise the contents factory, keyed by  @nodeViewFunction and @state
		if ( nodeViewFunction == null )
		{
			nodeViewFunction = generalNodeViewFunction;
		}

		NodeContentsFactoryKey key = new NodeContentsFactoryKey( nodeViewFunction, state );
		
		NodeContentsFactory factory = nodeContentsFactories.get( key );
		
		if ( factory == null )
		{
			factory = new NodeContentsFactory( this, nodeViewFunction, state );
			nodeContentsFactories.put( key, factory );
			return factory;
		}
		
		return factory;
	}
	
	
	
	public Caret getCaret()
	{
		DPPresentationArea elementTree = frame.getPresentationArea();
		return elementTree != null  ?  elementTree.getCaret()  :  null;
	}
	
	public Selection getSelection()
	{
		DPPresentationArea elementTree = frame.getPresentationArea();
		return elementTree != null  ?  elementTree.getSelection()  :  null;
	}
	
	
	
	
	public DocView getView()
	{
		return view;
	}
	
	public DPFrame getFrame()
	{
		return frame;
	}
	
	public EditHandler getEditHandler()
	{
		return frame.getEditHandler();
	}
	
	public DPPresentationArea getElementTree()
	{
		return frame.getPresentationArea();
	}
	
	
	
	public DMNode getDocRootNode()
	{
		return docRootNode;
	}
	
	
	
	public CommandHistory getCommandHistory()
	{
		return commandHistory;
	}



	public void onViewRequestRefresh(DocView view)
	{
		Runnable r = new Runnable()
		{
			public void run()
			{
				refreshView();
			}
		};
		frame.queueImmediateEvent( r );
	}
	
	
	private void refreshView()
	{
		view.refresh();
	}
}
