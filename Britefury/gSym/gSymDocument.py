##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Kernel.Abstract import abstractmethod

from Britefury.GLisp.GLispUtil import isGLispList
from Britefury.gSymConfig.gSymVersion import compareVersions, gSymVersion

from Britefury.gSym.View.gSymView import GSymViewFactory
from Britefury.gSym.gSymEnvironment import GSymEnvironment



class GSymRequireMetaLanguageDefinition (Exception):
	pass

class GSymRequireGMetaModule (Exception):
	pass




class GSymDocumentContentHandler (object):
	def withLanguageModule(self, world, importLanguage, xs):
		pass



class GSymDocumentViewContentHandler (GSymDocumentContentHandler):
	def __init__(self, commandHistory, stylesheetDispatcher):
		super( GSymDocumentViewContentHandler, self ).__init__()
		self._commandHistory = commandHistory 
		self._stylesheetDispatcher = stylesheetDispatcher 
	
	def withLanguageModule(self, world, importLanguage, xs):
		language = world.getModuleLanguage( importLanguage )
		languageViewFactory = GSymViewFactory( world, importLanguage, language.getViewFactory() )
		return languageViewFactory.createDocumentView( xs, self._commandHistory, self._stylesheetDispatcher )
		
		


class GSymDocumentInvalidStructure (Exception):
	pass

class GSymDocumentInvalidHeader (Exception):
	pass

class GSymDocumentInvalidVersion (Exception):
	pass

class GSymDocumentUnsupportedVersion (Exception):
	pass

class GSymDocumentUnknownItemType (Exception):
	pass


class GSymDocumentContentInvalidStructure (Exception):
	pass

class GSymDocumentContentInvalidType (Exception):
	pass




def loadDocument(world, xs, contentHandler):
	"""
	($gSymDocument <gsym_version> <module>)
	"""
	if not isGLispList( xs ):
		raise GSymDocumentInvalidStructure
	
	if len( xs ) < 3:
		raise GSymDocumentInvalidStructure
	
	header = xs[0]
	version = xs[1]
	unitXs = xs[2]
	
	if header != "$gSymDocument":
		raise GSymDocumentInvalidHeader
	
	try:
		versionCmp = compareVersions( version, gSymVersion )
	except TypeError:
		raise GSymDocumentInvalidVersion
	except ValueError:
		raise GSymDocumentInvalidVersion
	
	if versionCmp > 0:
		raise GSymDocumentUnsupportedVersion
	
	
	
	if not isGLispList( unitXs ):
		raise GSymDocumentInvalidStructure
	
	if len( unitXs ) < 1:
		raise GSymDocumentInvalidStructure
	
	
	"""
	($unit <content>)
	"""
	if unitXs[0] != '$unit':
		raise GSymDocumentInvalidStructure
	
	docXs = unitXs[1]
	
	
	
	
	if not isGLispList( docXs ):
		raise GSymDocumentInvalidStructure
	
	if len( docXs ) < 1:
		raise GSymDocumentInvalidStructure
	
		
		
	if docXs[0] == '$withLanguageModule':
		"""
		($withLanguageModule <language_module_to_import> <content>)
		"""
		if len( docXs ) != 3:
			raise GSymDocumentContentInvalidStructure
		
		languageImport = docXs[1]
		
		return contentHandler.withLanguageModule( world, languageImport, docXs[2] )
	else:
		raise GSymDocumentContentInvalidType

