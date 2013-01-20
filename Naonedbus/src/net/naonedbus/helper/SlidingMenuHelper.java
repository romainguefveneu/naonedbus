package net.naonedbus.helper;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.impl.AboutActivity;
import net.naonedbus.activity.impl.EquipementsActivity;
import net.naonedbus.activity.impl.InfosTraficActivity;
import net.naonedbus.activity.impl.MainActivity;
import net.naonedbus.activity.impl.MapActivity;
import net.naonedbus.activity.impl.ParkingsActivity;
import net.naonedbus.activity.impl.SettingsActivity;
import net.naonedbus.utils.DpiUtils;
import net.naonedbus.widget.adapter.impl.MainMenuAdapter;
import net.naonedbus.widget.indexer.impl.MainMenuIndexer;
import net.naonedbus.widget.item.impl.LinkMainMenuItem;
import net.naonedbus.widget.item.impl.MainMenuItem;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.slidingmenu.lib.SlidingMenu;

public class SlidingMenuHelper {
	/**
	 * Menu général.
	 */
	private ListView menuListView;
	private static MainMenuAdapter adapter;
	private static int savedPosition = -1;
	private static int savedListTop;

	/**
	 * Contenu du menu général.
	 */
	private static List<MainMenuItem> menuItems;
	static {
		// @formatter:off
		menuItems = new ArrayList<MainMenuItem>();
		menuItems.add(new MainMenuItem(R.string.menu_accueil, MainActivity.class, R.drawable.ic_menu_home, 0));
		menuItems.add(new MainMenuItem(R.string.menu_info_trafic, InfosTraficActivity.class,R.drawable.ic_menu_notifications, 0));
//		menuItems.add(new MainMenuItem(R.string.menu_itineraires, AboutActivity.class, R.drawable.ic_menu_directions, 0));
		menuItems.add(new MainMenuItem(R.string.menu_parkings, ParkingsActivity.class, R.drawable.ic_menu_parking, 0));
		menuItems.add(new MainMenuItem(R.string.menu_equipements, EquipementsActivity.class, R.drawable.ic_menu_goto, 0));
		menuItems.add(new MainMenuItem(R.string.menu_carte, MapActivity.class, R.drawable.ic_menu_mapmode, 0));
		menuItems.add(new MainMenuItem(R.string.menu_parametres, SettingsActivity.class, R.drawable.ic_menu_manage, 1));
		menuItems.add(new MainMenuItem(R.string.menu_about, AboutActivity.class, R.drawable.ic_menu_info_details, 1));
		menuItems.add(new LinkMainMenuItem(R.string.menu_don, "http://t.co/4uKK33eu", R.drawable.ic_menu_star, 1));
//		menuItems.add(new LinkMainMenuItem(R.string.menu_bug, "mailto:naonedbus@gmail.com?subject=Bug&body=Bonjour,", R.drawable.ic_menu_emoticons, 1));
		// @formatter:on
	}

	private Activity activity;

	public SlidingMenuHelper(Activity activity) {
		this.activity = activity;
	}

	public void onPostCreate(final Intent intent, final SlidingMenu slidingMenu, final Bundle savedInstanceState) {
		if (intent.getBooleanExtra("fromMenu", false)
				&& (savedInstanceState == null || !savedInstanceState.containsKey("menuConsumed"))) {
			// Afficher le menu au démarrage, pour la transition.
			slidingMenu.showMenu();

			// Masquer le menu.
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					slidingMenu.showContent();
				}
			}, 350);

			// Consommer l'affichage du menu pour ne pas réafficher en cas de
			// rotation.
			intent.putExtra("fromMenu", false);
		}
	}

	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("menuConsumed", true);
	}

	public void onWindowFocusChanged(boolean hasFocus, final SlidingMenu slidingMenu) {
		// Gérer le masquage de menu
		if (hasFocus == false && slidingMenu.isMenuShowing()) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					slidingMenu.showContent(false);
				}
			}, 500);
		}
	}

	public void setupSlidingMenu(final SlidingMenu slidingMenu) {
		Display display = this.activity.getWindowManager().getDefaultDisplay();
		int width = display.getWidth(); // deprecated

		slidingMenu.setBehindWidth(Math.min(width - DpiUtils.getDpiFromPx(activity, 48),
				DpiUtils.getDpiFromPx(activity, 380)));
		slidingMenu.setBehindScrollScale(0.3f);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
		slidingMenu.setShadowDrawable(R.drawable.shadow);
		slidingMenu.setMenu(R.layout.menu);

		menuListView = (ListView) slidingMenu.findViewById(android.R.id.list);
		if (adapter == null) {
			adapter = new MainMenuAdapter(activity);
			for (MainMenuItem item : menuItems) {
				adapter.add(item);
			}
			final MainMenuIndexer indexer = new MainMenuIndexer();
			indexer.buildIndex(activity, adapter);
			adapter.setIndexer(indexer);
		}
		adapter.setCurrentClass(activity.getClass());
		menuListView.setAdapter(adapter);

		if (savedPosition >= 0) { // initialized to -1
			menuListView.setSelectionFromTop(savedPosition, savedListTop);
		}

		menuListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// Ne pas permettre à l'utilisateur de cliquer n'importe où...
				menuListView.setClickable(false);

				// Sauvegarder l'état de la listview
				savedPosition = menuListView.getFirstVisiblePosition();
				final View firstVisibleView = menuListView.getChildAt(0);
				savedListTop = (firstVisibleView == null) ? 0 : firstVisibleView.getTop();

				final MainMenuItem item = (MainMenuItem) menuListView.getItemAtPosition(position);

				if (item instanceof LinkMainMenuItem) {
					final LinkMainMenuItem linkItem = (LinkMainMenuItem) item;
					final Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(linkItem.getUrl()));
					activity.startActivity(intent);
				} else {
					if (activity.getClass().equals(item.getIntentClass())) {
						// Même activité
						slidingMenu.showContent();
						menuListView.setClickable(true);
					} else {
						// Nouvelle activité
						final Intent intent = new Intent(activity, item.getIntentClass());
						intent.putExtra("fromMenu", true);
						activity.startActivity(intent);
						activity.overridePendingTransition(0, android.R.anim.fade_out);
					}
				}

			}
		});

	}

	public void setupActionBar(ActionBar actionBar) {
		actionBar.setBackgroundDrawable(this.activity.getResources().getDrawable(R.drawable.actionbar_back));
		actionBar.setDisplayHomeAsUpEnabled(true);
	}
}
