//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.TreeParser;

import java.util.List;

public class ClearBindings extends UnaryBranchExpression
{
	public ClearBindings(Object subexp)
	{
		super( subexp );
	}
	
	public ClearBindings(TreeParserExpression subexp)
	{
		super( subexp );
	}
	
	
	
	protected TreeParseResult evaluateNode(TreeParserState state, Object input)
	{
		TreeParseResult res = subexp.processNode( state, input );
		
		if ( res.isValid() )
		{
			return res.clearBindings();
		}
		else
		{
			return res;
		}
	}
	
	protected TreeParseResult evaluateList(TreeParserState state, List<Object> input, int start, int stop)
	{
		TreeParseResult res = subexp.processList( state, input, start, stop );
		
		if ( res.isValid() )
		{
			return res.clearBindings();
		}
		else
		{
			return res;
		}
	}
	
	
	public boolean compareTo(TreeParserExpression x)
	{
		if ( x instanceof ClearBindings )
		{
			return super.compareTo( x );
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "ClearBindings( " + subexp.toString() + " )";
	}
}