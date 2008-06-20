##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import pygtk
pygtk.require( '2.0' )

import cairo
import pango
import pangocairo

from Britefury.Math.Math import Colour3f, Vector2, Point2, Segment2

from Britefury.DocPresent.Toolkit.DTCursor import DTCursorLocation
from Britefury.DocPresent.Toolkit.DTSimpleStaticWidget import DTSimpleStaticWidget

from Britefury.DocPresent.Util.DUTextLayout import DUTextLayout



class DTLabel (DTSimpleStaticWidget):
	HALIGN_LEFT = 0
	HALIGN_CENTRE = 1
	HALIGN_RIGHT = 2

	VALIGN_TOP = 0
	VALIGN_CENTRE = 1
	VALIGN_RIGHT = 2
	
	MODE_TEXT = 0
	MODE_MARKUP = 1
	MODE_MIXEDCAPS = 2


	def __init__(self, text='', bUseMarkup=False, font=None, colour=Colour3f( 0.0, 0.0, 0.0), hAlign=HALIGN_CENTRE, vAlign=VALIGN_CENTRE):
		super( DTLabel, self ).__init__()
		
		assert text is None  or  isinstance( text, str )  or  isinstance( text, unicode )
		assert isinstance( bUseMarkup, bool )

		if font is None:
			font = 'Sans 11'
			
		self._layout = DUTextLayout( text, bUseMarkup, font, colour )
		self._layout.evRequestRedraw = self._o_queueFullRedraw
		self._layout.evRequestResize = self._p_onResizeRequest

		self._hAlign = hAlign
		self._vAlign = vAlign
		self._textPosition = None

		self._o_queueResize()



	def setText(self, text):
		assert text is None  or  isinstance( text, str )  or  isinstance( text, unicode )
		self._layout.setText( text )

	def getText(self):
		return self._layout.getText()


	def useMarkup(self):
		self._layout.useMarkup()
		
	def usePlaintext(self):
		self._layout.usePlaintext()

	def setUseMarkup(self, bUseMarkup):
		self._layout.setUseMarkup( bUseMarkup )

	def getUseMarkup(self):
		return self._layout.getUseMarkup()


	def setFont(self, font):
		self._layout.setFont( font )

	def getFont(self):
		return self._layout.getFont()


	def setColour(self, colour):
		self._layout.setColour( colour )

	def getColour(self):
		return self._layout.getColour()



	def setHAlign(self, hAlign):
		self._hAlign = hAlign
		self._textPosition = None
		self._o_queueFullRedraw()

	def getHAlign(self):
		return self._hAlign



	def setVAlign(self, vAlign):
		self._vAlign = vAlign
		self._textPosition = None
		self._o_queueFullRedraw()

	def getVAlign(self):
		return self._vAlign
	
	
	def getCharacterIndexAt(self, point):
		return self._layout.getCharacterIndexAt( point - self._p_getTextPosition() )


	def getCharacterIndexAtX(self, x):
		y = self._p_getTextPosition().y + self._textSize.y * 0.5
		return self.getCharacterIndexAt( Point2( x, y ) )


	def getCursorIndexAt(self, point):
		return self._layout.getCursorIndexAt( point - self._p_getTextPosition() )


	def getCursorIndexAtX(self, x):
		y = self._p_getTextPosition().y + self._textSize.y * 0.5
		return self.getCursorIndexAt( Point2( x, y ) )



	def _o_onRealise(self, context, pangoContext):
		super( DTLabel, self )._o_onRealise( context, pangoContext )
		self._layout.initialise( context )



	def _o_draw(self, context):
		super( DTLabel, self )._o_draw( context )
		self._o_clipIfAllocationInsufficient( context )
		pos = self._p_getTextPosition()
		context.move_to( pos.x, pos.y )
		self._layout.draw( context )


	def _o_onSetScale(self, scale, rootScale):
		context = self._realiseContext
		if context is not None:
			context.save()
			context.scale( rootScale, rootScale )
			self._layout.update( context )
			context.restore()


	def _o_getRequiredWidth(self):
		return self._layout.getSize().x + 2.0

	def _o_getRequiredHeightAndBaseline(self):
		return self._layout.getSize().y + 2.0, self._layout.getSize().y - self._layout.getBaseline() + 1.0



		
	def _p_getTextPosition(self):
		if self._textPosition is None:
			size = self._layout.getSize()
			if self._hAlign == self.HALIGN_LEFT:
				x = 0.0
			elif self._hAlign == self.HALIGN_CENTRE:
				x = ( self._allocation.x - size.x ) * 0.5
			elif self._hAlign == self.HALIGN_RIGHT:
				x = self._allocation.x - size.x
	
			if self._vAlign == self.VALIGN_TOP:
				y = 0.0
			elif self._vAlign == self.VALIGN_CENTRE:
				y = ( self._allocation.y - size.y ) * 0.5
			elif self._vAlign == self.VALIGN_BOTTOM:
				y = self._allocation.y - size.y
	
			self._textPosition = Vector2( x, y )  +  Vector2( 1.0, 1.0 )
		return self._textPosition
	
	
	def _p_onResizeRequest(self):
		self._textPosition = None
		self._o_queueResize()



		
	#
	# CURSOR POSITIONING METHODS
	#
	
	def getCursorSegment(self, cursorLocation):
		assert cursorLocation.cursorEntity is self._cursorEntity
		if cursorLocation.edge == DTCursorLocation.EDGE_LEADING:
			x = self._textPosition.x
		elif cursorLocation.edge == DTCursorLocation.EDGE_TRAILING:
			x = self._textPosition.x + self._textSize.x
		
		pos = Point2( x, self._textPosition.y )
		return Segment2( pos, pos + Vector2( 0.0, self._textSize.y ) )
	
	
	def _o_getCursorLocationAtPosition(self, localPosition):
		if localPosition.x  <  ( self._textPosition.x  +  self._textSize.x * 0.5 ):
			return DTCursorLocation( self._cursorEntity, DTCursorLocation.EDGE_LEADING )
		else:
			return DTCursorLocation( self._cursorEntity, DTCursorLocation.EDGE_TRAILING )
		
		
		
		

	text = property( getText, setText )
	bUseMarkup = property( getUseMarkup, setUseMarkup )
	font = property( getFont, setFont )
	colour = property( getColour, setColour )
	hAlign = property( getHAlign, setHAlign )
	vAlign = property( getVAlign, setVAlign )










