//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;

import java.util.Map;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.ParserHelpers.TraceNode;
import BritefuryJ.ParserHelpers.TracedParseResultInterface;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.HorizontalField;
import BritefuryJ.Pres.ObjectPres.ObjectBoxWithFields;
import BritefuryJ.Pres.ObjectPres.VerticalField;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Paragraph;
import BritefuryJ.Pres.Primitive.StaticText;

public class TracedParseResult extends ParseResult implements Presentable
{
	public static class ParseTrace implements TracedParseResultInterface, Presentable
	{
		private TracedParseResult parseResult;
		
		
		private ParseTrace(TracedParseResult parseResult)
		{
			this.parseResult = parseResult;
		}

		
		@Override
		public TraceNode getTraceNode()
		{
			return parseResult.traceNode;
		}


		@Override
		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return TraceGraphViewer.traceView( this, fragment.persistentState( "viewport" ) );
		}
	}

	
	protected TraceNode traceNode;
	private ParseTrace trace;
	
	
	protected TracedParseResult(Object value, int begin, int end, boolean bSuppressed, boolean bValid, boolean bMerge, Map<String, Object> bindings, TraceNode traceNode)
	{
		super( value, begin, end, bSuppressed, bValid, bMerge, bindings );
		this.traceNode = traceNode;
		trace = new ParseTrace( this );
	}
	
	
	
	public ParseTrace getTrace()
	{
		return trace;
	}
	

	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres fields[];
		
		if ( isValid() )
		{
			Pres status = parseResultStyle.applyTo( new HorizontalField( "Status:", successStyle.applyTo( new Label( "Success" ) ) ) );
			Pres range = parseResultStyle.applyTo( new HorizontalField( "Range:",
					new Paragraph( new Pres[] { 
							rangeStyle.applyTo( new StaticText( String.valueOf( getBegin() ) ) ),
							new StaticText( " to " ),
							rangeStyle.applyTo( new StaticText( String.valueOf( getEnd() ) ) ) } ) ) );
			
			Pres valueView = new InnerFragment( getValue() );
			Pres value = parseResultStyle.applyTo( new VerticalField( "Value:", valueView ) );
			Pres trace = parseResultStyle.applyTo( new VerticalField( "Trace:", TraceGraphViewer.traceView( this.trace, fragment.persistentState( "viewport" ) ) ) );
			fields = new Pres[] { status, range, value, trace.alignHExpand().alignVExpand() };
		}
		else
		{
			Pres status = parseResultStyle.applyTo( new HorizontalField( "Status:", failStyle.applyTo( new StaticText( "Fail" ) ) ) );
			Pres trace = parseResultStyle.applyTo( new VerticalField( "Trace:", TraceGraphViewer.traceView( this.trace, fragment.persistentState( "viewport" ) ) ) );
			fields = new Pres[] { status, trace.alignHExpand().alignVExpand() };
		}
		
		return parseResultStyle.applyTo( new ObjectBoxWithFields( "BritefuryJ.Parser.ParseResult", fields ).alignHExpand() );
	}
}
