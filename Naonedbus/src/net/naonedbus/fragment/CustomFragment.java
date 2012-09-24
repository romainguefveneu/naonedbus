package net.naonedbus.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class CustomFragment extends Fragment implements CustomFragmentActions {

	protected int titleId;
	protected int layoutId;

	protected CustomFragment(final int titleId, final int layoutId) {
		this.titleId = titleId;
		this.layoutId = layoutId;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(this.layoutId, container, false);
		bindView(view, savedInstanceState);
		return view;
	}

	protected abstract void bindView(View view, Bundle savedInstanceState);

	public int getTitleId() {
		return titleId;
	}

}
