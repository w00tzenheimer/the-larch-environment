//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;

import java.util.List;

import BritefuryJ.Util.RichString.RichStringAccessor;

/*
 * LiteralValue
 * 
 * LiteralValue:node( input )			->  input == LiteralValue.matchValue  ?  input  :  fail
 * LiteralValue:string( input, start )		->  fail
 * LiteralValue:richStr( input, start )		->  item = input.structuralItem(); item == LiteralValue.matchValue  ?  item  :  fail
 * LiteralValue:list( input, start )			->  input[start] == LiteralValue.matchValue  ?  input[start]  :  fail
 */
public class LiteralNode extends ParserExpression
{
	protected Object matchValue;
	
	
	public LiteralNode(Object matchValue)
	{
		this.matchValue = matchValue;
	}
	
	
	public Object getMatchValue()
	{
		return matchValue;
	}
	
	
	
	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		if ( input == matchValue  ||  input.equals( matchValue ) )
		{
			return new ParseResult( input, 0, 1 );
		}
		
		return ParseResult.failure( 0 );
	}

	protected ParseResult evaluateStringChars(ParserState state, String input, int start)
	{
		return ParseResult.failure( start );
	}
	
	protected ParseResult evaluateRichStringItems(ParserState state, RichStringAccessor input, int start)
	{
		if ( start < input.length() )
		{
			start = state.skipJunkChars( input, start );
			
			Object valueArray[] = input.matchStructuralNode( start );
			
			if ( valueArray != null )
			{
				if ( valueArray[0] == matchValue  ||  valueArray[0].equals( matchValue ) )
				{
					return new ParseResult( valueArray[0], start, start + 1 );
				}
			}
		}
		
		return ParseResult.failure( start );
	}

	protected ParseResult evaluateListItems(ParserState state, List<Object> input, int start)
	{
		if ( start < input.size() )
		{
			Object x = input.get( start );
			
			if ( x == matchValue  ||  x.equals( matchValue ) )
			{
				return new ParseResult( x, start, start + 1 );
			}
		}

		return ParseResult.failure( start );
	}
	
	
	
	public boolean isEquivalentTo(ParserExpression x)
	{
		if ( x instanceof LiteralNode )
		{
			LiteralNode xl = (LiteralNode)x;
			return matchValue.equals( xl.matchValue );
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "LiteralValue( " + matchValue.toString() + " )";
	}
}
