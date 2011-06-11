//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Inspect;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import org.python.core.PyDictionary;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.core.PyTuple;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Controls.DropDownExpander;
import BritefuryJ.DefaultPerspective.DefaultPerspective;
import BritefuryJ.DefaultPerspective.PrimitivePresenter;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.LineBreak;
import BritefuryJ.Pres.Primitive.Paragraph;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.StaticText;
import BritefuryJ.StyleSheet.StyleSheet;

public class Inspector
{
	public static Pres presentJavaFieldWithValue(Object x, Field field)
	{
		int modifiers = field.getModifiers();
		boolean isSmall = false;
		Object value = null;
		Pres valuePres = null;

		boolean isAccessible = field.isAccessible();
		field.setAccessible( true );
		try
		{
			value = field.get( x );
		}
		catch (IllegalArgumentException e1)
		{
			valuePres = errorStyle.applyTo( new Label( "<Illegal argument>" ) );
			isSmall = true;
		}
		catch (IllegalAccessException e1)
		{
			valuePres = errorStyle.applyTo( new Label( "<Cannot access>" ) );
			isSmall = true;
		}
		field.setAccessible( isAccessible );
		
		if ( valuePres == null )
		{
			// No exception thrown while getting field value
			if ( PrimitivePresenter.isPrimitive( value ) )
			{
				isSmall = PrimitivePresenter.isSmallPrimitive( value );
				valuePres = PrimitivePresenter.presentPrimitive( value );
			}
			else
			{
				valuePres = new InnerFragment( value );
			}
		}
		if ( isSmall )
		{
			return new Paragraph( new Pres[] {
					PrimitivePresenter.getModifierKeywords( modifiers ),
					PrimitivePresenter.presentJavaClassName( field.getType(), typeNameStyle ),
					space,
					new LineBreak(),
					PrimitivePresenter.getAccessNameStyle( modifiers ).applyTo( new Label( field.getName() ) ),
					space,
					valuePres } );
		}
		else
		{
			Pres header = new Paragraph( new Pres[] {
					PrimitivePresenter.getModifierKeywords( modifiers ),
					space,
					PrimitivePresenter.presentJavaClassName( field.getType(), typeNameStyle ),
					space,
					new LineBreak(),
					PrimitivePresenter.getAccessNameStyle( modifiers ).applyTo( new Label( field.getName() ) ) } );
			return new Column( new Pres[] { header, valuePres.padX( 45.0, 0.0 ) } );
		}
	}
	
	
	
	public static Pres presentJavaObjectInspector(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres asString = PrimitivePresenter.presentObjectAsString( x );

		ArrayList<Object> contents = new ArrayList<Object>();
		
		// Type
		Pres type = new DropDownExpander( sectionHeadingStyle.applyTo( new Label( "Type" ) ), DefaultPerspective.instance.applyTo( x.getClass() ) );
		contents.add( type );
		
		
		ArrayList<Object> fields = new ArrayList<Object>();
		Class<?> cls = x.getClass();
		while ( cls != null )
		{
			for (Field field: cls.getDeclaredFields())
			{
				int modifiers = field.getModifiers();
				if ( !Modifier.isStatic( modifiers ) )
				{
					boolean isSmall = false;
					Object value = null;
					Pres valuePres = null;
	
					boolean isAccessible = field.isAccessible();
					field.setAccessible( true );
					try
					{
						value = field.get( x );
					}
					catch (IllegalArgumentException e1)
					{
						valuePres = errorStyle.applyTo( new Label( "<Illegal argument>" ) );
						isSmall = true;
					}
					catch (IllegalAccessException e1)
					{
						valuePres = errorStyle.applyTo( new Label( "<Cannot access>" ) );
						isSmall = true;
					}
					field.setAccessible( isAccessible );
					
					if ( valuePres == null )
					{
						// No exception thrown while getting field value
						if ( PrimitivePresenter.isPrimitive( value ) )
						{
							isSmall = PrimitivePresenter.isSmallPrimitive( value );
							valuePres = PrimitivePresenter.presentPrimitive( value );
						}
						else
						{
							valuePres = new InnerFragment( value );
						}
					}
					if ( isSmall )
					{
						fields.add( new Row( new Pres[] {
								PrimitivePresenter.getModifierKeywords( modifiers ),
								PrimitivePresenter.presentJavaClassName( field.getType(), typeNameStyle ),
								space,
								PrimitivePresenter.getAccessNameStyle( modifiers ).applyTo( new Label( field.getName() ) ),
								space,
								valuePres } ) );
					}
					else
					{
						Pres header = new Row( new Pres[] {
								PrimitivePresenter.getModifierKeywords( modifiers ),
								PrimitivePresenter.presentJavaClassName( field.getType(), typeNameStyle ),
								space,
								PrimitivePresenter.getAccessNameStyle( modifiers ).applyTo( new Label( field.getName() ) ) } );
						fields.add( new Column( new Pres[] { header, valuePres.padX( 30.0, 0.0 ) } ) );
					}
				}
			}
			
			cls = cls.getSuperclass();
		}
		if ( fields.size() > 0 )
		{
			contents.add( new DropDownExpander( sectionHeadingStyle.applyTo( new Label( "Fields" ) ), new Column( fields ) ) );
		}
		
		Pres inspector = new Column( contents );
		return new DropDownExpander( asString, inspector );
	}
	
