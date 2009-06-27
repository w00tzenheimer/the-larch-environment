//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocTree;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;

import BritefuryJ.DocModel.DMListInterface;
import BritefuryJ.DocModel.DMObjectInterface;
import BritefuryJ.Utils.HashUtils;


public class DocTree
{
	public static class NodeTypeNotSupportedException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}

	
	
	private static class Key
	{
		private WeakReference<Object> docNode;
		private WeakReference<DocTreeNode> parentTreeNode;
		private int index;
		private int hash;
		
		public Key(Object docNode, DocTreeNode parentTreeNode, int index)
		{
			this.docNode = new WeakReference<Object>( docNode );
			if ( parentTreeNode != null )
			{
				this.parentTreeNode = new WeakReference<DocTreeNode>( parentTreeNode );
			}
			this.index = index;
			
			this.hash = HashUtils.tripleHash( docNode.hashCode(), parentTreeNode != null  ?  parentTreeNode.hashCode()  :  0, index );
		}
		
		
		public int hashCode()
		{
			return hash;
		}
		
		
		public boolean equals(Object x)
		{
			if ( this == x )
			{
				return true;
			}
			
			if ( x instanceof Key )
			{
				Key kx = (Key)x;
				if ( parentTreeNode == null )
				{
					return docNode.get() == kx.docNode.get()  &&  kx.parentTreeNode == null  &&  index == kx.index;
				}
				else
				{
					return docNode.get() == kx.docNode.get()  &&  parentTreeNode.get() == kx.parentTreeNode.get()  &&  index == kx.index;
				}
			}
			else
			{
				return false;
			}
		}
		
		
		public boolean hasWeakRef(Reference<? extends Object> r)
		{
			return r == docNode  ||  r == parentTreeNode;
		}
		
		
		public String toString()
		{
			String d = "<null>", p = "<null>";
			if ( docNode.get() != null )
			{
				d = docNode.get().toString();
			}
			if ( parentTreeNode != null )
			{
				if ( parentTreeNode.get() != null )
				{
					p = parentTreeNode.get().toString();
				}
			}
			return "Key( docNode=" + d + ", parentTreeNode=" + p + ", index=" + index + ", hash=" + hash + ")";
		}
	}
	
	
	private static class DocTreeKeyError extends Exception
	{
		private static final long serialVersionUID = 1L;
	}
	
	private static class DocTreeKey
	{
		private WeakReference<Object> docNode;
		private WeakReference<DocTreeNode> parentTreeNode;
		private int index;
		
		public DocTreeKey(Object docNode, DocTreeNode parentTreeNode, int index)
		{
			this.docNode = new WeakReference<Object>( docNode );
			if ( parentTreeNode != null )
			{
				this.parentTreeNode = new WeakReference<DocTreeNode>( parentTreeNode );
			}
			this.index = index;
		}
		
		
		public Object getDocNode()
		{
			return docNode.get();
		}
		
		public DocTreeNode getParentTreeNode()
		{
			if ( parentTreeNode != null )
			{
				return parentTreeNode.get();
			}
			else
			{
				return null;
			}
		}
		
		
		public Key key() throws DocTreeKeyError
		{
			Object docNode = getDocNode();
			DocTreeNode parent = getParentTreeNode();
			if ( docNode == null  ||  ( parentTreeNode != null  &&  parent == null ) )
			{
				throw new DocTreeKeyError();
			}
			return new Key( docNode, parent, index );
		}

	
		public String toString()
		{
			String d = "<null>", p = "<null>";
			if ( docNode.get() != null )
			{
				d = docNode.get().toString();
			}
			if ( parentTreeNode != null )
			{
				if ( parentTreeNode.get() != null )
				{
					p = parentTreeNode.get().toString();
				}
			}
			return "DocTreeKey( docNode=" + d + ", parentTreeNode=" + p + ", index=" + index + ")";
		}
	}



	private static class DocTreeNodeTable
	{
		private ReferenceQueue<Object> refQueue;
		private HashMap<Key,DocTreeNode> table;
		
		public DocTreeNodeTable()
		{
			refQueue = new ReferenceQueue<Object>();
			table = new HashMap<Key,DocTreeNode>();
		}
		
		
		public DocTreeNode get(DocTreeKey k)
		{
			try
			{
				return table.get( k.key() );
			}
			catch (DocTreeKeyError e)
			{
				return null;
			}
		}

		public void put(DocTreeKey k, DocTreeNode v)
		{
			removeDeadEntries();
			try
			{
				table.put( k.key(), v );
			}
			catch (DocTreeKeyError e)
			{
			}
		}
		
		
		@SuppressWarnings("unchecked")
		private void removeDeadEntries()
		{
			Reference<Object> r = (Reference<Object>)refQueue.poll();
			
			while ( r != null )
			{
				r = (Reference<Object>)refQueue.poll();
				
				HashSet<Key> keysToRemove = new HashSet<Key>();
				for (Key k: table.keySet())
				{
					if ( k.hasWeakRef( r ) )
					{
						keysToRemove.add( k );
					}
				}
				
				for (Key k: keysToRemove)
				{
					table.remove( k );
				}
			}
		}
	}
	
	
	
	
	private DocTreeNodeTable table;
	
	
	public DocTree()
	{
		table = new DocTreeNodeTable();
	}
	
	
	public Object treeNode(Object x)
	{
		return treeNode( x, null, -1 );
	}
	
	
	public Object treeNode(Object x, DocTreeNode parentTreeNode, int indexInParent)
	{
		if ( x == null  ||  x instanceof String )
		{
			return x;
		}
		else
		{
			DocTreeKey key = new DocTreeKey( x, parentTreeNode, indexInParent );
			
			DocTreeNode node = table.get( key );
			
			if ( node != null )
			{
				return node;
			}
			else
			{
				if ( x instanceof DMListInterface )
				{
					node = new DocTreeList( this, (DMListInterface)x, parentTreeNode, indexInParent );
				}
				else if ( x instanceof DMObjectInterface )
				{
					node = new DocTreeObject( this, (DMObjectInterface)x, parentTreeNode, indexInParent );
				}
				else
				{
					System.out.println( "DocTree.treeNode(): wrapping " + x.getClass().getName() );
					node = new DocTreeNodeGeneric( x, parentTreeNode, indexInParent );
				}
	
				table.put( key, node );
					
				return node;
			}
		}
	}
}

