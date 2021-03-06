//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.ChangeHistory;

import BritefuryJ.ObjectPresentation.PresentationStateListenerList;
import org.python.core.PyObject;

import java.util.ArrayList;

public abstract class AbstractChangeHistory {
	private ArrayList<ChangeHistoryListener> listeners = new ArrayList<ChangeHistoryListener>();


	public void addChangeHistoryListener(ChangeHistoryListener listener)
	{
		listeners.add( listener );
	}

	public void removeChangeHistoryListener(ChangeHistoryListener listener)
	{
		listeners.remove( listener );
	}




	public abstract ChangeHistory concreteChangeHistory();

	public abstract boolean canUndo();
	public abstract boolean canRedo();

	public abstract int getNumUndoChanges();
	public abstract int getNumRedoChanges();


	protected void onModified()
	{
		for (ChangeHistoryListener listener: listeners)
		{
			listener.onChangeHistoryChanged( this );
		}
	}
}
