/*******************************************************************************
 * Copyright (c) 2006-2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ui.dialogs;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.views.navigator.ResourceComparator;


/**
 *
 */
public class TreeAndListGroup implements ISelectionChangedListener {
	
	private Object fRoot;
	private Object fCurrentTreeSelection;
	private ListenerList fSelectionChangedListeners = new ListenerList();
	private ListenerList fDoubleClickListeners = new ListenerList();

	private ITreeContentProvider fTreeContentProvider;
	private IStructuredContentProvider fListContentProvider;
	private ILabelProvider fTreeLabelProvider;
	private ILabelProvider fListLabelProvider;

	// widgets
	private Composite fControl;
	private SashForm fSplitControl;
	private TreeViewer fTreeViewer;
	private TableViewer fListViewer;
	private boolean fAllowMultiselection = false;

	/**
	 *	Create an instance of this class.  Use this constructor if you wish to specify
	 *	the width and/or height of the combined widget (to only hardcode one of the
	 *	sizing dimensions, specify the other dimension's value as -1)
	 *
	 *	@param parent org.eclipse.swt.widgets.Composite
	 *	@param style int
	 *  @param rootObject java.lang.Object
	 *	@param width int
	 *	@param height int
	 *  @param allowMultiselection Whether to allow multi-selection in the list viewer.
	 */
	public TreeAndListGroup(Composite parent, Object rootObject, 
			ITreeContentProvider treeContentProvider, ILabelProvider treeLabelProvider, 
			IStructuredContentProvider listContentProvider, ILabelProvider listLabelProvider,
			boolean allowMultiselection) {

		fRoot = rootObject;
		this.fTreeContentProvider = treeContentProvider;
		this.fListContentProvider = listContentProvider;
		this.fTreeLabelProvider = treeLabelProvider;
		this.fListLabelProvider = listLabelProvider;
		this.fAllowMultiselection = allowMultiselection;
		
		fControl = createControls(parent);
	}
	
	public Composite getControl() {
		
		return fControl;
	}
	
//	/**
//	 * This method must be called just before this window becomes visible.
//	 */
//	public void initDefaultSelection() {
//	
//		fCurrentTreeSelection = null;
//
//		//select the first element in the list
//		Object[] elements = fTreeContentProvider.getElements(fRoot);
//		Object primary = elements.length > 0 ? elements[0] : null;
//		if (primary != null) {
//			fTreeViewer.setSelection(new StructuredSelection(primary));
//		}
//	}
	
	/**
	 *	Add the passed listener to collection of clients
	 *	that listen for changes to list viewer selection state
	 *
	 *	@param listener ISelectionChangedListener
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		
		fSelectionChangedListeners.add(listener);
	}
	
	/**
	 * Add the given listener to the collection of clients that listen to
	 * double-click events in the list viewer
	 * 
	 * @param listener IDoubleClickListener
	 */
	public void addDoubleClickListener(IDoubleClickListener listener) {
		
		fDoubleClickListeners.add(listener);
	}

	/**
	 * Notify all selection listeners that a selection has occurred in the list
	 * viewer
	 */
	protected void notifySelectionListeners(SelectionChangedEvent event) {
		
		for (Object o : fSelectionChangedListeners.getListeners()) {
			 ((ISelectionChangedListener) o).selectionChanged(event);
		}
	}
	
	/**
	 * Notify all double click listeners that a double click event has occurred
	 * in the list viewer
	 */
	protected void notifyDoubleClickListeners(DoubleClickEvent event) {
		
		for (Object o : fDoubleClickListeners.getListeners()) {
			((IDoubleClickListener) o).doubleClick(event);
		}
	}

