//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.CommandHistory.CommandHistory;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPRegion;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.Browser.Page;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.PersistentState.PersistentStateStore;
import BritefuryJ.DocPresent.PersistentState.PersistentStateTable;
import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.GSymAbstractPerspective;
import BritefuryJ.GSym.GSymBrowserContext;
import BritefuryJ.GSym.GSymSubject;
import BritefuryJ.IncrementalTree.IncrementalTree;
import BritefuryJ.IncrementalTree.IncrementalTreeNode;
import BritefuryJ.IncrementalTree.IncrementalTreeNodeTable;
import BritefuryJ.Logging.Log;
import BritefuryJ.Utils.HashUtils;
import BritefuryJ.Utils.Profile.ProfileTimer;

public class GSymView extends IncrementalTree
{
	//
	//
	// PROFILING
	//
	//
	
	static boolean ENABLE_PROFILING = false;
	static boolean ENABLE_DISPLAY_TREESIZES = false;
	
	
	public interface NodeElementChangeListener
	{
		public void reset(GSymView view);
		public void elementChangeFrom(GSymFragmentView node, DPElement e);
		public void elementChangeTo(GSymFragmentView node, DPElement e);
	}
	
	
	public static class StateStore extends PersistentStateStore
	{
		private HashMap<IncrementalTreeNodeTable.Key, LinkedList<PersistentStateTable>> table = new HashMap<IncrementalTreeNodeTable.Key, LinkedList<PersistentStateTable>>();
		
		
		public StateStore()
		{
		}
		
		private void addPersistentState(Object node, PersistentStateTable persistentStateTable)
		{
			IncrementalTreeNodeTable.Key key = new IncrementalTreeNodeTable.Key( node );
			LinkedList<PersistentStateTable> entryList = table.get( key );
			if ( entryList == null )
			{
				entryList = new LinkedList<PersistentStateTable>();
				table.put( key, entryList );
			}
			entryList.push( persistentStateTable );
		}
		
		private PersistentStateTable usePersistentState(Object node)
		{
			LinkedList<PersistentStateTable> entryList = table.get( new IncrementalTreeNodeTable.Key( node ) );
			if ( entryList != null )
			{
				return entryList.removeFirst();
			}
			else
			{
				return null;
			}
		}
		
		
		public boolean isEmpty()
		{
			return table.isEmpty();
		}
	}
	
	
	
	
	protected static class ViewFragmentContextAndResultFactory implements IncrementalTreeNode.NodeResultFactory
	{
		protected GSymView view;
		protected GSymAbstractPerspective perspective;
		protected AttributeTable subjectContext;
		protected StyleSheet styleSheet;
		protected AttributeTable inheritedState;
		
		public ViewFragmentContextAndResultFactory(GSymView view, GSymAbstractPerspective perspective, AttributeTable subjectContext, StyleSheet styleSheet, AttributeTable inheritedState)
		{
			this.view = view;
			this.perspective = perspective;
			this.subjectContext = subjectContext;
			this.styleSheet = styleSheet;
			this.inheritedState = inheritedState;
		}


		public Object createNodeResult(IncrementalTreeNode incrementalNode, Object docNode)
		{
			view.profile_startPython();

			// Create the node context
			GSymFragmentView fragmentView = (GSymFragmentView)incrementalNode;
			
			// Create the view fragment
			DPElement fragment = perspective.present( docNode, fragmentView, styleSheet, inheritedState );
			
			view.profile_stopPython();
			
			return fragment;
		}
	}
	
	
	protected static class ViewFragmentContextAndResultFactoryKey
	{
		private GSymAbstractPerspective perspective;
		private AttributeTable subjectContext;
		private StyleSheet styleSheet;
		private AttributeTable inheritedState;
		
		
		public ViewFragmentContextAndResultFactoryKey(GSymAbstractPerspective perspective, AttributeTable subjectContext, StyleSheet styleSheet, AttributeTable inheritedState)
		{
			this.perspective = perspective;
			this.styleSheet = styleSheet;
			this.inheritedState = inheritedState;
			this.subjectContext = subjectContext;
		}
		
		
		public int hashCode()
		{
			if ( styleSheet == null )
			{
				throw new RuntimeException( "null?styleSheet=" + ( styleSheet == null ) );
			}
			return HashUtils.nHash( new int[] { System.identityHashCode( perspective ), styleSheet.hashCode(), inheritedState.hashCode(), subjectContext.hashCode() } );
		}
		
