//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Clipboard;

import java.awt.datatransfer.DataFlavor;

import BritefuryJ.LSpace.Clipboard.LocalDataFlavor;
import BritefuryJ.LSpace.Focus.Selection;
import BritefuryJ.LSpace.Focus.Target;

public class DataImporter <TargetType extends Target> extends AbstractDataImporter<TargetType>
{
	public interface ImportDataFn <TargetType extends Target>
	{
		Object importData(TargetType target, Selection selection, Object data);
	}
	
	public interface CanImportFn <TargetType extends Target>
	{
		boolean canImport(TargetType target, Selection selection, Object data);
	}
	
	public interface CanImportFlavorFn
	{
		boolean canImportFlavor(DataFlavor flavor);
	}
	
	
	private CanImportFlavorFn canImportFlavorFn;
	private ImportDataFn<TargetType> importDataFn;
	private CanImportFn<TargetType> canImportFn;
	
	
	
	public DataImporter(CanImportFlavorFn canImportFlavorFn, ImportDataFn<TargetType> importDataFn, CanImportFn<TargetType> canImportFn)
	{
		this.canImportFlavorFn = canImportFlavorFn;
		this.importDataFn = importDataFn;
		this.canImportFn = canImportFn;
	}
	
	public DataImporter(CanImportFlavorFn flavor, ImportDataFn<TargetType> importDataFn)
	{
		this( flavor, importDataFn, null );
	}
	
	public DataImporter(Class<?> type, ImportDataFn<TargetType> importDataFn, CanImportFn<TargetType> canImportFn)
	{
		this( localFlavorImportFn( type ), importDataFn, canImportFn );
	}
	
	public DataImporter(Class<?> type, ImportDataFn<TargetType> importDataFn)
	{
		this( localFlavorImportFn( type ), importDataFn, null );
	}

	public DataImporter(DataFlavor flavor, ImportDataFn<TargetType> importDataFn, CanImportFn<TargetType> canImportFn)
	{
		this( flavorImportFn( flavor ), importDataFn, canImportFn );
	}

	public DataImporter(DataFlavor flavor, ImportDataFn<TargetType> importDataFn)
	{
		this( flavorImportFn( flavor ), importDataFn, null );
	}


	@Override
	protected boolean canImportFlavor(DataFlavor flavor)
	{
		return canImportFlavorFn.canImportFlavor( flavor );
	}

	@Override
	protected boolean canImportChecked(TargetType target, Selection selection, Object data)
	{
		return canImportFn == null  ||  canImportFn.canImport( target, selection, data );
	}

	@Override
	protected Object importCheckedData(TargetType target, Selection selection, Object data)
	{
		return importDataFn.importData( target, selection, data );
	}
	
	
	
	private static CanImportFlavorFn localFlavorImportFn(Class<?> type)
	{
		final DataFlavor localFlavor = new LocalDataFlavor( type );
		CanImportFlavorFn test = new CanImportFlavorFn() {
			public boolean canImportFlavor(DataFlavor flavor) {
                // The comparison must be in the order potentialTarget.equals(incoming)
                // as the changes introduced by JDK8 to the way HTML content is handled
                // when it is pasted in require this ordering.
                return localFlavor.equals( flavor );
			}
		};
		return test;
	}

	private static CanImportFlavorFn flavorImportFn(final DataFlavor requiredFlavor)
	{
		CanImportFlavorFn test = new CanImportFlavorFn() {
			public boolean canImportFlavor(DataFlavor flavor) {
                // The comparison must be in the order potentialTarget.equals(incoming)
                // as the changes introduced by JDK8 to the way HTML content is handled
                // when it is pasted in require this ordering.
                return requiredFlavor.equals(flavor);
			}
		};
		return test;
	}
}
