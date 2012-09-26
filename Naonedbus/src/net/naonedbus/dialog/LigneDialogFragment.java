package net.naonedbus.dialog;

import net.naonedbus.R;
import net.naonedbus.bean.Ligne;
import net.naonedbus.manager.impl.SensManager;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.widget.adapter.impl.SensArrayAdapter;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

public class LigneDialogFragment extends DialogFragment {

	public static final String BUNDLE_LIGNE = "ligne";

	private Ligne mLigne;
	private Typeface mRobotoLight;
	private SensManager mSensManager;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mRobotoLight = Typeface.createFromAsset(dialog.getContext().getAssets(), "fonts/Roboto-Light.ttf");

		mSensManager = SensManager.getInstance();

		return dialog;
	}

	@Override
	public void setArguments(Bundle args) {
		super.setArguments(args);
		mLigne = (Ligne) args.get(BUNDLE_LIGNE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.dialog_ligne, container);
		final View header = view.findViewById(R.id.ligne_dialog_header);
		final TextView code = (TextView) view.findViewById(R.id.ligne_dialog_code);
		final ListView listView = (ListView) view.findViewById(android.R.id.list);

		header.setBackgroundDrawable(ColorUtils.getGradiant(mLigne.couleurBackground));
		code.setText(mLigne.lettre);
		code.setTextColor(mLigne.couleurTexte);
		code.setTypeface(mRobotoLight);

		listView.setAdapter(new SensArrayAdapter(getActivity(), mSensManager.getAll(getActivity().getContentResolver(),
				mLigne.code)));

		return view;
	}
}