	public static Pres presentPythonObjectInspector(PyObject x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres asString = PrimitivePresenter.presentObjectAsString( x );

		ArrayList<Object> contents = new ArrayList<Object>();
		
		// Type
		Pres type = new DropDownExpander( sectionHeadingStyle.applyTo( new Label( "Type" ) ), DefaultPerspective.instance.applyTo( x.getType() ) );
		contents.add( type );
		
		
		// Attributes
		ArrayList<Object> attributes = new ArrayList<Object>();
		PyObject dict = x.fastGetDict();
		if ( dict != null )
		{
			PyList dictItems;
			if ( dict instanceof PyDictionary )
			{
				dictItems = ((PyDictionary)dict).items();
			}
			else if ( dict instanceof PyStringMap )
			{
				dictItems = ((PyStringMap)dict).items();
			}
			else
			{
				throw new RuntimeException( "Expected a PyDictionary or a PyStringMap when acquiring __dict__ from a PyObject" );
			}
			
			
			for (Object dictItem: dictItems)
			{
				PyTuple pair = (PyTuple)dictItem;
				PyObject key = pair.getArray()[0];
				PyObject value = pair.getArray()[1];
				String name = key.toString();
				
				if ( name.equals( "__dict__" ) )
				{
					break;
				}
				
				Pres namePres = attributeNameStyle.applyTo( new Label( name ) );
				Pres valuePres = null;
				boolean isSmall = false;
				
				if ( PrimitivePresenter.isPrimitivePy( value ) )
				{
					isSmall = PrimitivePresenter.isSmallPrimitivePy( value );
					valuePres = PrimitivePresenter.presentPrimitivePy( value );
				}
				else
				{
					valuePres = new InnerFragment( value );
					isSmall = false;
				}
				
				if ( isSmall )
				{
					attributes.add( new Row( new Pres[] { namePres, space, valuePres } ) );
				}
				else
				{
					attributes.add( new Column( new Pres[] { namePres, valuePres.padX( 15.0, 0.0 ) } ) );
				}
			}
		}
		
		if ( attributes.size() > 0 )
		{
			contents.add( new DropDownExpander( sectionHeadingStyle.applyTo( new Label( "Attributes" ) ),   new Column( attributes ) ) );
		}
		
		
		Pres inspector = new Column( contents );
		return new DropDownExpander( asString, inspector );
	}
	
	
	
	private static final StyleSheet staticStyle = StyleSheet.instance.withAttr( Primitive.editable, false );
	private static final StyleSheet labelStyle = StyleSheet.instance.withAttr( Primitive.editable, false ).withAttr( Primitive.selectable, false );

	private static final StyleSheet sectionHeadingStyle = labelStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.0f, 0.5f ) ).withAttr( Primitive.fontBold, true ).withAttr( Primitive.fontFace, "Serif" );
	private static final StyleSheet attributeNameStyle = labelStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.0f, 0.25f ) );

	private static final StyleSheet typeNameStyle = labelStyle.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.4f ) );


	private static final StyleSheet errorStyle = labelStyle.withAttr( Primitive.foreground, new Color( 0.5f, 0.0f, 0.0f ) ).withAttr( Primitive.fontBold, true ).withAttr( Primitive.fontFace, "Serif" );

	private static final Pres space = staticStyle.applyTo( new StaticText( " " ) );
}