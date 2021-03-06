//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Input;

import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.List;

public class Modifier
{
	public static final int CTRL = 0x1;
	public static final int SHIFT = 0x2;
	public static final int ALT = 0x4;
	public static final int ALT_GRAPH = 0x8;
	public static final int META = 0x10;
	public static final int BUTTON1 = 0x0100;
	public static final int BUTTON2 = 0x0200;
	public static final int BUTTON3 = 0x0400;
	public static final int BUTTON4 = 0x0800;
	public static final int BUTTON5 = 0x1000;
	public static final int BUTTON6 = 0x2000;
	public static final int BUTTON7 = 0x4000;
	public static final int BUTTON8 = 0x8000;

	private static int KEY_MODS_MASK = CTRL | SHIFT | ALT | ALT_GRAPH | META;
	private static int BUTTON_MODS_MASK = CTRL | SHIFT | ALT | ALT_GRAPH;

	public static final int _BUTTONS_SHIFT = 8;
	public static final int BUTTONS_MASK = 0xff00;
	
	
	public static boolean getButton(int modifiers, int button)
	{
		return ( ( ( modifiers & BUTTONS_MASK )  >>  _BUTTONS_SHIFT )  >>  button ) != 0;
	}

	public static int setButton(int modifiers, int button, boolean value)
	{
		int mask = 0x1 << _BUTTONS_SHIFT << button;
		if ( value )
		{
			return modifiers | mask;
		}
		else
		{
			return modifiers & ~mask;
		}
	}


	public static boolean isAButtonPressed(int modifiers)
	{
		return ( modifiers & BUTTONS_MASK ) != 0;
	}
	
	
	
	private static void addModifiersToString(StringBuilder b, int modifiers, int mask, String name)
	{
		if ( ( modifiers & mask ) != 0 )
		{
			if ( b.length() > 1 )
			{
				b.append( ", " );
			}
			b.append( name );
		}
	}
	
	public static String keyModifiersToString(int modifiers)
	{
		StringBuilder b = new StringBuilder( "<" );
		addModifiersToString( b, modifiers, CTRL, "CTRL" );
		addModifiersToString( b, modifiers, SHIFT, "SHIFT" );
		addModifiersToString( b, modifiers, ALT, "ALT" );
		addModifiersToString( b, modifiers, ALT_GRAPH, "ALT_GRAPH" );
		addModifiersToString( b, modifiers, META, "META" );
		b.append( '>' );
		return b.toString();
	}
	
	private static void addModifiersToStringList(List<String> b, int modifiers, int mask, String name)
	{
		if ( ( modifiers & mask ) != 0 )
		{
			b.add( name );
		}
	}
	
	public static List<String> getKeyModifierNames(int modifiers)
	{
		ArrayList<String> b = new ArrayList<String>();
		addModifiersToStringList( b, modifiers, CTRL, "CTRL" );
		addModifiersToStringList( b, modifiers, SHIFT, "SHIFT" );
		addModifiersToStringList( b, modifiers, ALT, "ALT" );
		addModifiersToStringList( b, modifiers, ALT_GRAPH, "ALT_GRAPH" );
		addModifiersToStringList( b, modifiers, META, "META" );
		return b;
	}

	
	
	public static int getKeyModifiersFromEvent(InputEvent e)
	{
		int modifiers = 0;
		
		if ( e.isControlDown() )
		{
			modifiers |= CTRL;
		}
		
		if ( e.isShiftDown() )
		{
			modifiers |= SHIFT;
		}
		
		if ( e.isAltDown() )
		{
			modifiers |= ALT;
		}
		
		if ( e.isAltGraphDown() )
		{
			modifiers |= ALT_GRAPH;
		}

		if ( e.isMetaDown() )
		{
			modifiers |= META;
		}
		
		return modifiers;
	}


	public static int maskKeyModifiers(int modifiers)
	{
		return modifiers & KEY_MODS_MASK;
	}

	public static int invMaskKeyModifiers(int modifiers)
	{
		return modifiers & ~KEY_MODS_MASK;
	}


	public static int maskButtonModifiers(int modifiers)
	{
		return modifiers & BUTTON_MODS_MASK;
	}

	public static int invMaskButtonModifiers(int modifiers)
	{
		return modifiers & ~BUTTON_MODS_MASK;
	}
}
