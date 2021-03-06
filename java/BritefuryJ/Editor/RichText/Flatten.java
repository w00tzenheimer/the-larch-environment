//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.RichText;

import java.awt.Color;
import java.util.*;
import java.util.regex.Pattern;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.Editor.RichText.Attrs.RichTextAttributes;
import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;

public class Flatten
{
	private static class FlattenInput
	{
		private List<Object> xs;
		private int pos;


		private FlattenInput(List<Object> xs)
		{
			this.xs = xs;
			this.pos = 0;
		}


		private boolean matches(Class<?> cls)
		{
			return pos < xs.size()  &&  cls.isInstance( xs.get( pos ) );
		}

		private boolean matches(Class<?> ...cls)
		{
			if ( pos <= xs.size() - cls.length )
			{
				int p = pos;
				for (Class<?> c: cls)
				{
					if ( !c.isInstance( xs.get( p  ) ) )
					{
						return false;
					}
					p++;
				}
				return true;
			}
			else
			{
				return false;
			}
		}

		private List<Object> consume(int n)
		{
			List<Object> result = xs.subList( pos, pos + n );
			pos += n;
			return result;
		}

		private Object consume()
		{
			Object result = xs.get( pos );
			pos++;
			return result;
		}


		private boolean isAtStart()
		{
			return pos == 0;
		}

		private boolean hasMoreContent()
		{
			return pos < xs.size();
		}
	}




	protected static class Newline implements Presentable
	{
		@Override
		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return style.applyTo( new Border( new Label( "<newline>" ) ) );
		}

