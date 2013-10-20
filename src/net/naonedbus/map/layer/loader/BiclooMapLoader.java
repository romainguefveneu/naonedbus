package net.naonedbus.map.layer.loader;

import java.io.IOException;
import java.util.ArrayList;

import net.naonedbus.bean.Equipment.Type;
import net.naonedbus.manager.impl.BiclooManager;

import org.json.JSONException;

import android.content.Context;

import com.twotoasters.clusterkraf.InputPoint;

public class BiclooMapLoader extends EquipementMapLoader {

	public BiclooMapLoader() {
		super(Type.TYPE_BICLOO);
	}

	@Override
	public ArrayList<InputPoint> getInputPoints(final Context context) {
		final BiclooManager manager = BiclooManager.getInstance();
		try {
			manager.getAll(context);
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final JSONException e) {
			e.printStackTrace();
		}
		return super.getInputPoints(context);
	}

}
