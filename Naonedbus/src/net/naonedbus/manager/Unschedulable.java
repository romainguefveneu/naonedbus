package net.naonedbus.manager;

import net.naonedbus.bean.async.AsyncTaskInfo;

public interface Unschedulable<T extends AsyncTaskInfo<?>> {
	/**
	 * Annuler une tâche.
	 * 
	 * @param task
	 *            La tâche à annuler.
	 */
	void unschedule(T task);
}
