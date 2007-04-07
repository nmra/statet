/*******************************************************************************
 * Copyright (c) 2006 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.core;

import de.walware.eclipsecommons.preferences.Preference.IntPref;



/**
 *
 */
public class NicoPreferenceNodes {


	public static final String CAT_HISTORY_QUALIFIER = NicoCore.PLUGIN_ID + "/history"; //$NON-NLS-1$

	
	public static final IntPref KEY_DEFAULT_TIMEOUT = new IntPref(NicoCore.PLUGIN_ID, "timeout.default"); //$NON-NLS-1$
	
}