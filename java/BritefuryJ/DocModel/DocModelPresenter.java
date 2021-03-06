//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.DocModel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Controls.DropDownExpander;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Isolation.IsolationBarrier;
import BritefuryJ.Pres.CompositePres;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.ObjectPres.ObjectPresStyle;
import BritefuryJ.Pres.ObjectPres.ObjectBorder;
import BritefuryJ.Pres.ObjectPres.UnescapedStringAsRow;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Paragraph;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Span;
import BritefuryJ.Pres.Primitive.StaticText;
import BritefuryJ.Pres.Sequence.ParagraphSequenceView;
import BritefuryJ.Pres.Sequence.TrailingSeparator;
import BritefuryJ.Pres.Sequence.VerticalInlineSequenceView;
import BritefuryJ.Pres.Sequence.VerticalSequenceView;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;
import com.sun.org.apache.xpath.internal.functions.FuncFalse;


public class DocModelPresenter
{
	private static final StyleSheet defaultStyle = StyleSheet.style( Primitive.fontFace.as( "Sans serif" ), Primitive.fontSize.as( 14 ), Primitive.foreground.as( Color.black ), Primitive.paragraphIndentation.as( 60.0 ) );

	private static final StyleSheet nullStyle = defaultStyle.withValues( Primitive.fontItalic.as( true ), Primitive.foreground.as( new Color( 0.5f, 0.0f, 0.25f ) ) );

	private static final StyleSheet stringStyle = defaultStyle.withValues( ObjectPresStyle.stringContentStyle.as( StyleSheet.style( Primitive.foreground.as( new Color( 0.0f, 0.25f, 0.5f ) ) ) ) );

	private static final StyleSheet punctuationStyle = defaultStyle.withValues( Primitive.foreground.as( new Color( 0.0f, 0.0f, 1.0f ) ) );

	private static final StyleSheet classNameStyle = defaultStyle.withValues( Primitive.foreground.as( new Color( 0.0f, 0.5f, 0.0f ) ) );
	private static final StyleSheet schemaNameStyle = defaultStyle.withValues( Primitive.foreground.as( new Color( 0.0f, 0.0f, 0.5f ) ) );

	private static final StyleSheet embedStyle = defaultStyle.withValues( Primitive.foreground.as( new Color( 0.25f, 0.0f, 0.5f ) ), Primitive.fontItalic.as( true ) );

	private static final StyleSheet fieldNameStyle = defaultStyle.withValues( Primitive.foreground.as( new Color( 0.5f, 0.0f, 0.25f ) ) );

	
	
	private static final Pres space = new Label( " " );
	private static final Pres openBracket = punctuationStyle.applyTo( new Label( "[" ) );
	private static final Pres closeBracket = punctuationStyle.applyTo( new Label( "]" ) );
	private static final Pres openParen = punctuationStyle.applyTo( new Label( "(" ) );
	private static final Pres closeParen = punctuationStyle.applyTo( new Label( ")" ) );


	
	private static Pres present(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		if ( x == null )
		{
			return nullStyle.applyTo( new Label( "<null>" ) );
		}
		else if ( x instanceof String )
		{
			return stringStyle.applyTo( new UnescapedStringAsRow( (String )x ) );
		}
		else if ( x instanceof DMNode )
		{
			return new InnerFragment( x );
		}
		else
		{
			return presentEmbeddedObject( x, fragment, inheritedState );
		}
	}
	
	protected static Pres presentDMList(DMList node, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		List<Object> xViews = new ArrayList<Object>();
		for (Object x: node)
		{
			xViews.add( present( x, fragment, inheritedState ) );
		}
		
		// Check the contents, to determine the layout
		// Default: horizontal (paragraph layout)
		if ( node.size() > 0 )
		{
			for (Object x: node )
			{
				if ( !(x instanceof String) )
				{
					return new VerticalSequenceView( xViews, openBracket, closeBracket, null, space, TrailingSeparator.NEVER ).alignHPack().alignVRefY();
				}
			}
		}
		
		// Create a list view
		return new ParagraphSequenceView( xViews, openBracket, closeBracket, null, space, TrailingSeparator.NEVER ).alignHPack().alignVRefY();
	}
	
	
	protected static Pres presentDMObject(DMObject node, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		DMObjectClass cls = node.getDMObjectClass();
		
		// Check the contents, to determine the layout
		// Default: horizontal (paragraph layout)
		boolean presentVertically = false;
		for (int i = 0; i < cls.getNumFields(); i++)
		{
			Object value = node.get( i );
			if ( value != null )
			{
				// If we encounter a non-string value, then this object cannot be displayed in a single line
				if ( !( value instanceof String ) )
				{
					presentVertically = true;
					break;
				}
			}
		}
		
		// Header
		Pres header;
		Pres schemaName = schemaNameStyle.applyTo( new Label( cls.getSchema().getShortName() ) );
		Pres className = classNameStyle.applyTo( new Label( cls.getName() ) );
		if ( presentVertically )
		{
			header = defaultStyle.applyTo( new Paragraph( new Object[] { schemaName, punctuationStyle.applyTo( new Label( "." ) ), className,
					new Label( " " ), punctuationStyle.applyTo( new Label( ":" ) ) } ) );
		}
		else
		{
			header = defaultStyle.applyTo( new Span( new Object[] { schemaName, punctuationStyle.applyTo( new Label( "." ) ), className,
					new Label( " " ), punctuationStyle.applyTo( new Label( ":" ) ) } ) );
		}

		ArrayList<Object> itemViews = new ArrayList<Object>();
		itemViews.add( header );
		// Create views of each item
		for (int i = 0; i < cls.getNumFields(); i++)
		{
			Object value = node.get( i );
			String fieldName = cls.getField( i ).getName();
			if ( value != null )
			{
				Pres line;
				if ( presentVertically )
				{
					line = defaultStyle.applyTo( new Paragraph( new Object[] { fieldNameStyle.applyTo( new StaticText( fieldName ) ), punctuationStyle.applyTo( new StaticText( "=" ) ),
							 present( value, fragment, inheritedState ) } ) );
				}
				else
				{
					line = defaultStyle.applyTo( new Span( new Object[] { fieldNameStyle.applyTo( new StaticText( fieldName ) ), punctuationStyle.applyTo( new StaticText( "=" ) ),
							 present( value, fragment, inheritedState ) } ) );
				}
				itemViews.add( line );
			}
		}
				
		// Create the layout
		if ( presentVertically )
		{
			return new VerticalInlineSequenceView( itemViews, openParen, closeParen, null, space, TrailingSeparator.NEVER ).alignHPack().alignVRefY();
		}
		else
		{
			return new ParagraphSequenceView( itemViews, openParen, closeParen, null, space, TrailingSeparator.NEVER ).alignHPack().alignVRefY();
		}
	}


	protected static Pres presentEmbeddedObject(Object node, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return new ObjectBorder( new DropDownExpander( embedStyle.applyTo( new Label( "Embedded object" ) ), node ) );
	}
	

	public static Pres presentDMNode(DMNode node, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		if ( node instanceof DMObject )
		{
			return presentDMObject( (DMObject)node, fragment, inheritedState );
		}
		else if ( node instanceof DMList )
		{
			return presentDMList( (DMList)node, fragment, inheritedState );
		}
		else
		{
			throw new RuntimeException( "Unknown DMNode type" );
		}
	}
}
