//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.RichText;


public class EdInlineEmbed extends EdEmbed
{
	protected EdInlineEmbed(Object value)
	{
		super( value );
	}


	@Override
	protected EdNode deepCopy(RichTextEditor editor)
	{
		return new EdInlineEmbed( editor.deepCopyInlineEmbedValue( value ) );
	}


	@Override
	protected Object buildModel(RichTextEditor editor)
	{
		return editor.buildInlineEmbed( value );
	}
}