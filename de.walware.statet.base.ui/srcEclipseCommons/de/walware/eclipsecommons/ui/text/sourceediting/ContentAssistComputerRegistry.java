/*******************************************************************************
 * Copyright (c) 2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ui.text.sourceediting;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.Bundle;

import de.walware.eclipsecommons.ICommonStatusConstants;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


/**
 * Registry of contributions for {@link ContentAssistProcessor}
 */
public class ContentAssistComputerRegistry {
	
	
	/** The extension schema name of the contribution id attribute. */
	private static final String CONFIG_ID_ATTRIBUTE_NAME = "id"; //$NON-NLS-1$
	/** The extension schema name of the contribution name attribute. */
	private static final String CONFIG_NAME_ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	/** The extension schema name of the category id attribute. */
	private static final String CONFIG_CATEGORY_ID_ATTRIBUTE_NAME = "categoryId"; //$NON-NLS-1$
	/** The extension schema name of the partition type element. */
	private static final String CONFIG_PARTITION_ELEMENT_NAME = "partition"; //$NON-NLS-1$
	/** The extension schema name of the computer type element. */
	private static final String CONFIG_COMPUTER_ELEMENT_NAME = "computer"; //$NON-NLS-1$
	/** The extension schema name of the computer type element. */
	private static final String CONFIG_CATEGORY_ELEMENT_NAME = "category"; //$NON-NLS-1$
	/** The extension schema name of the content type id attribute of the partition. */
	private static final String CONFIG_CONTENTTYPE_ID_ELEMENT_NAME = "partitionType"; //$NON-NLS-1$
	/** The extension schema name of the class attribute. */
	private static final String CONFIG_CLASS_ATTRIBUTE_NAME = "class"; //$NON-NLS-1$
	/** The extension schema name of the icon resource attribute. */
	private static final String CONFIG_ICON_ATTRIBUTE_NAME = "icon"; //$NON-NLS-1$
	
	
	private static Bundle getBundle(final IConfigurationElement element) {
		final String namespace = element.getDeclaringExtension().getContributor().getName();
		final Bundle bundle = Platform.getBundle(namespace);
		return bundle;
	}
	
	private static String getCheckedString(final IConfigurationElement element, final String attrName) throws CoreException {
		final String s = element.getAttribute(attrName);
		if (s == null || s.length() == 0) {
			throw new CoreException(new Status(IStatus.ERROR, StatetUIPlugin.PLUGIN_ID, -1,
					NLS.bind("missing value for attribute ''{0}''", attrName), null));
		}
		return s;
	}
	
	private static ImageDescriptor getImageDescriptor(final IConfigurationElement element, final String attrName) throws CoreException {
		final String imagePath = element.getAttribute(attrName);
		if (imagePath != null) {
			final Bundle bundle = getBundle(element);
			final URL url = FileLocator.find(bundle, new Path(imagePath), null);
			if (url != null) {
				return ImageDescriptor.createFromURL(url);
			}
		}
		return null;
	}
	
	
	/**
	 * The description of an {@link IContentAssistComputer}
	 */
	final class ComputerDescriptor {
		
		
		/** The identifier of the extension. */
		private final String fId;
		/** The class name of the provided <code>ICompletionProposalComputer</code>. */
		private final Set<String> fPartitions;
		/** The configuration element of this extension. */
		private final IConfigurationElement fConfigurationElement;
		/** The computer, if instantiated, <code>null</code> otherwise. */
		private IContentAssistComputer fComputer;
		/** Tells whether we tried to load the computer. */
		boolean fTriedLoadingComputer = false;
		
		
		/**
		 * Creates a new descriptor with lazy loaded computer
		 */
		ComputerDescriptor(final String id, final Set<String> partitions, final IConfigurationElement configurationElement) {
			fId = id;
			fPartitions = partitions;
			fConfigurationElement = configurationElement;
		}
		
		
		/**
		 * Returns the identifier of the described extension.
		 *
		 * @return Returns the id
		 */
		public String getId() {
			return fId;
		}
		
		/**
		 * Returns the partition types of the described extension.
		 * 
		 * @return the set of partition types (element type: {@link String})
		 */
		public Set<String> getPartitions() {
			return fPartitions;
		}
		
