//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StructuralRepresentation;

import BritefuryJ.Parser.ItemStream.ItemStream;
import BritefuryJ.Parser.ItemStream.ItemStreamBuilder;

public class StructuralValueStream extends StructuralValue
{
	private ItemStream value;
	
	
	public StructuralValueStream(ItemStream value)
	{
		this.value = value;
	}
	
	
	public void addToStream(ItemStreamBuilder builder)
	{
		builder.extend( value );
	}


	public Object getValue()
	{
		return value;
	}
}