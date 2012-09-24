package net.naonedbus.fragment.impl;

import net.naonedbus.R;
import net.naonedbus.bean.Equipement.Type;
import net.naonedbus.fragment.EquipementFragment;
import net.naonedbus.manager.impl.EquipementManager.SousType;

public class ParkingsRelaisFragment extends EquipementFragment {

	public ParkingsRelaisFragment() {
		super(R.string.title_fragment_parkings_relais, R.layout.fragment_listview_section, Type.TYPE_PARKING,
				SousType.PARKING_RELAI);
	}

}
