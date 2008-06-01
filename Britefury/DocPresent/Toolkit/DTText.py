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

from Britefury.extlibs.pyconsole.pyconsole import testScriptingWindow

from Britefury.Math.Math import Colour3f, Vector2, Point2, Segment2

from Britefury.DocPresent.Text.DocText import DocTextActiveRange

from Britefury.DocPresent.Toolkit.DTCursor import DTCursorLocation
from Britefury.DocPresent.Toolkit.DTSimpleStaticWidget import DTSimpleStaticWidget



class DTText (DTSimpleStaticWidget):
	HALIGN_LEFT = 0
	HALIGN_CENTRE = 1
	HALIGN_RIGHT = 2

	VALIGN_TOP = 0
	VALIGN_CENTRE = 1
	VALIGN_RIGHT = 2


	def __init__(self, docText, textRange, textFilter=None, bUseMarkup=False, font=None, colour=Colour3f( 0.0, 0.0, 0.0), hAlign=HALIGN_CENTRE, vAlign=VALIGN_CENTRE):
		super( DTText, self ).__init__()
		
		assert text is None  or  isinstance( text, str )  or  isinstance( text, unicode )
		assert isinstance( bUseMarkup, bool )

		if font is None:
			font = 'Sans 11'

		self._docText = docText
		self._textRange = textRange
		self._textRange.contentsSignal.connect( self._p_onRangeContents )
		self._textFilter = textFilter
		self._bUseMarkup = bUseMarkup

		self._fontString = font
		self._fontDescription = pango.FontDescription( font )
		self._layout = None
		self._layoutContext = None

		self._bLayoutNeedsRefresh = True
		self._colour = colour
		self._hAlign = hAlign
		self._vAlign = vAlign
		self._textPosition = Vector2()
		self._textSize = Vector2()

		self._o_queueResize()



	def setTextRange(self, r):
		assert isinstance( r, DocTextActiveRange )
		self._textRange.contentsSignal.disconnect( self._p_onRangeContents )
		self._textRange = r
		self._textRange.contentsSignal.connect( self._p_onRangeContents )
		self._bLayoutNeedsRefresh = True
		self._o_queueResize()

	def getTextRange(self):
		return self._textRange


	def setTextFilter(self, f):
		self._textFilter = f
		self._bLayoutNeedsRefresh = True
		self._o_queueResize()

	def getTextFilter(self):
		return self._textFilter


	def useMarkup(self):
		self.setUseMarkup( True )
		
	def usePlaintext(self):
		self.setUseMarkup( False )

	def setUseMarkup(self, bUseMarkup):
		self._bUseMarkup = bUseMarkup
		self._bLayoutNeedsRefresh = True
		self._o_queueResize()

	def getUseMarkup(self):
		return self._bUseMarkup


	def setFont(self, font):
		self._fontString = font
		self._fontDescription = pango.FontDescription( font )
		self._font = None
		self._o_queueResize()

	def getFont(self):
		return self._fontString


	def setColour(self, colour):
		self._colour = colour
		self._o_queueFullRedraw()

	def getColour(self):
		return self._colour



	def setHAlign(self, hAlign):
		self._hAlign = hAlign
		self._o_refreshTextPosition()
		self._o_queueFullRedraw()

	def getHAlign(self):
		return self._hAlign



	def setVAlign(self, vAlign):
		self._vAlign = vAlign
		self._o_refreshTextPosition()
		self._o_queueFullRedraw()

	def getVAlign(self):
		return self._vAlign


	def getCharacterIndexAt(self, point):
		self._p_refreshLayout()
		pointInLayout = point - self._textPosition
		index, trailing = self._layout.xy_to_index( int( pointInLayout.x * pango.SCALE ), int( pointInLayout.y * pango.SCALE ) )
		return index


	def getCharacterIndexAtX(self, x):
		y = self._textPosition.y + self._textSize.y * 0.5
		return self.getCharacterIndexAt( Point2( x, y ) )


	def getCursorIndexAt(self, point):
		self._p_refreshLayout()
		pointInLayout = point - self._textPosition
		index, trailing = self._layout.xy_to_index( int( pointInLayout.x * pango.SCALE ), int( pointInLayout.y * pango.SCALE ) )
		return index + trailing


	def getCursorIndexAtX(self, x):
		y = self._textPosition.y + self._textSize.y * 0.5
		return self.getCursorIndexAt( Point2( x, y ) )


	def _p_refreshLayout(self):
		if self._bLayoutNeedsRefresh  and  self._layout is not None:
			self._layout.set_font_description( self._fontDescription )

			text = self._docText.getTextInRange( self._textRange )

			if self._textFilter is not None:
				if isinstance( self._textFilter, str )  or  isinstance( self._textFilter, unicode ):
					text = self._textFilter
				else:
					text = self._textFilter( text )
			
			if self._bUseMarkup:
				self._layout.set_markup( text )
			else:
				self._layout.set_text( text )
			self._bLayoutNeedsRefresh = False


	def _o_onRealise(self, context, pangoContext):
		super( DTText, self )._o_onRealise( context, pangoContext )
		if context is not self._layoutContext:
			self._layoutContext = context
			self._layout = context.create_layout()
			self._layout.set_font_description( self._fontDescription )
			self._bLayoutNeedsRefresh = True



	def _o_draw(self, context):
		super( DTText, self )._o_draw( context )
		self._o_clipIfAllocationInsufficient( context )
		self._p_refreshLayout()
		context.set_source_rgb( self._colour.r, self._colour.g, self._colour.b )
		context.move_to( self._textPosition.x, self._textPosition.y )
		context.update_layout( self._layout )
		context.show_layout( self._layout )


	def _o_onSetScale(self, scale, rootScale):
		context = self._realiseContext
		if context is not None:
			context.save()
			context.scale( rootScale, rootScale )
			context.update_layout( self._layout )
			context.restore()


	def _o_getRequiredWidth(self):
		self._p_refreshLayout()
		return self._layout.get_pixel_size()[0]  +  2.0

	def _o_getRequiredHeightAndBaseline(self):
		self._p_refreshLayout()
		height = self._layout.get_pixel_size()[1]
		baseline = height  -  self._layout.get_iter().get_baseline() / float(pango.SCALE)
		return height  +  2.0,  baseline + 1.0


	def _o_onAllocateY(self, allocation):
		super( DTText, self )._o_onAllocateY( allocation )
		self._p_refreshLayout()

		self._textSize = Vector2( *self._layout.get_pixel_size() )

		self._o_refreshTextPosition()


	def _o_refreshTextPosition(self):
		if self._hAlign == self.HALIGN_LEFT:
			x = 0.0
		elif self._hAlign == self.HALIGN_CENTRE:
			x = ( self._allocation.x - self._textSize.x ) * 0.5
		elif self._hAlign == self.HALIGN_RIGHT:
			x = self._allocation.x - self._textSize.x

		if self._vAlign == self.VALIGN_TOP:
			y = 0.0
		elif self._vAlign == self.VALIGN_CENTRE:
			y = ( self._allocation.y - self._textSize.y ) * 0.5
		elif self._vAlign == self.VALIGN_BOTTOM:
			y = self._allocation.y - self._textSize.y

		self._textPosition = Vector2( x, y )  +  Vector2( 1.0, 1.0 )


	def _p_onFontChanged(self):
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
		
		
	
	#
	# TEXT RANGE RESPONSE METHODS
	#
	
	def _p_onRangeContents(self, r):
		self._bLayoutNeedsRefresh = True
		self._o_queueResize()
		
		
		
		

	textRange = property( getTextRange, setTextRange )
	textFilter = property( getTextFilter, setTextFilter )
	bUseMarkup = property( getUseMarkup, setUseMarkup )
	font = property( getFont, setFont )
	colour = property( getColour, setColour )
	hAlign = property( getHAlign, setHAlign )
	vAlign = property( getVAlign, setVAlign )










if __name__ == '__main__':
	import pygtk
	pygtk.require( '2.0' )
	import gtk
	import sys


	from Britefury.DocPresent.Text.DocText import DocText

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
	
	text = 'Hello world, this is a test of the DTText system'
	docText = DocText( text )
	

	doc = DTDocument()
	doc.getGtkWidget().show()

	vbox = DTBox( DTBox.TOP_TO_BOTTOM, alignment=DTBox.ALIGN_EXPAND )
	vbox.spacing = 10.0
	vbox.backgroundColour = Colour3f( 0.8, 0.8, 0.8 )
	
	hbox = DTBox( spacing=5.0 )

	for word in text.split():
		begin = text.index( word )
		end = begin + len( word )
		t = DTText( docText, docText.textRange( begin, end, True ) )
		hbox.append( t )
		

	vbox.append( hbox )
		
	doc.child = vbox


	window.add( doc.getGtkWidget() )
	window.show()

	scriptWindow, scriptConsole = testScriptingWindow( sys.version, 'DTText test', window, False, locals() )
	
	scriptWindow.show()

	gtk.main()