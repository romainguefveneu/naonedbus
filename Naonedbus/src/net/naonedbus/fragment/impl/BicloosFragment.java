package net.naonedbus.fragment.impl;

import net.naonedbus.R;
import net.naonedbus.bean.Equipement;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.fragment.EquipementFragment;

public class BicloosFragment extends EquipementFragment implements CustomFragmentActions {

	public BicloosFragment() {
		super(R.string.title_fragment_bicloos, R.layout.fragment_listview_section, Equipement.Type.TYPE_BICLOO);
	}

}