		public boolean equals(Object x)
		{
			if ( x == this )
			{
				return true;
			}
			
			if ( x instanceof ViewFragmentContextAndResultFactoryKey )
			{
				ViewFragmentContextAndResultFactoryKey kx = (ViewFragmentContextAndResultFactoryKey)x;
				return perspective == kx.perspective  &&  styleSheet.equals( kx.styleSheet )  &&  inheritedState == kx.inheritedState  &&  subjectContext == kx.subjectContext;
			}
			else
			{
				return false;
			}
		}
	}


	
	
	
	
	
	private NodeElementChangeListener elementChangeListener = null;
	private DPVBox rootBox = null;
	
	private StateStore stateStoreToLoad;
	
	
	private boolean bProfilingEnabled = false;
	private ProfileTimer pythonTimer = new ProfileTimer();
	private ProfileTimer javaTimer = new ProfileTimer();
	private ProfileTimer elementTimer = new ProfileTimer();
	private ProfileTimer contentChangeTimer = new ProfileTimer();
	private ProfileTimer updateNodeElementTimer = new ProfileTimer();
	
	
	
	private GSymFragmentView.NodeResultFactory rootNodeResultFactory;
	
	
	private DPVBox vbox;
	private DPRegion region;
	
	private GSymBrowserContext browserContext;
	private GSymViewPage page;
	
	private CommandHistory commandHistory;

	private HashMap<ViewFragmentContextAndResultFactoryKey, ViewFragmentContextAndResultFactory> viewFragmentContextAndResultFactories =
		new HashMap<ViewFragmentContextAndResultFactoryKey, ViewFragmentContextAndResultFactory>();
	
	
	
	
	public GSymView(GSymSubject subject, GSymBrowserContext browserContext, PersistentStateStore persistentState)
	{
		super( subject.getFocus(), DuplicatePolicy.ALLOW_DUPLICATES );
		rootNodeResultFactory = makeNodeResultFactory( subject.getPerspective(), subject.getSubjectContext(), subject.getPerspective().getStyleSheet(), subject.getPerspective().getInitialInheritedState() );
		
		rootBox = null;
		
		if ( persistentState != null  &&  persistentState instanceof StateStore )
		{
			stateStoreToLoad = (StateStore)persistentState;
		}
	
	
		
		this.browserContext = browserContext;
		this.commandHistory = subject.getCommandHistory();
		
		region = new DPRegion();
		vbox = PrimitiveStyleSheet.instance.vbox( new DPElement[] { region } );

		page = new GSymViewPage( vbox.alignHExpand().alignVExpand(), subject.getTitle(), browserContext, commandHistory, this );
		
		setElementChangeListener( new NodeElementChangeListenerDiff() );
		
		// We need to do this last
		region.setChild( getRootViewElement().alignHExpand().alignVExpand() );
		region.setEditHandler( subject.getPerspective().getEditHandler() );
	}
	
	
	public void setElementChangeListener(NodeElementChangeListener elementChangeListener)
	{
		this.elementChangeListener = elementChangeListener;
	}
	
	
	
	
	protected IncrementalTreeNode.NodeResultFactory getRootNodeResultFactory()
	{
		return rootNodeResultFactory;
	}

	protected GSymFragmentView.NodeResultFactory makeNodeResultFactory(GSymAbstractPerspective perspective, AttributeTable subjectContext, StyleSheet styleSheet, AttributeTable inheritedState)
	{
		// Memoise the contents factory, keyed by  @nodeViewFunction and @state
		ViewFragmentContextAndResultFactoryKey key = new ViewFragmentContextAndResultFactoryKey( perspective, subjectContext, styleSheet, inheritedState );
		
		ViewFragmentContextAndResultFactory factory = viewFragmentContextAndResultFactories.get( key );
		
		if ( factory == null )
		{
			factory = new ViewFragmentContextAndResultFactory( this, perspective, subjectContext, styleSheet, inheritedState );
			viewFragmentContextAndResultFactories.put( key, factory );
		}
		
		return factory;
	}

	
	
	
	
