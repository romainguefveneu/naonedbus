/**
 * Copyright (C) 2013 Romain Guefveneu.
 *   
 *  This file is part of naonedbus.
 *   
 *  Naonedbus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Naonedbus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.naonedbus.widget.indexer.impl;

import java.util.List;

import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.TypeLigne;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import android.content.Context;
import android.util.SparseArray;

public class LigneArrayIndexer extends ArraySectionIndexer<Ligne> {

	private final SparseArray<String> mTypesLignes;

	public LigneArrayIndexer(final List<TypeLigne> typesLignes) {
		mTypesLignes = new SparseArray<String>();
		for (final TypeLigne typeLigne : typesLignes) {
			mTypesLignes.put(typeLigne._id, typeLigne.nom);
		}
	}

	@Override
	protected String getSectionLabel(final Context context, final Ligne item) {
		return mTypesLignes.get((Integer) item.getSection());
	}

	@Override
	protected void prepareSection(final Ligne item) {
	}

}
