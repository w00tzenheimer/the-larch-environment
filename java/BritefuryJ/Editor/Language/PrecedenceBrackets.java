//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Language;

import BritefuryJ.DocPresent.Combinators.CompositePres;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Span;
import BritefuryJ.DocPresent.Combinators.Primitive.Text;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class PrecedenceBrackets extends CompositePres
{
	private Pres child;
	private int numBrackets;
	private Pres openBracket, closeBracket;
	
	
	
	public PrecedenceBrackets(Object child, int precedence, int outerPrecedence, int numBrackets, Object openBracket, Object closeBracket)
	{
		this.child = Pres.coerce( child );
		int required = numBracketsRequired( precedence, outerPrecedence );
		this.numBrackets = Math.max( numBrackets, required );
		this.openBracket = Pres.coerce( openBracket );
		this.closeBracket = Pres.coerce( closeBracket );
	}
	
	public PrecedenceBrackets(Object child, int precedence, int outerPrecedence, int numAdditionalBrackets, String openBracket, String closeBracket, StyleSheet bracketStyle)
	{
		this( child, precedence, outerPrecedence, numAdditionalBrackets, bracketStyle.applyTo( new Text( openBracket ) ), bracketStyle.applyTo( new Text( closeBracket ) ) );
	}
	
	public PrecedenceBrackets(Object child, int precedence, int outerPrecedence, int numAdditionalBrackets, StyleSheet bracketStyle)
	{
		this( child, precedence, outerPrecedence, numAdditionalBrackets, bracketStyle.applyTo( new Text( "(" ) ), bracketStyle.applyTo( new Text( ")" ) ) );
	}
	
	
	private static int numBracketsRequired(int precedence, int outerPrecedence)
	{
		if ( precedence != -1  &&  outerPrecedence != -1  &&  precedence > outerPrecedence )
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}


	@Override
	public Pres pres(PresentationContext ctx, StyleValues style)
	{
		if ( numBrackets == 0 )
		{
			return child;
		}
		else
		{
			Pres x[] = new Pres[numBrackets*2 + 1];
			for (int i = 0; i < numBrackets; i++)
			{
				x[i] = openBracket;
				x[numBrackets+1+i] = closeBracket; 
			}
			x[numBrackets] = child;
			return new Span( x );
		}
	}
}
