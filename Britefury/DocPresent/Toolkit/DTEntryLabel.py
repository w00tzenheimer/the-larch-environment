##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Util.SignalSlot import *

import traceback

from Britefury.Math.Math import Colour3f

from Britefury.DocPresent.Toolkit.DTBin import DTBin
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
from Britefury.DocPresent.Toolkit.DTEntry import DTEntry




class DTEntryLabel (DTBin):
	textInsertedSignal = ClassSignal()				# ( entry, position, bAppended, textInserted )
	textDeletedSignal = ClassSignal()				# ( entry, startIndex, endIndex, textDeleted )
	startEditingSignal = ClassSignal()				# ( entry, text )
	finishEditingSignal = ClassSignal()			# ( entry, text, bUserEvent )


	class _Label (DTLabel):
		def __init__(self, entryLabel, text, markup=None, font=None, colour=Colour3f( 0.0, 0.0, 0.0 )):
			super( DTEntryLabel._Label, self ).__init__( text, markup, font, colour )
			self._entryLabel = entryLabel


		def _o_onButtonDown(self, localPos, button, state):
			super( DTEntryLabel._Label, self )._o_onButtonDown( localPos, button, state )
			return button == 1

		def _o_onButtonUp(self, localPos, button, state):
			super( DTEntryLabel._Label, self )._o_onButtonUp( localPos, button, state )
			if button == 1:
				self._entryLabel._p_onLabelClicked( localPos )
				return True
			else:
				return False

			
		#
		# FOCUS NAVIGATION METHODS
		#
		
		def _o_isFocusTarget(self):
			return True
			

		def startEditing(self):
			self._entryLabel.startEditing()
			
		def startEditingOnLeft(self):
			self._entryLabel.startEditingOnLeft()
			
		def startEditingOnRight(self):
			self._entryLabel.startEditingOnRight()
			
		def startEditingAtPosition(self, pos):
			self._entryLabel.startEditingAtPosition( pos )
			
		def finishEditing(self):
			self._entryLabel.finishEditing()

	
	
	class _Entry (DTEntry):
		def __init__(self, entryLabel, text, font=None, borderWidth=2.0, backgroundColour=Colour3f( 0.9, 0.95, 0.9 ), highlightedBackgroundColour=Colour3f( 0.0, 0.0, 0.5 ), textColour=Colour3f( 0.0, 0.0, 0.0 ), highlightedTextColour=Colour3f( 1.0, 1.0, 1.0 ), borderColour=Colour3f( 0.6, 0.8, 0.6 ), regexp=None):
			super( DTEntryLabel._Entry, self ).__init__( text, font, borderWidth, backgroundColour, highlightedBackgroundColour, textColour, highlightedTextColour, borderColour, regexp=regexp )
			self._entryLabel = entryLabel

		def _o_onLoseFocus(self):
			super( DTEntryLabel._Entry, self )._o_onLoseFocus()
			if not self._bFocusGrabbed:
				self._entryLabel._p_onEntryLoseFocus()





	def __init__(self, text='', labelFilter=None, bLabelMarkup=False, font=None, textColour=Colour3f( 0.0, 0.0, 0.0 ), regexp=None):
		super( DTEntryLabel, self ).__init__()

		self._text = text

		self._labelFilter = labelFilter
		self._bLabelMarkup = bLabelMarkup

		self._label = self._Label( self, text, None, font, textColour )
		self._p_refreshLabel()
		self._entry = self._Entry( self, text, font, textColour=textColour, regexp=regexp )
		self._entry.textInsertedSignal.connect( self._p_onEntryTextInserted )
		self._entry.textDeletedSignal.connect( self._p_onEntryTextDeleted )
		self._entry.returnSignal.connect( self._p_onEntryReturn )

		self.setChild( self._label )

		self._bIgnoreEntryLoseFocus = False



	def getText(self):
		return self._text

	def setText(self, text):
		self._text = text
		self._p_refreshLabel()
		self._entry.text = text



	def setFont(self, font):
		self._label.setFont( font )
		self._entry.setFont( font )

	def getFont(self):
		return self._label.getFont()


	def setTextColour(self, colour):
		self._label.setColour( colour )
		self._entry.setTextColour( colour )

	def getTextColour(self):
		return self._label.getColour()



	def startEditing(self):
		if self.getChild() is not self._entry:
			self.setChild( self._entry )
			self._entry.startEditing()
			self.startEditingSignal.emit( self, self.text )

	def startEditingOnLeft(self):
		self.startEditing()
		self._entry.moveCursorToStart()

	def startEditingOnRight(self):
		self.startEditing()
		self._entry.moveCursorToEnd()

	def startEditingAtPosition(self, pos):
		index = self.getChild().getCursorIndexAt( pos )
		self.startEditing()
		self._entry.setCursorIndex( index )

	def finishEditing(self):
		self._p_finishEditing( False )

	def _p_finishEditing(self, bUserEvent):
		if self.getChild() is not self._label:
			# Store @bUserEvent so that _p_onEditingFinish() can retrieve it
			self._bIgnoreEntryLoseFocus = True
			self._entry.ungrabFocus()
			self._bIgnoreEntryLoseFocus = False
			self.setChild( self._label )
			self._o_emitFinishEditing( bUserEvent )

	def _o_emitFinishEditing(self, bUserEvent):
		self.finishEditingSignal.emit( self, self.text, bUserEvent )





	def getCursorIndex(self):
		if self.getChild() is self._entry:
			return self._entry.getCursorIndex()
		else:
			return None

	def getCursorPosition(self):
		if self.getChild() is self._entry:
			return self._entry.getCursorPosition()
		else:
			return None

	def isCursorAtStart(self):
		if self.getChild() is self._entry:
			return self._entry.isCursorAtStart()
		else:
			return False

	def isCursorAtEnd(self):
		if self.getChild() is self._entry:
			return self._entry.isCursorAtEnd()
		else:
			return False




	def _p_refreshLabel(self):
		labelText = self._text
		if self._labelFilter is not None:
			labelText = self._labelFilter( labelText )

		if self._bLabelMarkup:
			self._label.markup = labelText
		else:
			self._label.text = labelText



	def _p_onLabelClicked(self, localPos):
		self.startEditing()
		index = self._label.getCursorIndexAt( localPos )
		self._entry.setCursorIndex( index )


	def _p_onEntryTextInserted(self, entry, position, bAppended, textInserted):
		self._text = self._entry.text
		self._o_emitTextInserted( position, bAppended, textInserted )
		self._p_refreshLabel()

	def _p_onEntryTextDeleted(self, entry, start, end, textDeleted):
		self._text = self._entry.text
		self._o_emitTextDeleted( start, end, textDeleted )
		self._p_refreshLabel()

	def _o_emitTextInserted(self, position, bAppended, textInserted):
		self.textInsertedSignal.emit( self, position, bAppended, textInserted )

	def _o_emitTextDeleted(self, start, end, textDeleted):
		self.textInsertedSignal.emit( self, start, end, textDeleted )

	def _p_onEntryReturn(self, entry):
		self._p_finishEditing( True )

	def _p_onEntryLoseFocus(self):
		if not self._bIgnoreEntryLoseFocus:
			self._p_finishEditing( False )


	def _p_setKeyHandler(self, handler):
		self._entry.keyHandler = handler

	def _p_setAllowableCharacters(self, chars):
		self._entry.allowableCharacters = chars

	def _p_setBEditable(self, bEditable):
		self._entry.bEditable = bEditable

		


	#
	# CURSOR ENTITY METHODS
	#

	def _o_getFirstCursorEntity(self):
		return self._entry.getFirstCursorEntity()
	
	def _o_getLastCursorEntity(self):
		return self._entry.getLastCursorEntity()

	
	def _o_linkChildEntryCursorEntity(self, childEntry):
		# Prevent the DTBin superclass from linking in anything other than the entry
		pass
		#prevCursorEntity = self._f_getPrevCursorEntityBeforeChild( childEntry.child )
		#nextCursorEntity = self._f_getNextCursorEntityAfterChild( childEntry.child )
		#DTCursorEntity.splice( prevCursorEntity, nextCursorEntity, childEntry.child.getFirstCursorEntity(), childEntry.child.getLastCursorEntity() )

	def _o_unlinkChildEntryCursorEntity(self, childEntry):
		# Prevent the DTBin superclass from unlinking in anything other than the entry
		pass
		#DTCursorEntity.remove( childEntry.child.getFirstCursorEntity(), childEntry.child.getLastCursorEntity() )
		
		
		

	text = property( getText, setText )
	font = property( getFont, setFont )
	textColour = property( getTextColour, setTextColour )

	keyHandler = property( None, _p_setKeyHandler )
	allowableCharacters = property( None, _p_setAllowableCharacters )
	bEditable = property( None, _p_setBEditable )








if __name__ == '__main__':
	import pygtk
	pygtk.require( '2.0' )
	import gtk
	import cairo

	from Britefury.DocPresent.Toolkit.DTDocument import DTDocument
	from Britefury.Math.Math import Colour3f
	import traceback

	def onDeleteEvent(widget, event, data=None):
		return False

	def onDestroy(widget, data=None):
		gtk.main_quit()

	def onInserted(entry, position, bAppended, text):
		print 'INSERT: ', position, bAppended, text

	def onDeleted(entry, start, end, text):
		print 'DELETE: ', start, end, text


	window = gtk.Window( gtk.WINDOW_TOPLEVEL );
	window.connect( 'delete-event', onDeleteEvent )
	window.connect( 'destroy', onDestroy )
	window.set_border_width( 10 )
	window.set_size_request( 300, 100 )

	doc = DTDocument()
	doc.show()

	entry = DTEntryLabel( 'Hello world' )
	doc.child = entry
	entry.textInsertedSignal.connect( onInserted )
	entry.textDeletedSignal.connect( onDeleted )


	window.add( doc )
	window.show()

	gtk.main()