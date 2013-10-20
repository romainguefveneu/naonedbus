package net.naonedbus.map.layer.loader;

import java.io.IOException;
import java.util.ArrayList;

import net.naonedbus.bean.Equipment.Type;
import net.naonedbus.manager.impl.ParkingPublicManager;
import net.naonedbus.manager.impl.ParkingRelaiManager;

import org.json.JSONException;

import android.content.Context;

import com.bugsense.trace.BugSenseHandler;
import com.twotoasters.clusterkraf.InputPoint;

public class ParkingMapLoader extends EquipementMapLoader {

	public ParkingMapLoader() {
		super(Type.TYPE_PARK);
	}

	@Override
	public ArrayList<InputPoint> getInputPoints(final Context context) {

		try {
			final ParkingPublicManager publicManager = ParkingPublicManager.getInstance();
			final ParkingRelaiManager relaiManager = ParkingRelaiManager.getInstance();

			publicManager.getAll(context);
			relaiManager.getAll(context.getContentResolver());
		} catch (final IOException e) {
			BugSenseHandler.sendException(e);
		} catch (final JSONException e) {
			BugSenseHandler.sendException(e);
		}

		return super.getInputPoints(context);
	}

}