if __name__ == '__main__':
	import pygtk
	pygtk.require( '2.0' )
	import gtk


	from Britefury.DocPresent.Toolkit.DTBox import DTBox
	from Britefury.DocPresent.Toolkit.DTDocument import DTDocument
	import cairo
	from Britefury.Math.Math import Colour3f
	import traceback

	def onDeleteEvent(widget, event, data=None):
		return False

	def onDestroy(widget, data=None):
		gtk.main_quit()


	def makeButton(text, response):
		button = gtk.Button( text )
		button.connect( 'clicked', response )
		button.show()
		return button


	window = gtk.Window( gtk.WINDOW_TOPLEVEL );
	window.connect( 'delete-event', onDeleteEvent )
	window.connect( 'destroy', onDestroy )
	window.set_border_width( 10 )
	window.set_size_request( 300, 100 )

	doc = DTDocument()
	doc.getGtkWidget().show()

	vbox = DTBox( DTBox.TOP_TO_BOTTOM, alignment=DTBox.ALIGN_EXPAND )
	vbox.spacing = 10.0
	vbox.backgroundColour = Colour3f( 0.8, 0.8, 0.8 )

	for i in xrange( 0, 4 ):
		hbox = DTBox()
		for j in xrange( i*64, i*64+64 ):
			hbox.append( DTLabel( chr( j ) ) )
		vbox.append( hbox )
		
	chars = [ u"\u03bb" ]
	hbox = DTBox()
	for c in chars:
		hbox.append( DTLabel( c ) )
	vbox.append( hbox )
		


	doc.child = vbox


	window.add( doc.getGtkWidget() )
	window.show()

	gtk.main()
