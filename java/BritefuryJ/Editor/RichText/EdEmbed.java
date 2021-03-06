//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.RichText;

import java.awt.Color;
import java.util.List;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Region;
import BritefuryJ.StyleSheet.StyleSheet;

abstract class EdEmbed extends EdNode
{
	protected Object value;
	
	
	protected EdEmbed(Object value)
	{
		this.value = value;
	}
	
	
	public Object getValue()
	{
		return value;
	}

	public void setValue(Object value)
	{
		this.value = value;
	}
	
	
	
	@Override
	protected void buildTagList(List<Object> tags)
	{
		tags.add( this );
	}
	
	
	
	protected boolean isTextual()
	{
		return false;
	}



	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return new Region( _paraStyle.applyTo( new Border( value ) ) );
	}


	private static final StyleSheet _paraStyle = StyleSheet.style( Primitive.border.as( new SolidBorder( 2.0, 2.0, new Color( 0.0f, 0.5f, 0.0f ), null ) ) );
}
