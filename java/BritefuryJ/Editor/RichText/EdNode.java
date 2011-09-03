//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.RichText;

import java.util.List;

import BritefuryJ.DefaultPerspective.Presentable;

public abstract class EdNode implements Presentable
{
	protected Tag regionStartTag()
	{
		return null;
	}

	protected Tag regionEndTag()
	{
		return null;
	}

	protected Tag prefixTag()
	{
		return null;
	}

	protected Tag suffixTag()
	{
		return null;
	}

	protected abstract void buildTagList(List<Object> tags);
	
	
	protected abstract EdNode deepCopy(RichTextEditor editor);
	
	
	protected abstract boolean isTextual();
	
	protected void buildTextualValue(StringBuilder builder)
	{
		throw new RuntimeException( "Contents are not purely textual" );
	}
	
	
	protected abstract Object buildModel(RichTextEditor editor);
	
	
	protected boolean isParagraph()
	{
		return false;
	}
}