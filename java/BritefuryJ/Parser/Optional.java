//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.List;


public class Optional extends UnaryBranchExpression
{
	public Optional(String subexp)
	{
		super( subexp );
	}
	
	public Optional(List<Object> subexp) throws ParserCoerceException
	{
		super( subexp );
	}
		
	public Optional(ParserExpression subexp)
	{
		super( subexp );
	}
	

	protected ParseResult evaluate(ParserState state, String input, int start, int stop)
	{
		ParseResult res = subexp.evaluate( state, input, start, stop );
		
		if ( res.isValid() )
		{
			return res;
		}
		else
		{
			return new ParseResult( null, res.begin, res.end );
		}
	}


	public String toString()
	{
		return "Optional( " + subexp.toString() + " )";
	}
}
