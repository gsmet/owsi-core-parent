package fr.openwide.core.jpa.more.util.transaction.service;

import fr.openwide.core.jpa.more.util.transaction.model.TransactionSynchronizationTasks;

public interface ITransactionSynchronizationTaskMergerService {

	/**
	 * Merge the tasks.
	 * 
	 * <p>This methods offers the opportunity for multiple optimizations:
	 * <ul>
	 * <li>Duplicates removal
	 * <li>Grouping the equivalent of multiple pushed tasks in a single task
	 * <li>and so on.
	 * </ul>
	 * 
	 * <p>This method <strong>may be called multiple times for a given set of tasks</strong>,
	 * and should behave correctly in this case. In particular, if this method adds new types of tasks into the queues,
	 * it should also handle those types of tasks as an input in a later call.<br/>
	 * For instance, if a merger replaces multiple tasks of type <code>SingleTask</code> with a single
	 * task of type <code>MultipleTask</code>, then this same merger should probably account for the fact that
	 * there might be tasks of type <code>MultipleTask</code> <em>as an input</em>, who should probably be merged too.
	 * 
	 * <p>{@link TransactionSynchronizationTasks#getAlreadyExecutedBeforeClearTasks() Tasks that have already been executed}
	 * are provided for the sole purpose of allowing the merging of the rollback behavior.
	 */
	void merge(TransactionSynchronizationTasks tasks);

}
