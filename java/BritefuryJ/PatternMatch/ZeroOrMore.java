//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;


public class ZeroOrMore extends Repetition
{
	public ZeroOrMore(Object subexp)
	{
		super( subexp, 0, -1 );
	}
	
	public ZeroOrMore(MatchExpression subexp)
	{
		super( subexp, 0, -1 );
	}



	public String toString()
	{
		return "ZeroOrMore( " + subexp.toString() + " )";
	}
}