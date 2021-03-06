//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;

import java.util.List;
import java.util.Map;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.Util.RichString.RichStringAccessor;


/*
 * Action
 * 
 * Action:node( input )		->  Action.action( Action.subexp:node( input ) )
 * Action:string( input, start )	->  Action.action( Action.subexp:string( input, start ) )
 * Action:richStr( input, start )	->  Action.action( Action.subexp:richStr( input, start ) )
 * Action:list( input, start )		->  Action.action( Action.subexp:list( input, start ) )
 */
public class Action extends UnaryBranchExpression
{
	protected static class PyAction implements ParseAction
	{
		private final PyObject callable;
		
		
		public PyAction(PyObject callable)
		{
			this.callable = callable;
		}


		public Object invoke(Object input, int pos, int end, Object value, Map<String, Object> bindings)
		{
			return callable.__call__( new PyObject[] { Py.java2py( input ), Py.java2py( pos ), Py.java2py( end ), Py.java2py( value ), Py.java2py( bindings ) } );
		}
	}
	
	
	protected ParseAction a;
	protected boolean bMergeUp;
	
	
	public Action(ParserExpression subexp, ParseAction a)
	{
		this( subexp, a, false );
	}
	
	public Action(ParserExpression subexp, ParseAction a, boolean bMergeUp)
	{
		super( subexp );
		this.a = a;
		this.bMergeUp = bMergeUp;
	}
	
	public Action(ParserExpression subexp, PyObject a)
	{
		this( subexp, new PyAction( a ) );
	}
	
	public Action(ParserExpression subexp, PyObject a, boolean bMergeUp)
	{
		this( subexp, new PyAction( a ), bMergeUp );
	}
	
	
	public Action(Object subexp, ParseAction a) throws ParserCoerceException
	{
		this( subexp, a, false );
	}
	
	public Action(Object subexp, ParseAction a, boolean bMergeUp) throws ParserCoerceException
	{
		super( subexp );
		this.a = a;
		this.bMergeUp = bMergeUp;
	}
	
	public Action(Object subexp, PyObject a) throws ParserCoerceException
	{
		this( subexp, new PyAction( a ) );
	}
	
	public Action(Object subexp, PyObject a, boolean bMergeUp) throws ParserCoerceException
	{
		this( subexp, new PyAction( a ), bMergeUp );
	}
	
	

	public ParseAction getAction()
	{
		return a;
	}
	
	public boolean getMergeUp()
	{
		return bMergeUp;
	}
	

	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		ParseResult res = subexp.handleNode( state, input );
		
		if ( res.isValid() )
		{
			return res.actionValue( this.a.invoke( input, 0, 1, res.value, res.bindings ), bMergeUp );
		}
		else
		{
			return res;
		}
	}

	protected ParseResult evaluateStringChars(ParserState state, String input, int start)
	{
		ParseResult res = subexp.handleStringChars( state, input, start );
		
		if ( res.isValid() )
		{
			return res.actionValue( this.a.invoke( input, start, res.end, res.value, res.bindings ), bMergeUp );
		}
		else
		{
			return res;
		}
	}

	protected ParseResult evaluateRichStringItems(ParserState state, RichStringAccessor input, int start)
	{
		ParseResult res = subexp.handleRichStringItems( state, input, start );
		
		if ( res.isValid() )
		{
			return res.actionValue( this.a.invoke( input, start, res.end, res.value, res.bindings ), bMergeUp );
		}
		else
		{
			return res;
		}
	}
	
	protected ParseResult evaluateListItems(ParserState state, List<Object> input, int start)
	{
		ParseResult res = subexp.handleListItems( state, input, start );
		
		if ( res.isValid() )
		{
			return res.actionValue( this.a.invoke( input, start, res.end, res.value, res.bindings ), bMergeUp );
		}
		else
		{
			return res;
		}
	}


	public boolean isEquivalentTo(ParserExpression x)
	{
		if ( x instanceof Action )
		{
			Action ax = (Action)x;
			return super.isEquivalentTo( x )  &&  a == ax.a  &&  bMergeUp == ax.bMergeUp;
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "Action( " + subexp.toString() + " -> " + a.toString() + ", " + bMergeUp + " )";
	}
	
	
	
	public static Action mergeUpAction(ParserExpression subexp, ParseAction a)
	{
		return new Action( subexp, a, true );
	}

	public static Action mergeUpAction(ParserExpression subexp, PyObject a)
	{
		return new Action( subexp, a, true );
	}
}
