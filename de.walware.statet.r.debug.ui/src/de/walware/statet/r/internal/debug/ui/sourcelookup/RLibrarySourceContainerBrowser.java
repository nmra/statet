/*=============================================================================#
 # Copyright (c) 2010-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.internal.debug.ui.sourcelookup;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.ui.sourcelookup.AbstractSourceContainerBrowser;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.walware.ecommons.databinding.jface.DataBindingSupport;
import de.walware.ecommons.ui.dialogs.ExtStatusDialog;
import de.walware.ecommons.ui.util.DialogUtil;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.workbench.ResourceInputComposite;

import de.walware.statet.r.debug.core.sourcelookup.RLibrarySourceContainer;


public class RLibrarySourceContainerBrowser extends
		AbstractSourceContainerBrowser {
	
	
	private static class EditContainerDialog extends ExtStatusDialog {
		
		
		private ResourceInputComposite resourceControl;
		
		private final WritableValue resourceValue;
		
		
		public EditContainerDialog(final Shell parent, final String location) {
			super(parent, (location == null) ? WITH_DATABINDING_CONTEXT :
					(WITH_DATABINDING_CONTEXT | SHOW_INITIAL_STATUS) );
			setTitle((location == null) ? Messages.RLibrarySourceContainerBrowser_Add_title :
					Messages.RLibrarySourceContainerBrowser_Edit_title );
			
			this.resourceValue= new WritableValue(location, String.class);
		}
		
		
		@Override
		protected Control createDialogArea(final Composite parent) {
			final Composite area= new Composite(parent, SWT.NONE);
			area.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
			area.setLayout(LayoutUtil.applyDialogDefaults(new GridLayout(), 1));
			
			final Composite composite= area;
			{	final Label label= new Label(area, SWT.NONE);
				label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
				label.setText(Messages.RLibrarySourceContainerBrowser_Directory_label+':');
				
				this.resourceControl= new ResourceInputComposite(composite, ResourceInputComposite.STYLE_TEXT,
						ResourceInputComposite.MODE_DIRECTORY | ResourceInputComposite.MODE_OPEN,
						Messages.RLibrarySourceContainerBrowser_Directory_label);
				this.resourceControl.setShowInsertVariable(false, DialogUtil.DEFAULT_NON_ITERACTIVE_FILTERS, null);
				final GridData gd= new GridData(SWT.FILL, SWT.CENTER, true, false);
				gd.widthHint= LayoutUtil.hintWidth((Text) this.resourceControl.getTextControl(), 60);
				this.resourceControl.setLayoutData(gd);
			}
			
			applyDialogFont(area);
			
			return area;
		}
		
		@Override
		protected void addBindings(final DataBindingSupport db) {
			db.getContext().bindValue(this.resourceControl.getObservable(), this.resourceValue,
					new UpdateValueStrategy().setAfterGetValidator(this.resourceControl.getValidator()), null);
		}
		
		public String getResult() {
			return (String) this.resourceValue.getValue();
		}
	}
	
	
	/** Created via extension point */
	public RLibrarySourceContainerBrowser() {
	}
	
	
	@Override
	public ISourceContainer[] addSourceContainers(final Shell shell,
			final ISourceLookupDirector director) {
		final EditContainerDialog dialog= new EditContainerDialog(shell, null);
		if (dialog.open() == Dialog.OK) {
			final String location= dialog.getResult();
			if (location != null) {
				return new ISourceContainer[] { new RLibrarySourceContainer(location) };
			}
		}
		return new ISourceContainer[0];
	}
	
	@Override
	public boolean canEditSourceContainers(final ISourceLookupDirector director,
			final ISourceContainer[] containers) {
		return (containers.length == 1);
	}
	
	@Override
	public ISourceContainer[] editSourceContainers(final Shell shell,
			final ISourceLookupDirector director, final ISourceContainer[] containers) {
		final EditContainerDialog dialog= new EditContainerDialog(shell,
				((RLibrarySourceContainer) containers[0]).getLocationPath());
		if (dialog.open() == Dialog.OK) {
			final String location= dialog.getResult();
			if (location != null) {
				return new ISourceContainer[] { new RLibrarySourceContainer(location) };
			}
		}
		return containers;
	}
	
}
