//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table.ObjectList;

import org.python.core.Py;

public abstract class AbstractColumn
{
	public abstract Object get(Object modelRow);
	public abstract void set(Object modelRow, Object value);
	
	
	protected static AbstractColumn coerce(Object x)
	{
		if ( x instanceof AbstractColumn )
		{
			return (AbstractColumn)x;
		}
		else if ( x instanceof String )
		{
			return new AttributeColumn( Py.newString( (String)x ) );
		}
		else
		{
			throw new RuntimeException( "Could not coerce type " + x.getClass().getName() + " to create an AbstractColumn" );
		}
	}
}