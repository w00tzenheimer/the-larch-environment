//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DefaultPerspective.Presenters;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.ObjectPresentation.ObjectPresenter;
import BritefuryJ.ObjectPresentation.ObjectPresenterRegistry;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.ObjectBorder;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.LineBreak;
import BritefuryJ.Pres.Primitive.LineBreakCostSpan;
import BritefuryJ.Pres.Primitive.Paragraph;
import BritefuryJ.Pres.Primitive.ParagraphDedentMarker;
import BritefuryJ.Pres.Primitive.ParagraphIndentMarker;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;

public class PresentersJericho extends ObjectPresenterRegistry
{
	public PresentersJericho()
	{
		registerJavaObjectPresenter( Source.class, presenter_Source );
		registerJavaObjectPresenter( Element.class, presenter_Element );
	}

	
	public static final ObjectPresenter presenter_Source = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			Source source = (Source)x;
			
			Pres header = htmlSourceStyle.applyTo( new Label( "HTML Source" ) ).pad( 2.0, 2.0 ).alignHExpand();
			
			List<Element> children = source.getChildElements();
			Pres childPres = new Column( children.toArray() );
			
			return new ObjectBorder( new Column( new Pres[] { header, childPres.padX( 15.0, 0.0 ) } ) );
		}
	};


	public static final ObjectPresenter presenter_Element = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			Element element = (Element)x;
			
			Pres tag = tagStyle.applyTo( new Label( element.getName() ) );
			List<Pres> headerElements = new ArrayList<Pres>();
			headerElements.add( tag );
			Attributes attrs = element.getAttributes();
			if ( attrs != null )
			{
				headerElements.add( new Label( " " ) );
				headerElements.add( new ParagraphIndentMarker() );
				boolean first = true;
				for (Attribute attr: element.getAttributes())
				{
					if ( !first )
					{
						headerElements.add( new Label( " " ) );
						headerElements.add( new LineBreak() );
					}
					presentAttribute( headerElements, attr, fragment, inheritedState );
					first = false;
				}
				headerElements.add( new ParagraphDedentMarker() );
			}
			Pres header = new Paragraph( headerElements.toArray() );
			
			List<Element> children = element.getChildElements();
			Pres childPres = new Column( children.toArray() );

			return new Column( new Pres[] { header, childPres.padX( 15.0, 0.0 ) } );
		}
	};
	
	
	private static final Pattern whitespacePattern = Pattern.compile( "[ ]+" );
	
	private static void presentAttribute(List<Pres> p, Attribute attr, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres name = attrNameStyle.applyTo( new Label( attr.getKey() ) );
		Pres eq = attrPunctuationStyle.applyTo( new Label( "=" ) );
		String valueStrings[] = whitespacePattern.split( attr.getValue() );
		ArrayList<Pres> valueElements = new ArrayList<Pres>();
		boolean first = true;
		for (String v: valueStrings)
		{
			if ( !first )
			{
				valueElements.add( new Label( " " ) );
				valueElements.add( new LineBreak() );
			}
			valueElements.add( new Label( v ) );
			first = false;
		}
		Pres value = attrValueStyle.applyTo( new LineBreakCostSpan( valueElements.toArray() ) );
		p.add( name );
		p.add( eq );
		p.add( value );
	}

	
	
	private static final StyleSheet htmlSourceStyle = StyleSheet.instance.withAttr( Primitive.fontBold, true ).withAttr( Primitive.foreground, new Color( 0.2f, 0.4f, 0.6f ) ).withAttr(
			Primitive.background, new FillPainter( new Color( 0.85f, 0.85f, 0.85f ) ) );
	private static final StyleSheet tagStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.0f, 0.2f, 0.5f ) );
	private static final StyleSheet attrNameStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.4f, 0.4f, 0.4f ) );
	private static final StyleSheet attrPunctuationStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.3f, 0.4f, 0.3f ) );
	private static final StyleSheet attrValueStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.2f, 0.4f, 0.2f ) );
}