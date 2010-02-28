##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.gSym.gSymUnitClass import GSymUnitClass, GSymUnitFactory
from Britefury.gSym.gSymDocument import gSymUnit

from GSymCore.Project.ProjectEditor.View import viewProjectDocNodeAsElement, viewProjectDocNodeAsPage, resolveProjectLocation
from GSymCore.Project import NodeClasses as Nodes


def newProject():
	package = Nodes.Package( name='Root', contents=[] )
	project = Nodes.Project( rootPackage=package )
	return gSymUnit( Nodes.schema, project )



unitClass = GSymUnitClass( Nodes.schema )
unitClass.registerViewDocNodeAsElementFn( viewProjectDocNodeAsElement )
unitClass.registerViewDocNodeAsPageFn( viewProjectDocNodeAsPage )
unitClass.registerResolveLocationFn( resolveProjectLocation )


newUnitFactory = GSymUnitFactory( 'gSym Document', newProject )