		private static final StyleSheet style = StyleSheet.style( Primitive.border.as( new SolidBorder( 1.0, 1.0, 5.0, 5.0, Color.BLACK, new Color( 0.85f, 0.85f, 0.95f ) ) ), Primitive.fontBold.as( true ), Primitive.fontItalic.as( true ) );
		
		
		protected static final Newline instance = new Newline();
	}
	
	
	private static Pattern newlinePattern = Pattern.compile( "\n" );


	// Replace newline characters in strings with newline tokens
	// SHOULD IMPLEMENT AS ITERATOR, BUT I'M BUGGERED IF I AM GOING TO SPEND TIME CONVERTING A NICE PYTHON GENERATOR TO A JAVA ITERATOR......
	private static ArrayList<Object> newlineSplit(Iterable<Object> xs)
	{
		ArrayList<Object> result = new ArrayList<Object>();
		for (Object x: xs)
		{
			if ( x instanceof String  &&  ((String)x).contains( "\n" ) )
			{
				String lines[] = newlinePattern.split( (String)x, -1 );
				result.add( lines[0] );
				for (int i = 1; i < lines.length; i++)
				{
					result.add( Newline.instance );
					String line = lines[i];
					if ( line.length() > 0 )
					{
						result.add( line );
					}
				}
			}
			else
			{
				result.add( x );
			}
		}
		return result;
	}

	// Join adjacent string with one another
	// SHOULD IMPLEMENT AS ITERATOR, BUT I'M BUGGERED IF I AM GOING TO SPEND TIME CONVERTING A NICE PYTHON GENERATOR TO A JAVA ITERATOR......
	private static ArrayList<Object> textJoin(Iterable<Object> xs)
	{
		ArrayList<Object> result = new ArrayList<Object>();
		StringBuilder text = new StringBuilder();

		for (Object x: xs)
		{
			if ( x instanceof String )
			{
				text.append( (String)x );
			}
			else
			{
				if ( text.length() > 0 )
				{
					result.add( text.toString() );
					text = new StringBuilder();
				}
				result.add( x );
			}
		}

		if ( text.length() > 0 )
		{
			result.add( text.toString() );
		}
		return result;
	}


	private static boolean isPara(Object x)
	{
		return x instanceof EdNode  &&  ((EdNode)x).isParagraph();
	}

	private static boolean isTextPara(Object x)
	{
		return x instanceof EdParagraph;
	}

	private static boolean isParaEmbed(Object x)
	{
		return x instanceof EdParagraphEmbed;
	}

	private static boolean isStyleSpan(Object x)
	{
		return x instanceof EdSpan;
	}
	
	// Create a flattened version of the sequence of tags and strings in @xs
	// Paragraph start tags (TagPStart) remain
	// Newlines are converted to paragraph start tags (TagPStart)
	// Strings are wrapped in EdSpan objects, with styles determined by the style span start and end tags (TagSStart and TagSEnd)
	// Paragraphs (editor model paragraphs) that have not been 'flattened out' but remain as structural items are left as is.
	// SHOULD IMPLEMENT AS ITERATOR, BUT I'M BUGGERED IF I AM GOING TO SPEND TIME CONVERTING A NICE PYTHON GENERATOR TO A JAVA ITERATOR......
	private static void flatten(ArrayList<Object> result, FlattenInput xs, RichTextAttributes currentStyleAttrs)
	{
		Stack<RichTextAttributes> styleStack = new Stack<RichTextAttributes>();
		styleStack.add( new RichTextAttributes() );
		Object prevElement = null;

		// There are 9 possible types of element encountered in xs:
		//
		// String: text
		// Newline: \n
		// EdInlineEmbed: value as inline
		// EdParagraphEmbed: value as paragraph
		// EdParagraph: complete paragraph
		// EdSpan: complete span
		// TagPStart: start of paragraph
		// TagSStart: start of span
		// TagSEnd: end of span

		while ( xs.hasMoreContent() )
		{
			if ( xs.matches( Newline.class, EdParagraphEmbed.class ) )
			{
				// Newline followed by a paragraph embed

				// If the newline is the first element, then the user has attempted to insert a newline before the paragraph embed, so emit
				// a paragraph start tag
				// otherwise, it is the end of one paragraph, just before the embed, so suppress it
				if ( xs.isAtStart() )
				{
					result.add( new TagPStart( null ) );
				}
				xs.consume();
				result.add( xs.consume() );
			}
			else if ( xs.matches( Newline.class, TagPStart.class )  ||
					xs.matches( Newline.class, EdParagraph.class ) )
			{
				// Newline followed by:
				// - paragraph start tag
				// - paragraph

				// End of one paragraph and the beginning of another
				// Discard the newline and emit the start tag or paragraph
				xs.consume();
				result.add( xs.consume() );
			}
			else if ( xs.matches( Newline.class, String.class )  ||
					xs.matches( Newline.class, EdInlineEmbed.class )  ||
					xs.matches( Newline.class, TagSStart.class )  ||
					xs.matches( Newline.class, TagSEnd.class )  ||
					xs.matches( Newline.class, EdSpan.class )  ||
					xs.matches( Newline.class, Newline.class ) )
			{
				// Newline followed by text content

				// The user has inserted a newline to split the paragraph
				// Consume ONLY the newline and emit a paragraph start tag
				xs.consume();
				result.add( new TagPStart( null ) );
			}
			else if ( xs.matches( Newline.class ) )
			{
				// Newline

				// Should be at the end; this should be the last element. If not, something has gone wrong.

				// Discard
				xs.consume();
				if ( xs.hasMoreContent() )
				{
					throw new RuntimeException( "Should have reached the end" );
				}
			}
			else if ( xs.matches( TagPStart.class, EdParagraphEmbed.class )  ||
					xs.matches( TagPStart.class, EdParagraph.class ) )
			{
				// Paragraph start tag followed by:
				// - paragraph embed
				// - paragraph

				// Eliminate the paragraph start tag
				xs.consume();
				result.add( xs.consume() );
			}
			else if ( xs.matches( TagPStart.class, String.class )  ||
					xs.matches( TagPStart.class, EdInlineEmbed.class )  ||
					xs.matches( TagPStart.class, TagSStart.class )  ||
					xs.matches( TagPStart.class, EdSpan.class )  ||
					xs.matches( TagPStart.class, Newline.class ) )
			{
				// Paragraph start tag followed by text content

				// Consume and emit only the paragraph start tag
				// Leave the next element until next time round the loop
				result.add( xs.consume() );
			}
			else if ( xs.matches( EdParagraphEmbed.class, Newline.class ) )
			{
				// Paragraph embed followed by newline
				//
				// User has inserted a newline after a paragraph embed
				//
				// Emit the paragraph embed, and a paragraph start tag
				result.add( xs.consume() );
				xs.consume();
				result.add( new TagPStart( null ) );
			}
			else if ( xs.matches( String.class )  ||  xs.matches( EdInlineEmbed.class ) )
			{
				// Textual content; wrap in a style span
				Object x = xs.consume();
				result.add( new EdSpan( Arrays.asList( new Object[] { x } ), currentStyleAttrs ) );

				// Text content followed by paragraph start, with no newline in between
				// The user has deleted the newline in an attempt to join paragraphs
				// Discard the paragraph start
				if ( xs.matches( TagPStart.class ) )
				{
					xs.consume();
				}
			}
			else if ( xs.matches( TagSStart.class ) )
			{
				// Span start tag; put attributes onto stack
				TagSStart tag = (TagSStart)xs.consume();
				// Update the style stack
				RichTextAttributes attrs = currentStyleAttrs.concatenate(tag.getSpanAttrs());
				currentStyleAttrs = attrs;
				styleStack.add( attrs );

				// Text content followed by paragraph start, with no newline in between
				// The user has deleted the newline in an attempt to join paragraphs
				// Discard the paragraph start
				if ( xs.matches( TagPStart.class ) )
				{
					xs.consume();
				}
			}
			else if ( xs.matches( TagSEnd.class ) )
			{
				// Span end tag; pop attributes from stack
				xs.consume();
				// Update the style stack
				styleStack.pop();
				currentStyleAttrs = styleStack.lastElement();

				// Text content followed by paragraph start, with no newline in between
				// The user has deleted the newline in an attempt to join paragraphs
				// Discard the paragraph start
				if ( xs.matches( TagPStart.class ) )
				{
					xs.consume();
				}
			}
			else if ( xs.matches( EdSpan.class ) )
			{
				// Style span; process recursively
				EdSpan span = (EdSpan)xs.consume();
				RichTextAttributes attrs = currentStyleAttrs.concatenate(span.getSpanAttrs());
				flatten( result, new FlattenInput( span.getContents() ), attrs );

				// Text content followed by paragraph start, with no newline in between
				// The user has deleted the newline in an attempt to join paragraphs
				// Discard the paragraph start
				if ( xs.matches( TagPStart.class ) )
				{
					xs.consume();
				}
			}
			else if ( xs.matches( TagPStart.class ) )
			{
				// Lone paragraph start tag; suppress
				xs.consume();
			}
			else if ( xs.matches( EdParagraph.class )  ||  xs.matches( EdParagraphEmbed.class ) )
			{
				// Paragraph start
				result.add( xs.consume() );
			}
			else
			{
				throw new RuntimeException( "Could not process element " + xs.consume().getClass().getName() );
			}
		}
	}

	private static ArrayList<Object> flatten(List<Object> xs)
	{
		ArrayList<Object> result = new ArrayList<Object>();
		RichTextAttributes currentStyleAttrs = new RichTextAttributes();
		flatten( result, new FlattenInput( xs ), currentStyleAttrs );
		return result;
	}
	
	
	protected static ArrayList<Object> flattenParagraphs(List<Object> xs)
	{
		return flatten( textJoin( newlineSplit( xs ) ) );
	}
}
