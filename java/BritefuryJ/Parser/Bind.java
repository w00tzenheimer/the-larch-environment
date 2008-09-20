//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Bind extends ParserExpression
{
	protected ParserExpression subexp;
	protected String name;
	
	
	public Bind(String subexp, String name)
	{
		this( coerce( subexp ), name );
	}
	
	public Bind(List<Object> subexp, String name) throws ParserCoerceException
	{
		this( coerce( subexp ), name );
	}
		
	public Bind(ParserExpression subexp, String name)
	{
		this.subexp = subexp;
		this.name = name;
	}
	
	
	public String getName()
	{
		return name;
	}
	

	protected ParseResult evaluate(ParserState state, String input, int start, int stop)
	{
		ParseResult res = subexp.evaluate( state, input, start, stop );
		
		if ( res.isValid() )
		{
			HashMap<String, Object> b = new HashMap<String, Object>();
			b.putAll( res.bindings );
			b.put( name, res.value );
			return new ParseResult( res.value, res.begin, res.end, b );
		}
		else
		{
			return res;
		}
	}



	public List<ParserExpression> getChildren()
	{
		ParserExpression[] children = { subexp };
		return Arrays.asList( children );
	}
	
	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof Bind )
		{
			Bind xb = (Bind)x;
			return subexp.compareTo( xb.subexp )  &&  name.equals( xb.name );
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "Bind( '" + name + "' = " + subexp.toString() + " )";
	}
}