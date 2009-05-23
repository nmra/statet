/*******************************************************************************
 * Copyright (c) 2007-2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ltk.ast;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.text.IRegion;


/**
 * 
 */
public interface IAstNode extends IRegion {
	
	
	public int getStatusCode();
	
	public int getOffset();
	public int getStopOffset();
	public int getLength();
	
	public void accept(ICommonAstVisitor visitor) throws InvocationTargetException;
	public void acceptInChildren(ICommonAstVisitor visitor) throws InvocationTargetException;
	
	public IAstNode getParent();
	public IAstNode getRoot();
	public boolean hasChildren();
	public int getChildCount();
	public IAstNode getChild(int index);
//	public IAstNode[] getChildren();
	public int getChildIndex(IAstNode element);
	
}