//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.IncrementalUnit.Editor;

import java.awt.Color;
import java.util.WeakHashMap;

import javax.swing.SwingUtilities;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;
import BritefuryJ.IncrementalUnit.LiteralUnit;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;

public abstract class LiteralUnitEditor implements Presentable, IncrementalMonitorListener
{
	private static final StyleSheet errorStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.8f, 0.0f, 0.0f ) );
	
	protected abstract class Editor
	{
		private boolean bSettingCellValue = false;
		private LiteralUnit presCell = new LiteralUnit();
		private Pres pres = presCell.defaultPerspectiveValuePresInFragment();

		
		
		protected abstract void refreshEditor();
		
		
		protected Pres getPres()
		{
			return pres;
		}
		
		
		protected void setPres(Pres p)
		{
			presCell.setLiteralValue( p );
		}
		
		protected void error(String message)
		{
			setPres( errorStyle.applyTo( new Label( "<" + message + ">" ) ) );
		}
		
		
		protected void onCellChanged()
		{
			if ( !bSettingCellValue )
			{
				Runnable run = new Runnable()
				{
					@Override
					public void run()
					{
						refreshEditor();
					}
				};
				
				SwingUtilities.invokeLater( run );
			}
		}
		
		protected void setCellValue(Object value)
		{
			bSettingCellValue = true;
			cell.setLiteralValue( value );
			bSettingCellValue = false;
		}
	};
	
	
	protected LiteralUnit cell;
	protected WeakHashMap<Editor, Object> editors = new WeakHashMap<Editor, Object>();
	
	
	public LiteralUnitEditor(LiteralUnit cell)
	{
		this.cell = cell;
		this.cell.addListener( this );
	}
	
	
	protected abstract Editor createEditor();
	

	protected <V extends Object> V getCellValue(Class<V> valueClass)
	{
		Object v = cell.getLiteralValue();
		
		if ( v == null )
		{
			return null;
		}
		
		V typedV = null;
		try
		{
			typedV = valueClass.cast( v );
		}
		catch (ClassCastException e)
		{
			return null;
		}
		
		return typedV;
	}
	
	protected <V extends Object> V getCellValueNonNull(Class<V> valueClass, V defaultValue)
	{
		Object v = cell.getLiteralValue();
		
		if ( v == null )
		{
			return defaultValue;
		}
		
		V typedV = null;
		try
		{
			typedV = valueClass.cast( v );
		}
		catch (ClassCastException e)
		{
			return defaultValue;
		}
		
		return typedV;
	}
	

	
	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return createEditor().getPres();
	}


	@Override
	public void onIncrementalMonitorChanged(IncrementalMonitor inc)
	{
		for (Editor editor: editors.keySet())
		{
			editor.onCellChanged();
		}
	}
}