	public DPElement getRootViewElement()
	{
		if ( rootBox == null )
		{
			performRefresh();
			GSymFragmentView rootView = (GSymFragmentView)getRootIncrementalTreeNode();
			rootView.getRefreshedFragmentElement().alignHExpand();
			rootView.getRefreshedFragmentElement().alignVExpand();
			rootBox = PrimitiveStyleSheet.instance.vbox( new DPElement[] { rootView.getRefreshedFragmentElement() } );
		}
		return rootBox;
	}
	
	
	
	public PersistentStateStore storePersistentState()
	{
		StateStore store = new StateStore();
		
		LinkedList<IncrementalTreeNode> nodeQueue = new LinkedList<IncrementalTreeNode>();
		nodeQueue.push( getRootIncrementalTreeNode() );
		
		while ( !nodeQueue.isEmpty() )
		{
			IncrementalTreeNode node = nodeQueue.removeFirst();
			
			// Get the persistent state, if any, and store it
			GSymFragmentView viewNode = (GSymFragmentView)node;
			PersistentStateTable stateTable = viewNode.getPersistentStateTable();
			if ( stateTable != null )
			{
				store.addPersistentState( node.getDocNode(), stateTable );
			}
			
			// Add the children using an interator; that means that they will be inserted at the beginning
			// of the queue so that they appear *in order*, hence they will be removed in order.
			ListIterator<IncrementalTreeNode> iterator = nodeQueue.listIterator();
			for (IncrementalTreeNode child: ((GSymFragmentView)node).getChildren())
			{
				iterator.add( child );
			}
		}
		
		
		return store;
	}
	
	
	
	protected void performRefresh()
	{
		// >>> PROFILING
		long t1 = 0;
		if ( ENABLE_PROFILING )
		{
			t1 = System.nanoTime();
			ProfileTimer.initProfiling();
			beginProfiling();
		}

		
		profile_startJava();
		// <<< PROFILING
		
		super.performRefresh();
		stateStoreToLoad = null;
		
		// >>> PROFILING
		profile_stopJava();
	
	
		if ( ENABLE_PROFILING )
		{
			long t2 = System.nanoTime();
			endProfiling();
			ProfileTimer.shutdownProfiling();
			double deltaT = ( t2 - t1 )  *  1.0e-9;
			System.out.println( "DocView: REFRESH VIEW TIME = " + deltaT );
			System.out.println( "DocView: REFRESH VIEW PROFILE: JAVA TIME = " + getJavaTime() + ", ELEMENT CREATE TIME = " + getElementTime() +
					", PYTHON TIME = " + getPythonTime() + ", CONTENT CHANGE TIME = " + getContentChangeTime() +
					", UPDATE NODE ELEMENT TIME = " + getUpdateNodeElementTime() );
		}
		
		if ( ENABLE_DISPLAY_TREESIZES )
		{
			GSymFragmentView rootView = (GSymFragmentView)getRootIncrementalTreeNode();
			int presTreeSize = rootView.getRefreshedFragmentElement().computeSubtreeSize();
			int numFragments = rootView.computeSubtreeSize();
			System.out.println( "DocView.performRefresh(): presentation tree size=" + presTreeSize + ", # fragments=" + numFragments );
		}
		// <<< PROFILING
	}
	

	private PersistentStateTable persistentStateForNode(Object node)
	{
		PersistentStateTable persistentState = null;
		if ( stateStoreToLoad != null )
		{
			persistentState = stateStoreToLoad.usePersistentState( node );
		}
		return persistentState;
	}
	
	protected IncrementalTreeNode createIncrementalTreeNode(Object node)
	{
		return new GSymFragmentView( node, this, persistentStateForNode( node ) );
	}

	
	
	
	public void profile_startPython()
	{
		if ( bProfilingEnabled )
		{
			pythonTimer.start();
		}
	}
	
