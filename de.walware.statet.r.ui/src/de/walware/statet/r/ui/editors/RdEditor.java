/*******************************************************************************
 * Copyright (c) 2005-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.editors;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import de.walware.statet.base.ui.StatetUIServices;
import de.walware.statet.ext.ui.editors.SourceViewerConfigurator;
import de.walware.statet.ext.ui.editors.StatextEditor1;
import de.walware.statet.r.core.RProject;
import de.walware.statet.r.internal.ui.RUIPlugin;


public class RdEditor extends StatextEditor1<RProject, IContentOutlinePage> {

	
	RdSourceViewerConfigurator fRdConfig;

	
	public RdEditor() {
		super();
	}
	
	@Override
	protected SourceViewerConfigurator createConfiguration() {
		configureStatetProjectNatureId(RProject.NATURE_ID);
		setDocumentProvider(RUIPlugin.getDefault().getRdDocumentProvider());
		
		IPreferenceStore store = RUIPlugin.getDefault().getEditorPreferenceStore();
		fRdConfig = new RdSourceViewerConfigurator(null, store);
		fRdConfig.setConfiguration(new RdSourceViewerConfiguration(
				fRdConfig, store, StatetUIServices.getSharedColorManager()));
		return fRdConfig;
	}
	
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		fRdConfig.setTarget(this, getSourceViewer());
	}
	
	@Override
	protected void setupConfiguration(RProject prevProject, RProject newProject, IEditorInput newInput) {
		fRdConfig.setSource(newProject);
	}
	
	@Override
	protected String[] collectContextMenuPreferencePages() {
		String[] ids = super.collectContextMenuPreferencePages();
		String[] more = new String[ids.length + 1];
		more[0]= "de.walware.statet.r.preferencePages.RdSyntaxColoring"; //$NON-NLS-1$
		System.arraycopy(ids, 0, more, 1, ids.length);
		return more;
	}

}
