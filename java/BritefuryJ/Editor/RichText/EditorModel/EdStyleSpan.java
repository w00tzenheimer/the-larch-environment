//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.RichText.EditorModel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.Editor.RichText.Tags.SEnd;
import BritefuryJ.Editor.RichText.Tags.SStart;
import BritefuryJ.Editor.RichText.Tags.Tag;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Table;
import BritefuryJ.StyleSheet.StyleSheet;

public class EdStyleSpan extends EdAbstractText
{
	private HashMap<Object, Object> styleAttrs = new HashMap<Object, Object>();
	
	
	public EdStyleSpan(List<Object> contents, Map<Object, Object> styleAttrs)
	{
		super( contents );
		this.styleAttrs.putAll( styleAttrs );
	}
	
	public EdStyleSpan(Map<Object, Object> styleAttrs)
	{
		super();
		this.styleAttrs.putAll( styleAttrs );
	}
	
	
	public HashMap<Object, Object> getStyleAttrs()
	{
		return styleAttrs;
	}
	
	public void setStyleAttrs(HashMap<Object, Object> styleAttrs)
	{
		this.styleAttrs.clear();
		this.styleAttrs.putAll( styleAttrs );
	}
	
	
	
	
	
	
	@Override
	public Tag startTag()
	{
		return new SStart( styleAttrs );
	}

	@Override
	public Tag endTag()
	{
		return new SEnd();
	}


	@Override
	public void buildTagList(List<Object> tags)
	{
		tags.add( new SStart( styleAttrs ) );
		for (Object x: contents)
		{
			if ( x instanceof EdAbstractText )
			{
				((EdNode)x).buildTagList( tags );
			}
			else
			{
				tags.add( x );
			}
		}
		tags.add( new SEnd() );
	}

	
	
	@Override
	protected EdNode withContents(List<Object> contents)
	{
		return new EdStyleSpan( contents, styleAttrs );
	}
	
	
	protected static Pres presentStyleAttrs(HashMap<Object, Object> styleAttrs)
	{
		ArrayList<Pres[]> tableContents = new ArrayList<Pres[]>();
		for (Map.Entry<Object, Object> e: styleAttrs.entrySet())
		{
			tableContents.add( new Pres[] { Pres.coerce( e.getKey() ), Pres.coerce( e.getValue() ) } );
		}
		Pres[][] tabCells = tableContents.toArray( new Pres[tableContents.size()][] );
		return attrStyle.applyTo( new Table( tabCells ) );
	}

	
	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return new Border( new Column( new Object[] { presentContents(), presentStyleAttrs( styleAttrs ) } ) );
	}

	
	private static final StyleSheet attrStyle = StyleSheet.instance.withAttr( Primitive.background, new FillPainter( new Color( 0.85f, 0.95f, 1.0f ) ) );
}
