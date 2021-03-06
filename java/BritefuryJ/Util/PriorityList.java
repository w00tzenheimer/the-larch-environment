//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class PriorityList <T> implements Iterable<T>, Serializable
{
	private static final long serialVersionUID = 1L;

	
	private static class Entry <T>
	{
		private int priority;
		T item;
		
		
		public Entry(int priority, T item)
		{
			this.priority = priority;
			this.item = item;
		}
	}
	
	
	private ArrayList<Entry<T>> priorityList = new ArrayList<Entry<T>>();
	private T grab = null;
	
	
	
	public PriorityList()
	{
	}
	
	
	public void add(int priority, T item)
	{
		remove( item );
		int index = binarySearchInsertionPoint( priorityList, priority );
		priorityList.add( index, new Entry<T>( priority, item ) );
	}

	public void add(T item)
	{
		add( 0, item );
	}
	
	public void remove(T data)
	{
		int index = 0;
		if ( grab == data )
		{
			grab = null;
		}
		for (Entry<T> e: priorityList)
		{
			if ( e.item == data )
			{
				priorityList.remove( index );
				return;
			}
			index++;
		}
	}
	
	public int size()
	{
		return priorityList.size();
	}
	
	
	
	public void grab(T item)
	{
		if ( grab != null )
		{
			throw new RuntimeException( "Grab already in place" );
		}
		grab = item;
	}

	public void ungrab(T item)
	{
		if ( grab != item )
		{
			throw new RuntimeException( "Attempted to ungrab wrong item" );
		}
		grab = null;
	}


	
	
	public Iterator<T> iterator()
	{
		Iterator<T> iter = new Iterator<T>()
		{
			int index = grab != null  ?  -1  :  0;
			
			
			public boolean hasNext()
			{
				return index < size();
			}

			public T next()
			{
				if ( index == -1 )
				{
					index = nextIndex( index );
					return grab;
				}
				else
				{
					if ( index >= size() )
					{
						throw new NoSuchElementException();
					}
					
					T item = priorityList.get( index ).item;
					index = nextIndex( index );
					
					return item;
				}
			}
			
			
			private int nextIndex(int index)
			{
				index++;
				
				while ( index < size()  &&  priorityList.get( index ).item == grab )
				{
					index++;
				}
				
				return index;
			}
			
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
		
		return iter;
	}
	



	private static <T> int binarySearchInsertionPoint(ArrayList<Entry<T>> sorted, int key)
	{
		return binarySearchInsertionPoint( sorted, 0, sorted.size(), key );
	}

	private static <T> int binarySearchInsertionPoint(ArrayList<Entry<T>> sorted, int lo, int hi, int key)
	{
		while ( lo < hi )
		{
			int mid = ( lo + hi ) / 2;
			int midPriority = sorted.get( mid ).priority;
			if ( key < midPriority )
			{
				hi = mid;
			}
			else if ( key > midPriority )
			{
				lo = mid + 1;
			}
			else
			{
				return mid;
			}
		}
		
		return lo;
	}
}
