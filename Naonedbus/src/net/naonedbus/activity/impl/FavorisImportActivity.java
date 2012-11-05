package net.naonedbus.activity.impl;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.activity.SimpleActivity;
import net.naonedbus.manager.impl.FavoriManager;
import net.naonedbus.utils.FontUtils;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class FavorisImportActivity extends SimpleActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_favoris_import);

		final Typeface robotoLight = FontUtils.getRobotoLight(this);

		final TextView title = (TextView) findViewById(android.R.id.title);
		title.setTypeface(robotoLight);

		final EditText code = (EditText) findViewById(android.R.id.input);

		final Button importBtn = (Button) findViewById(android.R.id.button1);
		importBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new ImportTask().execute(code.getText().toString());
			}
		});
	}

	/**
	 * Classe d'import des favoris
	 * 
	 * @author romain
	 */
	private class ImportTask extends AsyncTask<String, Void, Void> {
		private Exception exception = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(String... params) {
			final FavoriManager favoriManager = FavoriManager.getInstance();

			try {
				favoriManager.importFavoris(getContentResolver(), params[0]);
			} catch (Exception e) {
				exception = e;
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (exception == null) {
				finish();
			} else {
				Log.w(NBApplication.LOG_TAG, "Erreur lors de l'import des favoris", exception);
			}
		}

	}

}
