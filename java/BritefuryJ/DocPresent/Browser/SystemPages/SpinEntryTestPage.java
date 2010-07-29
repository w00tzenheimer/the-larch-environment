//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import BritefuryJ.Controls.IntSpinEntry;
import BritefuryJ.Controls.RealSpinEntry;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.HBox;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.SpaceBin;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;
import BritefuryJ.DocPresent.Combinators.RichText.Body;
import BritefuryJ.DocPresent.Combinators.RichText.Heading2;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;

public class SpinEntryTestPage extends SystemPage
{
	protected SpinEntryTestPage()
	{
		register( "tests.controls.spinentry" );
	}
	
	
	public String getTitle()
	{
		return "Spin entry test";
	}
	
	protected String getDescription()
	{
		return "Spin entry control: edit a numeric value";
	}
	
	
	private class RealSpinEntryTextChanger implements RealSpinEntry.RealSpinEntryListener
	{
		private DPText textElement;
		
		
		public RealSpinEntryTextChanger(DPText textElement)
		{
			this.textElement = textElement;
		}


		public void onSpinEntryValueChanged(RealSpinEntry.RealSpinEntryControl spinEntry, double value)
		{
			textElement.setText( String.valueOf( value ) );
		}
	}

	

	private class IntSpinEntryTextChanger implements IntSpinEntry.IntSpinEntryListener
	{
		private DPText textElement;
		
		
		public IntSpinEntryTextChanger(DPText textElement)
		{
			this.textElement = textElement;
		}


		public void onSpinEntryValueChanged(IntSpinEntry.IntSpinEntryControl spinEntry, int value)
		{
			textElement.setText( String.valueOf( value ) );
		}
	}

	

	protected DPElement createContents()
	{
		Pres realValueTextPres = new StaticText( "0.0" );
		DPText realValueText = (DPText)realValueTextPres.present();
		Pres intValueTextPres = new StaticText( "0" );
		DPText intValueText = (DPText)intValueTextPres.present();
		RealSpinEntryTextChanger realListener = new RealSpinEntryTextChanger( realValueText );
		IntSpinEntryTextChanger intListener = new IntSpinEntryTextChanger( intValueText );
		RealSpinEntry realSpinEntry = new RealSpinEntry( 0.0, -100.0, 100.0, 1.0, 10.0, realListener );
		IntSpinEntry intSpinEntry = new IntSpinEntry( 0, -100, 100, 1, 10, intListener );
		Pres realLine = StyleSheet2.instance.withAttr( Primitive.hboxSpacing, 20.0 ).applyTo( new HBox( new Object[] { new StaticText( "Real number: " ),
				new SpaceBin( realSpinEntry.alignHExpand(), 100.0, -1.0 ), realValueText } ).padX( 5.0 ) );
		Pres intLine = StyleSheet2.instance.withAttr( Primitive.hboxSpacing, 20.0 ).applyTo( new HBox( new Object[] { new StaticText( "Integer: " ),
				new SpaceBin( intSpinEntry.alignHExpand(), 100.0, -1.0 ), intValueText } ).padX( 5.0 ) );
		Pres spinEntrySectionContents = new VBox( new Pres[] { realLine, intLine } );
		
		return new Body( new Pres[] { new Heading2( "Spin entries" ), spinEntrySectionContents } ).present();
	}
}
