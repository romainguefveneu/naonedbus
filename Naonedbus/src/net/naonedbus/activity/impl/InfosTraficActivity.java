package net.naonedbus.activity.impl;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import net.naonedbus.R;
import net.naonedbus.activity.SlidingMenuActivity;
import net.naonedbus.fragment.impl.CommentairesFragment;
import net.naonedbus.fragment.impl.TanActuFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

public class InfosTraficActivity extends SlidingMenuActivity {

	private static int[] titles = new int[] { R.string.title_fragment_en_direct, R.string.title_fragment_tan_actu };

	private static Class<?>[] classes = new Class<?>[] { CommentairesFragment.class, TanActuFragment.class };

	public InfosTraficActivity() {
		super(R.layout.activity_tabs);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// This has to be called before setContentView and you must use the
		// class in com.actionbarsherlock.view and NOT android.view
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			addFragments(titles, classes);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.activity_en_direct, menu);
		return super.onCreateOptionsMenu(menu);

	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_comment:
			startActivity(new Intent(this, CommentaireActivity.class));
		default:
			super.onOptionsItemSelected(item);
			break;
		}
		return true;
	}

}