		/**
		 * Returns a cached instance of the computer
		 */
		public IContentAssistComputer getComputer() {
			if (fComputer == null && !fTriedLoadingComputer && fConfigurationElement != null) {
				fTriedLoadingComputer = true;
				try {
					fComputer = (IContentAssistComputer) fConfigurationElement.createExecutableExtension(CONFIG_CLASS_ATTRIBUTE_NAME);
				}
				catch (final CoreException e) {
					StatusManager.getManager().handle(new Status(IStatus.ERROR, fPluginId, -1,
							NLS.bind("Loading ICompletionProposalComputer with id ''{0}'' failed (contributed by = '' {1}'').", fId, fConfigurationElement.getDeclaringExtension().getContributor().getName()), e)); //$NON-NLS-1$
				}
			}
			return fComputer;
		}
		
	}
	
	
	private final String fPluginId;
	private final String fExtensionsPointName;
	
	private List<ContentAssistCategory> fCategories;
	
	
	public ContentAssistComputerRegistry(final String pluginId, final String extensionPointName) {
		fPluginId = pluginId;
		fExtensionsPointName = extensionPointName;
	}
	
	
	protected void loadExtensions() {
		final ArrayList<IConfigurationElement> categoryConfigs = new ArrayList<IConfigurationElement>();
		final Map<String, List<ComputerDescriptor>> computersByCategoryId = new HashMap<String, List<ComputerDescriptor>>();
		
		final IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		final IConfigurationElement[] contributions = extensionRegistry.getConfigurationElementsFor(fPluginId, fExtensionsPointName);
		for (final IConfigurationElement config : contributions) {
			if (config.getName().equals(CONFIG_CATEGORY_ELEMENT_NAME)) {
				categoryConfigs.add(config);
				continue;
			}
			if (config.getName().equals(CONFIG_COMPUTER_ELEMENT_NAME)) {
				// Create computer descriptor
				String id = null;
				try {
					id = getCheckedString(config, CONFIG_ID_ATTRIBUTE_NAME);
					final String categoryId = getCheckedString(config, CONFIG_CATEGORY_ID_ATTRIBUTE_NAME);
					final Set<String> partitions = new HashSet<String>();
					final IConfigurationElement[] partitionConfigs = config.getChildren(CONFIG_PARTITION_ELEMENT_NAME);
					for (final IConfigurationElement partitionConfig : partitionConfigs) {
						partitions.add(getCheckedString(partitionConfig, CONFIG_CONTENTTYPE_ID_ELEMENT_NAME));
					}
					checkPartitions(partitions);
					
					final ComputerDescriptor comp = new ComputerDescriptor(id, partitions, config);
					
					List<ComputerDescriptor> list = computersByCategoryId.get(categoryId);
					if (list == null) {
						list = new ArrayList<ComputerDescriptor>(4);
						computersByCategoryId.put(categoryId, list);
					}
					list.add(comp);
				}
				catch (final CoreException e) {
					StatusManager.getManager().handle(new Status(IStatus.ERROR, fPluginId, ICommonStatusConstants.INTERNAL_PLUGGED_IN,
							NLS.bind("Loading Completion Proposal Computer failed (id = ''{0}'', contributed by = ''{1}'')", (id != null) ? id : "", config.getDeclaringExtension().getContributor().getName()), e));
				}
			}
		}
		
		final ArrayList<ContentAssistCategory> categories = new ArrayList<ContentAssistCategory>(categoryConfigs.size());
		for (final IConfigurationElement catConfig : categoryConfigs) {
			// Create category descriptor
			String id = null;
			try {
				id = getCheckedString(catConfig, CONFIG_ID_ATTRIBUTE_NAME);
				final String name = getCheckedString(catConfig, CONFIG_NAME_ATTRIBUTE_NAME);
				final ImageDescriptor icon = getImageDescriptor(catConfig, CONFIG_ICON_ATTRIBUTE_NAME);
				final List<ComputerDescriptor> descriptors = computersByCategoryId.get(id);
				if (descriptors != null) {
					final ContentAssistCategory cat = new ContentAssistCategory(id, name, icon, descriptors);
					categories.add(cat);
				}
			}
			catch (final CoreException e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, fPluginId, ICommonStatusConstants.INTERNAL_PLUGGED_IN,
						NLS.bind("Loading Completion Proposal Category failed (id = ''{0}'', contributed by = ''{1}'')", (id != null) ? id : "", catConfig.getDeclaringExtension().getContributor().getName()), e));
			}
		}
		fCategories = Collections.unmodifiableList(categories);
	}
	
	
	protected void checkPartitions(final Set<String> partitions) {
	}
	
	
	public List<ContentAssistCategory> getCategories() {
		if (fCategories == null) {
			loadExtensions();
		}
		return fCategories;
	}
	
}