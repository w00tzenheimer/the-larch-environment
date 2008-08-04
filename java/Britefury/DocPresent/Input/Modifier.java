package Britefury.DocPresent.Input;

public class Modifier {
	public static int CTRL = 0x1;
	public static int SHIFT = 0x2;
	public static int ALT = 0x4;
	public static int BUTTON1 = 0x0100;
	public static int BUTTON2 = 0x0200;
	public static int BUTTON3 = 0x0400;
	public static int BUTTON4 = 0x0800;
	public static int BUTTON5 = 0x1000;
	public static int BUTTON6 = 0x2000;
	public static int BUTTON7 = 0x4000;
	public static int BUTTON8 = 0x8000;
	
	public static int _BUTTONS_SHIFT = 8;
	public static int _BUTTONS_MASK = 0xff00;
	
	
	public static boolean getButton(int modifiers, int button)
	{
		return ( ( ( modifiers & _BUTTONS_MASK )  >>  _BUTTONS_SHIFT )  >>  button ) != 0;
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
}