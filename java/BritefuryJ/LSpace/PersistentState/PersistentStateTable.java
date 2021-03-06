//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.PersistentState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PersistentStateTable
{
	private HashMap<Object, PersistentState> data = new HashMap<Object, PersistentState>();
	private HashSet<Object> usedKeys = null;
	
	
	public PersistentStateTable()
	{
	}
	
	
	public void onRefreshBegin()
	{
		usedKeys = null;
	}
	
	public void onRefreshEnd()
	{
		if ( usedKeys == null )
		{
			data.clear();
		}
		else
		{
			for (Map.Entry<Object, PersistentState> entry: data.entrySet())
			{
				if ( !usedKeys.contains( entry.getKey() ) )
				{
					data.remove( entry.getKey() );
				}
			}
		}
		
		usedKeys = null;
	}
	
	
	public PersistentState persistentState(Object key)
	{
		PersistentState state = data.get( key );
		if ( state == null )
		{
			state = new PersistentState();
			data.put( key, state );
		}
		
		if ( usedKeys == null )
		{
			usedKeys = new HashSet<Object>();
		}
		usedKeys.add( key );
		
		return state;
	}
	
	
	public boolean isEmpty()
	{
		return data.isEmpty();
	}
}
