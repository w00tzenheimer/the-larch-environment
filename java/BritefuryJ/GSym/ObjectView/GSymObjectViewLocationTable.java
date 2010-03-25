//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.ObjectView;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.WeakHashMap;

public class GSymObjectViewLocationTable
{
	private WeakHashMap<Object, String> objectToLocation = new WeakHashMap<Object, String>();
	private HashMap<String, WeakReference<Object>> locationToObject = new HashMap<String, WeakReference<Object>>();
	int objectCount = 1;
	
	
	public GSymObjectViewLocationTable()
	{
	}
	
	
	public String getLocationForObject(Object x)
	{
		String location = objectToLocation.get( x );
		if ( location == null )
		{
			location = "$object/" + x.getClass().getName() + objectCount++;
			objectToLocation.put( x, location );
		}
		return location;
	}
	
	public Object getObjectAtLocation(String location)
	{
		WeakReference<Object> ref = locationToObject.get( location );
		if ( ref != null )
		{
			Object x = ref.get();
			
			if ( x == null )
			{
				locationToObject.remove( location );
			}
			
			return x;
		}
		
		return null;
	}
}