	public void profile_stopPython()
	{
		if ( bProfilingEnabled )
		{
			pythonTimer.stop();
		}
	}

	
	public void profile_startJava()
	{
		if ( bProfilingEnabled )
		{
			javaTimer.start();
		}
	}
	
	public void profile_stopJava()
	{
		if ( bProfilingEnabled )
		{
			javaTimer.stop();
		}
	}
	
	
	public void profile_startElement()
	{
		if ( bProfilingEnabled )
		{
			elementTimer.start();
		}
	}
	
	public void profile_stopElement()
	{
		if ( bProfilingEnabled )
		{
			elementTimer.stop();
		}
	}
	
	
	public void profile_startContentChange()
	{
		if ( bProfilingEnabled )
		{
			contentChangeTimer.start();
		}
	}
	
	public void profile_stopContentChange()
	{
		if ( bProfilingEnabled )
		{
			contentChangeTimer.stop();
		}
	}
	
	
	
	public void profile_startUpdateNodeElement()
	{
		if ( bProfilingEnabled )
		{
			updateNodeElementTimer.start();
		}
	}
	
	public void profile_stopUpdateNodeElement()
	{
		if ( bProfilingEnabled )
		{
			updateNodeElementTimer.stop();
		}
	}
	
	
	
	
	public void beginProfiling()
	{
		bProfilingEnabled = true;
		pythonTimer.reset();
		javaTimer.reset();
		elementTimer.reset();
		contentChangeTimer.reset();
		updateNodeElementTimer.reset();
	}

	public void endProfiling()
	{
		bProfilingEnabled = false;
	}
	
	public double getPythonTime()
	{
		return pythonTimer.getTime();
	}

	public double getJavaTime()
	{
		return javaTimer.getTime();
	}

	public double getElementTime()
	{
		return elementTimer.getTime();
	}
	
	public double getContentChangeTime()
	{
		return contentChangeTimer.getTime();
	}
	
	public double getUpdateNodeElementTime()
	{
		return updateNodeElementTimer.getTime();
	}

	
	

	protected void onResultChangeTreeRefresh()
	{
		if ( elementChangeListener != null )
		{
			elementChangeListener.reset( this );
		}
	}

	protected void onResultChangeFrom(IncrementalTreeNode node, Object result)
	{
		if ( elementChangeListener != null )
		{
			profile_startContentChange();
			elementChangeListener.elementChangeFrom( (GSymFragmentView)node, (DPElement)result );
			profile_stopContentChange();
		}
	}

	protected void onResultChangeTo(IncrementalTreeNode node, Object result)
	{
		if ( elementChangeListener != null )
		{
			profile_startContentChange();
			elementChangeListener.elementChangeTo( (GSymFragmentView)node, (DPElement)result );
			profile_stopContentChange();
		}
	}
	
	
	
	
	
	
	
	
	public Caret getCaret()
	{
		PresentationComponent.RootElement elementTree = region.getRootElement();
		return elementTree != null  ?  elementTree.getCaret()  :  null;
	}
	
	public Selection getSelection()
	{
		PresentationComponent.RootElement elementTree = region.getRootElement();
		return elementTree != null  ?  elementTree.getSelection()  :  null;
	}
	
	
	
	
	public Object getDocRootNode()
	{
		return modelRootNode;
	}
	
	
	
	public PresentationComponent.RootElement getElementTree()
	{
		return vbox.getRootElement();
	}
	
	
	
	public GSymBrowserContext getBrowserContext()
	{
		return browserContext;
	}
	
	public Page getPage()
	{
		return page;
	}
	
	public Log getPageLog()
	{
		return page.getLog();
	}
	
	public CommandHistory getCommandHistory()
	{
		return commandHistory;
	}
	
	
	
	public void onRequestRefresh()
	{
		Runnable r = new Runnable()
		{
			public void run()
			{
				refresh();
			}
		};
		region.queueImmediateEvent( r );
	}
}