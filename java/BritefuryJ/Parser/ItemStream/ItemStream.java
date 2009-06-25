//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.ItemStream;

import java.util.Arrays;
import java.util.List;

public class ItemStream
{
	public static abstract class Item
	{
		protected int start, stop;
		
		
		public Item(int start, int stop)
		{
			this.start = start;
			this.stop = stop;
		}

		
		public int getStart()
		{
			return start;
		}
		
		public int getStop()
		{
			return stop;
		}
		
		public int getLength()
		{
			return stop - start;
		}
		
		
		abstract public boolean isStructural();
		
		
		abstract public Item subItemFrom(int start, int atPos);
		abstract public Item subItemTo(int end, int atPos);
		abstract public Item subItem(int start, int end, int atPos);
		abstract public Item copyAt(int atPos);
	}
	
	
	
	public static class TextItem extends Item
	{
		protected String textValue;
		protected int start, stop;
		
		
		public TextItem(String textValue, int start, int stop)
		{
			super( start, stop );
			this.textValue = textValue;
		}

		
		public boolean isStructural()
		{
			return false;
		}

		
		public Item subItemFrom(int start, int atPos)
		{
			int offset = start - this.start;
			return new TextItem( textValue.substring( offset ), atPos, atPos + textValue.length() - offset );
		}
		
		public Item subItemTo(int end, int atPos)
		{
			end -= start;
			return new TextItem( textValue.substring( 0, end ), atPos, atPos + end );
		}
		
		public Item subItem(int start, int end, int atPos)
		{
			int offset = start - this.start;
			end -= this.start;
			return new TextItem( textValue.substring( offset, end ), atPos, atPos + end - offset );
		}
		
		public Item copyAt(int atPos)
		{
			return new TextItem( textValue, atPos, atPos + stop - start );
		}
		
		
		public String toString()
		{
			return textValue;
		}
	}
	
	
	
	public static class StructuralItem extends Item
	{
		protected Object structuralValue;
		
		
		public StructuralItem(Object structuralValue, int start, int stop)
		{
			super( start, stop );
			this.structuralValue = structuralValue;
		}
		
		
		public boolean isStructural()
		{
			return true;
		}

		
		public Item subItemFrom(int start, int atPos)
		{
			return new StructuralItem( structuralValue, atPos, atPos + 1 );
		}
		
		public Item subItemTo(int end, int atPos)
		{
			return new StructuralItem( structuralValue, atPos, atPos + 1 );
		}
		
		public Item subItem(int start, int end, int atPos)
		{
			return new StructuralItem( structuralValue, atPos, atPos + 1 );
		}
		
		public Item copyAt(int atPos)
		{
			return new StructuralItem( structuralValue, atPos, atPos + 1 );
		}
		
		
		public String toString()
		{
			return "<<--Structural: " + structuralValue.toString() + "-->>";
		}
	}
	
	

	protected Item items[];
	protected int length;
	
	
	public ItemStream(String text)
	{
		items = new Item[] { new TextItem( text, 0, text.length() ) };
		length = text.length();
	}
	
	protected ItemStream(Item items[])
	{
		this.items = items;
		length = items.length > 0  ?  items[items.length-1].stop  :  0;
	}
	
	
	
	public List<Item> getItems()
	{
		return Arrays.asList( items );
	}
	
	public int length()
	{
		return length;
	}
	
	
	
	public ItemStream subStream(int start, int stop)
	{
		if ( items.length == 0 )
		{
			return this;
		}
		else if ( start == stop )
		{
			return new ItemStream( new Item[] {} );
		}
		else
		{
			int startIndex = itemIndexAt( start );
			int stopIndex = itemIndexAt( stop );
			
			Item subItems[];
			
			if ( stop > items[stopIndex].start )
			{
				subItems = new Item[stopIndex+1-startIndex];
			}
			else
			{
				subItems = new Item[stopIndex-startIndex];
			}
			
			int pos = 0;
			subItems[0] = items[startIndex].subItemFrom( start, 0 );
			pos = subItems[0].stop;
			
			for (int i = startIndex + 1; i < stopIndex; i++)
			{
				Item subItem = items[i].copyAt( pos );
				subItems[i-startIndex] = subItem;
				pos = subItem.stop;
			}
			
			if ( stop > items[stopIndex].start )
			{
				subItems[stopIndex-startIndex] = items[stopIndex].subItemTo( stop, pos );
			}
			
			return new ItemStream( subItems );
		}
	}
	
	
	public ItemStreamAccessor accessor()
	{
		return new ItemStreamAccessor( this );
	}
	
	
	protected Item itemAt(int pos)
	{
		int index = itemIndexAt( pos );
		return items[index];
	}

	protected int itemIndexAt(int pos)
	{
		return binarySearchItem( 0, items.length, pos );
	}

	protected int binarySearchItem(int lo, int hi, int pos)
	{
		while ( lo < ( hi - 1 ) )
		{
			int mid = ( lo + hi ) / 2;
			if ( pos < items[mid].start )
			{
				hi = mid;
			}
			else if ( pos >= items[mid].stop )
			{
				lo = mid;
			}
			else
			{
				return mid;
			}
		}
		
		return lo;
	}
	
	
	

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		for (Item i: items)
		{
			builder.append( i.toString() );
		}
		return builder.toString();
	}
}
