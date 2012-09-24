package net.naonedbus.fragment.impl;

import net.naonedbus.R;
import net.naonedbus.bean.Equipement;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.fragment.EquipementFragment;

public class MargueritesFragment extends EquipementFragment implements CustomFragmentActions {

	public MargueritesFragment() {
		super(R.string.title_fragment_marguerites, R.layout.fragment_listview_section, Equipement.Type.TYPE_MARGUERITE);
	}

}
