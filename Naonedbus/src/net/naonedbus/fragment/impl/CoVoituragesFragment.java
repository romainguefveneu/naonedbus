package net.naonedbus.fragment.impl;

import net.naonedbus.R;
import net.naonedbus.bean.Equipement;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.fragment.EquipementFragment;

public class CoVoituragesFragment extends EquipementFragment implements CustomFragmentActions {

	public CoVoituragesFragment() {
		super(R.string.title_fragment_covoiturage, R.layout.fragment_listview_section, Equipement.Type.TYPE_COVOITURAGE);
	}

}
