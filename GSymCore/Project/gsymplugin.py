##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from GSymCore.Project import NodeClasses as Nodes
from GSymCore.Project import Project



def initPlugin(plugin, world):
	world.registerDMModule( plugin, Nodes.module )
	world.registerLanguage( plugin, Project.language )
	world.registerNewDocumentFactory( plugin, Project.newDocumentFactory )
