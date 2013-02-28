package net.naonedbus.activity.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.FragmentsActivity;
import net.naonedbus.activity.map.overlay.TypeOverlayItem;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Favori;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.fragment.impl.CommentairesFragment;
import net.naonedbus.fragment.impl.HorairesFragment;
import net.naonedbus.fragment.impl.HorairesFragment.OnSensChangeListener;
import net.naonedbus.fragment.impl.TanActuFragment;
import net.naonedbus.helper.HeaderHelper;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.ArretManager;
import net.naonedbus.manager.impl.FavoriManager;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.manager.impl.SensManager;
import net.naonedbus.utils.SymbolesUtils;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class HorairesActivity extends FragmentsActivity implements OnSensChangeListener {

	public static final String PARAM_LIGNE = "ligne";
	public static final String PARAM_SENS = "sens";
	public static final String PARAM_ARRET = "arret";

	private static int[] titles = new int[] { R.string.title_fragment_tan_actu, R.string.title_fragment_horaires,
			R.string.title_fragment_en_direct, };

	private static Class<?>[] classes = new Class<?>[] { TanActuFragment.class, HorairesFragment.class,
			CommentairesFragment.class };

	private HeaderHelper mHeaderHelper;
	private final ArretManager mArretManager;
	private final SensManager mSensManager;
	private final FavoriManager mFavoriManager;
	private OnSensChangeListener mOnSensChangeListener;

	private Ligne mLigne;
	private Sens mSens;
	private Arret mArret;

	public HorairesActivity() {
		super(R.layout.activity_horaires);
		mFavoriManager = FavoriManager.getInstance();
		mArretManager = ArretManager.getInstance();
		mSensManager = SensManager.getInstance();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		mArret = getIntent().getParcelableExtra(PARAM_ARRET);
		mLigne = getIntent().getParcelableExtra(PARAM_LIGNE);
		mSens = getIntent().getParcelableExtra(PARAM_SENS);

		if (mLigne == null) {
			final LigneManager ligneManager = LigneManager.getInstance();
			mLigne = ligneManager.getSingle(getContentResolver(), mArret.codeLigne);
		}
		if (mSens == null) {
			final SensManager sensManager = SensManager.getInstance();
			mSens = sensManager.getSingle(getContentResolver(), mArret.codeLigne, mArret.codeSens);
		}

		if (savedInstanceState == null) {
			final Bundle bundleHoraires = new Bundle();
			bundleHoraires.putParcelable(HorairesFragment.PARAM_LIGNE, mLigne);
			bundleHoraires.putParcelable(HorairesFragment.PARAM_SENS, mSens);
			bundleHoraires.putParcelable(HorairesFragment.PARAM_ARRET, mArret);

			final Bundle bundleCommentaires = new Bundle();
			bundleCommentaires.putString(CommentairesFragment.PARAM_CODE_LIGNE, mLigne.code);
			bundleCommentaires.putString(CommentairesFragment.PARAM_CODE_SENS, mSens.code);

			final Bundle bundleTanActu = new Bundle();
			bundleTanActu.putString(TanActuFragment.PARAM_CODE_LIGNE, mLigne.code);

			final Bundle[] bundles = new Bundle[] { bundleTanActu, bundleHoraires, bundleCommentaires };

			addFragments(titles, classes, bundles);
			setSelectedTab(1);
		}

		mHeaderHelper = new HeaderHelper(this);
		mHeaderHelper.setBackgroundColor(mLigne.couleurBackground, mLigne.couleurTexte);
		mHeaderHelper.setCode(mLigne.lettre);
		mHeaderHelper.setTitle(mArret.nomArret);
		mHeaderHelper.setSubTitle(SymbolesUtils.formatSens(mSens.text));

	}

	@Override
	public void onSensChange(Sens newSens) {
		mHeaderHelper.setSubTitleAnimated(SymbolesUtils.formatSens(newSens.text));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.activity_horaires, menu);
		final MenuItem menuFavori = menu.findItem(R.id.menu_favori);

		final int icon = isFavori() ? R.drawable.ic_action_important : R.drawable.ic_action_not_important;
		menuFavori.setIcon(icon);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		final MenuItem menuFavori = menu.findItem(R.id.menu_favori);

		final int icon = isFavori() ? R.drawable.ic_action_important : R.drawable.ic_action_not_important;
		menuFavori.setIcon(icon);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_favori:
			onStarClick();
			break;
		case R.id.menu_place:
			showArretPlan();
			break;
		case R.id.menu_comment:
			menuComment();
			break;
		case R.id.menu_show_plan:
			menuShowPlan();
			break;
		case R.id.menu_sens:
			menuChangeSens();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private boolean isFavori() {
		final Favori item = mFavoriManager.getSingle(this.getContentResolver(), mArret._id);
		return (item != null);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void onStarClick() {
		if (isFavori()) {
			removeFromFavoris();
			Toast.makeText(this, R.string.toast_favori_retire, Toast.LENGTH_SHORT).show();
		} else {
			addToFavoris();
			Toast.makeText(this, R.string.toast_favori_ajout, Toast.LENGTH_SHORT).show();
		}

		invalidateOptionsMenu();
	}

	private void addToFavoris() {
		mFavoriManager.addFavori(this.getContentResolver(), mArret);
	}

	private void removeFromFavoris() {
		mFavoriManager.removeFavori(this.getContentResolver(), mArret._id);
	}

	protected void showArretPlan() {
		final ParamIntent intent = new ParamIntent(this, MapActivity.class);
		intent.putExtra(MapActivity.Param.itemId, mArret.idStation);
		intent.putExtra(MapActivity.Param.itemType, TypeOverlayItem.TYPE_STATION.getId());
		startActivity(intent);
	}

	private void menuComment() {
		final ParamIntent intent = new ParamIntent(this, CommentaireActivity.class);
		intent.putExtra(CommentaireActivity.Param.idLigne, mLigne._id);
		intent.putExtra(CommentaireActivity.Param.idSens, mSens._id);
		intent.putExtra(CommentaireActivity.Param.idArret, mArret._id);
		startActivity(intent);
	}

	private void menuShowPlan() {
		final ParamIntent intent = new ParamIntent(this, PlanActivity.class);
		intent.putExtra(PlanActivity.Param.codeLigne, mArret.codeLigne);
		startActivity(intent);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void menuChangeSens() {
		Sens autreSens = null;

		// Inverser le sens
		final List<Sens> sens = mSensManager.getAll(this.getContentResolver(), mLigne.code);
		for (Sens sensItem : sens) {
			if (sensItem._id != mSens._id) {
				autreSens = sensItem;
				break;
			}
		}

		// Chercher l'arrêt dans le nouveau sens
		final Arret arret = mArretManager.getSingle(this.getContentResolver(), mLigne.code, autreSens.code,
				mArret.normalizedNom);

		if (arret != null) {
			mSens = autreSens;
			mArret = arret;

			// mAdapter.clear();
			// mAdapter.notifyDataSetChanged();
			//
			// changeDateToNow();

			if (mOnSensChangeListener != null) {
				mOnSensChangeListener.onSensChange(mSens);
			}

			invalidateOptionsMenu();
		} else {
			Toast.makeText(this, "Impossible de trouver l'arrêt dans l'autre sens.", Toast.LENGTH_SHORT).show();
		}

	}

}
