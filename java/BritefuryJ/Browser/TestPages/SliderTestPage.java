//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Browser.TestPages;

import BritefuryJ.Controls.*;
import BritefuryJ.Live.LiveFunction;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.SpaceBin;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.UI.Section;
import BritefuryJ.Pres.UI.SectionHeading2;
import BritefuryJ.StyleSheet.StyleSheet;

public class SliderTestPage extends TestPage
{
	protected SliderTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Slider test";
	}
	
	protected String getDescription()
	{
		return "Slider control: edit a numeric value";
	}


    private LiveFunction realRangeLowerLiveFn(final LiveValue rangeLive) {
        return new LiveFunction(new LiveFunction.Function() {
            public Object evaluate() {
                double arr[] = (double[])rangeLive.getValue();
                return arr[0];
            }
        });
    }
	
    private LiveFunction realRangeUpperLiveFn(final LiveValue rangeLive) {
        return new LiveFunction(new LiveFunction.Function() {
            public Object evaluate() {
                double arr[] = (double[])rangeLive.getValue();
                return arr[1];
            }
        });
    }

    private LiveFunction intRangeLowerLiveFn(final LiveValue rangeLive) {
        return new LiveFunction(new LiveFunction.Function() {
            public Object evaluate() {
                int arr[] = (int[])rangeLive.getValue();
                return arr[0];
            }
        });
    }

    private LiveFunction intRangeUpperLiveFn(final LiveValue rangeLive) {
        return new LiveFunction(new LiveFunction.Function() {
            public Object evaluate() {
                int arr[] = (int[])rangeLive.getValue();
                return arr[1];
            }
        });
    }

	protected Pres createContents()
	{
		LiveValue realValue = new LiveValue( -5.0 );
		LiveValue intValue = new LiveValue( -6 );
        final LiveValue realRangeValue = new LiveValue(new double[]{-5.0, 5.0});
        final LiveValue intRangeValue = new LiveValue(new int[]{-6, 6});
		RealSlider realSlider = new RealSlider( realValue, -10.0, 10.0, 0.5, 300.0 );
		IntSlider intSlider = new IntSlider( intValue, -10, 10, 2, 300.0 );
        ProgressBar progress = new ProgressBar(realValue, -10.0, 10.0, 300.0);
        RealRangeSlider realRangeSlider = new RealRangeSlider(realRangeValue, -10.0, 10.0, 0.5, 300.0);
        IntRangeSlider intRangeSlider = new IntRangeSlider(intRangeValue, -10, 10, 2, 300.0);

        LiveFunction realRangeLower = realRangeLowerLiveFn(realRangeValue);
        LiveFunction realRangeUpper = realRangeUpperLiveFn(realRangeValue);
        LiveFunction intRangeLower = intRangeLowerLiveFn(intRangeValue);
        LiveFunction intRangeUpper = intRangeUpperLiveFn(intRangeValue);


		Pres realLine = StyleSheet.style( Primitive.rowSpacing.as( 20.0 ) ).applyTo( new Row( new Object[] { new Label( "Real number: " ),
			    new SpaceBin( 200.0, -1.0, realSlider.alignHExpand() ).alignVCentre(), realValue } ).padX( 5.0 ) );
		Pres intLine = StyleSheet.style( Primitive.rowSpacing.as( 20.0 ) ).applyTo( new Row( new Object[] { new Label( "Integer: " ),
			    new SpaceBin( 200.0, -1.0, intSlider.alignHExpand() ).alignVCentre(), intValue } ).padX( 5.0 ) );
		Pres progLine = StyleSheet.style( Primitive.rowSpacing.as( 20.0 ) ).applyTo( new Row( new Object[] { new Label( "Progress bar: " ),
			    new SpaceBin( 200.0, -1.0, progress.alignHExpand() ).alignVCentre(), realValue } ).padX(5.0) );
        Pres realRangeLine = StyleSheet.style( Primitive.rowSpacing.as( 20.0 ) ).applyTo( new Row( new Object[] { new Label( "Real range: " ),
                new SpaceBin( 200.0, -1.0, realRangeSlider.alignHExpand() ).alignVCentre(),
                realRangeLower, new Label(":"), realRangeUpper } ).padX( 5.0 ) );
        Pres intRangeLine = StyleSheet.style( Primitive.rowSpacing.as( 20.0 ) ).applyTo( new Row( new Object[] { new Label( "Integer range: " ),
                new SpaceBin( 200.0, -1.0, intRangeSlider.alignHExpand() ).alignVCentre(),
                intRangeLower, new Label(":"), intRangeUpper } ).padX( 5.0 ) );

        Pres numericSection = new Section(new SectionHeading2("Numeric value sliders"),
                new Column(new Pres[] {realLine, intLine}));
        Pres progressSection = new Section(new SectionHeading2("Progress bar"),
                new Column(new Pres[] {progLine}));
        Pres rangeSection = new Section(new SectionHeading2("Ranged sliders"),
                new Column(new Pres[] {realRangeLine, intRangeLine}));

		return new Body( new Pres[] { numericSection, progressSection, rangeSection } );
	}
}
