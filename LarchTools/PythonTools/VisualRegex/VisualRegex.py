##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************

"""
Python visual regular expression

Inspired by the visual regular expression presentation system in SWYN (Say What You Need), by Alan Blackwell.
"""

import re

from BritefuryJ.Command import *

from BritefuryJ.DocModel import DMNode

from BritefuryJ.Pres.Primitive import *

from LarchCore.Languages.Python25.PythonCommands import pythonCommands, makeInsertEmbeddedExpressionAtCaretAction, chainActions

from LarchTools.PythonTools.VisualRegex import Schema
from LarchTools.PythonTools.VisualRegex.Parser import VisualRegexGrammar
from LarchTools.PythonTools.VisualRegex.View import perspective as VREPerspective
from LarchTools.PythonTools.VisualRegex.CodeGenerator import VisualRegexCodeGenerator



class VisualPythonRegex (object):
	_codeGen = VisualRegexCodeGenerator()


	def __init__(self, regex=None):
		if regex is None:
			regex = Schema.PythonRegEx( expr= Schema.UNPARSED( value=[ '' ] ) )

		if isinstance( regex, re._pattern_type ):
			# Extract pattern string
			regex = regex.pattern

		if isinstance( regex, str )  or  isinstance( regex, unicode ):
			# Convert to structural form
			g = VisualRegexGrammar()
			x = g.regex().parseStringChars( regex, None )
			regex = Schema.PythonRegEx( expr=x.value )

		if isinstance( regex, DMNode ):
			if not regex.isInstanceOf( Schema.PythonRegEx ):
				if regex.isInstanceOf( Schema.Node ):
					regex = Schema.PythonRegEx( expr=regex )
				else:
					raise TypeError, 'Wrong schema'

			self.regex = regex
		else:
			raise TypeError, 'Invalid regular expression type'


	def __py_eval__(self, _globals, _locals, codeGen):
		return self._codeGen( self.regex )


	def __present__(self, fragment, inherited_state):
		#return VREPerspective( self.regex )
		return Paragraph( [ HiddenText( u'\ue000' ), VREPerspective( self.regex ), HiddenText( u'\ue000' ) ] )



def _newVREAtCaret(caret):
	return VisualPythonRegex()

_exprAtCaret = makeInsertEmbeddedExpressionAtCaretAction( _newVREAtCaret )


_vreCommand = Command( '&Visual &Regular &Expression', _exprAtCaret )

_vreCommands = CommandSet( 'LarchTools.PythonTools.VisualRegex', [ _vreCommand ] )

pythonCommands.registerCommandSet( _vreCommands )