	/**
	 *	Lay out and initialize self's visual components.
	 *
	 *	@param parent org.eclipse.swt.widgets.Composite
	 *	@param width int
	 *	@param height int
	 */
	protected Composite createControls(Composite parent) {
		
		fSplitControl = new SashForm(parent, SWT.HORIZONTAL);
		fSplitControl.setVisible(true);
		fSplitControl.setFont(parent.getFont());

		createTreeViewer(fSplitControl);
		createListViewer(fSplitControl);
		fSplitControl.setWeights(new int[] { 1, 1 });

		return fSplitControl;
	}
	/**
	 *	Create this group's list viewer.
	 */
	protected void createListViewer(Composite parent) {
		
		int style;
		if (fAllowMultiselection) {
			style= SWT.MULTI;
		} else {
			style= SWT.SINGLE;
		}
		fListViewer = new TableViewer(parent, SWT.BORDER | style);
		GridData data = new GridData(GridData.FILL_BOTH);
		fListViewer.getTable().setLayoutData(data);
		fListViewer.getTable().setFont(parent.getFont());
		fListViewer.setContentProvider(fListContentProvider);
		fListViewer.setLabelProvider(fListLabelProvider);
		fListViewer.setComparator(new ResourceComparator(ResourceComparator.NAME));
		fListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				notifySelectionListeners(event);
			}
		});
		fListViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (!event.getSelection().isEmpty()) {
					notifyDoubleClickListeners(event);
				}
			}
		});
	}
	
	/**
	 *	Create this group's tree viewer.
	 */
	protected void createTreeViewer(Composite parent) {
		
		Tree tree = new Tree(parent, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		tree.setLayoutData(data);
		tree.setFont(parent.getFont());

		fTreeViewer = new TreeViewer(tree);
		fTreeViewer.setContentProvider(fTreeContentProvider);
		fTreeViewer.setLabelProvider(fTreeLabelProvider);
		fTreeViewer.setComparator(new ResourceComparator(ResourceComparator.NAME));
		fTreeViewer.addSelectionChangedListener(this);
		
	}
	
	public Table getListTable() {
		
		return fListViewer.getTable();
	}
	
	public IStructuredSelection getListSelection() {
		
		ISelection selection=  this.fListViewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			return (IStructuredSelection)selection;
		} 
		return StructuredSelection.EMPTY;
	}
	
	public void selectListElement(Object element) {
		
		Object parent = fTreeContentProvider.getParent(element);
		fTreeViewer.setSelection(new StructuredSelection(parent), true);
		fListViewer.setSelection(new StructuredSelection(element), true);
	}
	
	public void selectTreeElement(Object element) {
		
		fTreeViewer.setSelection(new StructuredSelection(element), true);
	}
	
	/**
	 *	Initialize this group's viewers after they have been laid out.
	 */
	protected void initFields() {
		
		fTreeViewer.setInput(fRoot);
	}

	public void selectionChanged(SelectionChangedEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Object selectedElement = selection.getFirstElement();
		if (selectedElement != fCurrentTreeSelection) {
			fCurrentTreeSelection = selectedElement;
			fListViewer.setInput(selectedElement);
			notifySelectionListeners(event);
		}
	}
	
	/**
	 *	Set the list viewer's providers to those passed
	 *
	 *	@param contentProvider ITreeContentProvider
	 *	@param labelProvider ILabelProvider
	 */
	public void setListProviders(IStructuredContentProvider contentProvider, ILabelProvider labelProvider) {
		fListViewer.setContentProvider(contentProvider);
		fListViewer.setLabelProvider(labelProvider);
	}
	
	/**
	 *	Set the sorter that is to be applied to self's list viewer
	 */
	public void setListSorter(ViewerSorter sorter) {
		fListViewer.setSorter(sorter);
	}
	
	/**
	 * Set the root of the widget to be new Root. Regenerate all of the tables and lists from this
	 * value.
	 * @param newRoot
	 */
	public void setRoot(Object newRoot) {
		
		fRoot = newRoot;
		initFields();
	}

	/**
	 *	Set the tree viewer's providers to those passed
	 *
	 *	@param contentProvider ITreeContentProvider
	 *	@param labelProvider ILabelProvider
	 */
	public void setTreeProviders(ITreeContentProvider contentProvider, ILabelProvider labelProvider) {
		
		fTreeViewer.setContentProvider(contentProvider);
		fTreeViewer.setLabelProvider(labelProvider);
	}
	
	/**
	 *	Set the sorter that is to be applied to self's tree viewer
	 */
	public void setTreeSorter(ViewerSorter sorter) {
		fTreeViewer.setSorter(sorter);
	}

	/**
	 * Set the focus on to the list widget.
	 */
	public void setFocus() {

		fTreeViewer.getTree().setFocus();
	}
}