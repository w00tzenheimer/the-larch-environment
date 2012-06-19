##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from copy import deepcopy

from BritefuryJ.Incremental import IncrementalValueMonitor

from Britefury.Util.TrackedList import *



class _LiveListIter (object):
	__slots__ = [ '_it', '_incr' ]
	
	def __init__(self, it, incr):
		self._it = it
		self._incr = incr
	
	
	def __iter__(self):
		return self

	def next(self):
		self._incr.onAccess()
		return self._it.next()


class LiveList (object):
	__slots__ = [ '__change_history__', '_items', '_incr' ]
	
	def __init__(self, xs=None):
		self._items = []
		if xs is not None:
			self._items[:] = xs[:]
		self.__change_history__ = None
		self._incr = IncrementalValueMonitor()
	
		
	def __getstate__(self):
		self._incr.onAccess()
		return { 'items' : self._items }

	def __setstate__(self, state):
		self._items = state['items']
		self.__change_history__ = None
		self._incr = IncrementalValueMonitor()
	
	def __copy__(self):
		self._incr.onAccess()
		t = type( self )
		return t( self._items )
	
	def __deepcopy__(self, memo):
		self._incr.onAccess()
		t = type( self )
		return t( deepcopy( self._items, memo ) )
	
	
	def __get_trackable_contents__(self):
		return self._items

	
	
	def __iter__(self):
		self._incr.onAccess()
		return _LiveListIter( iter( self._items ), self._incr )
	
	def __contains__(self, x):
		self._incr.onAccess()
		return x in self._items
	
	def __add__(self, xs):
		self._incr.onAccess()
		return self._items + xs
	
	def __mul__(self, x):
		self._incr.onAccess()
		return self._items * x
	
	def __rmul__(self, x):
		self._incr.onAccess()
		return x * self._items
	
	def __getitem__(self, index):
		self._incr.onAccess()
		return self._items[index]
	
	def __len__(self):
		self._incr.onAccess()
		return len( self._items )
	
	def index(self, x, i=None, j=None):
		self._incr.onAccess()
		if i is None:
			return self._items.index( x )
		elif j is None:
			return self._items.index( x, i )
		else:
			return self._items.index( x, i, j )

	def count(self, x):
		self._incr.onAccess()
		return self._items.count( x )
	
	def __setitem__(self, index, x):
		if isinstance( index, int )  or  isinstance( index, long ):
			oldX = self._items[index]
			self._items[index] = x
			onTrackedListSetItem( self.__change_history__, self, index, oldX, x, 'Live list set item' )
		else:
			oldContents = self._items[:]
			self._items[index] = x
			newContents = self._items[:]
			onTrackedListSetContents( self.__change_history__, self, oldContents, newContents, 'Live list set item' )
		self._incr.onChanged()
	
	def __delitem__(self, index):
		oldContents = self._items[:]
		del self._items[index]
		newContents = self._items[:]
		onTrackedListSetContents( self.__change_history__, self, oldContents, newContents, 'Live list del item' )
		self._incr.onChanged()
		
	def append(self, x):
		self._items.append( x )
		onTrackedListAppend( self.__change_history__, self, x, 'Live list append' )
		self._incr.onChanged()

	def extend(self, xs):
		self._items.extend( xs )
		onTrackedListExtend( self.__change_history__, self, xs, 'Live list extend' )
		self._incr.onChanged()
	
	def insert(self, i, x):
		self._items.insert( i, x )
		onTrackedListInsert( self.__change_history__, self, i, x, 'Live list insert' )
		self._incr.onChanged()

	def pop(self):
		x = self._items.pop()
		onTrackedListPop( self.__change_history__, self, x, 'Live list pop' )
		self._incr.onChanged()
		return x
		
	def remove(self, x):
		i = self._items.index( x )
		xFromList = self._items[i]
		del self._items[i]
		onTrackedListRemove( self.__change_history__, self, i, xFromList, 'Live list remove' )
		self._incr.onChanged()
		
	def reverse(self):
		self._items.reverse()
		onTrackedListReverse( self.__change_history__, self, 'Live list reverse' )
		self._incr.onChanged()
	
	def sort(self, cmp=None, key=None, reverse=None):
		oldContents = self._items[:]
		self._items.sort( cmp, key, reverse )
		newContents = self._items[:]
		onTrackedListSetContents( self.__change_history__, self, oldContents, newContents, 'Live list sort' )
		self._incr.onChanged()
	
	def _setContents(self, xs):
		oldContents = self._items[:]
		self._items[:] = xs
		onTrackedListSetContents( self.__change_history__, self, oldContents, xs, 'Live list set contents' )
		self._incr.onChanged()