package org.javasimon.console.action;

import org.javasimon.console.Action;
import org.javasimon.console.ActionContext;
import org.javasimon.console.ActionException;

import java.io.IOException;

import javax.servlet.ServletException;

/**
 * Action to clear the Simon manager and remove all Simons.
 *
 * @author gquintana
 */
public class ClearAction extends Action {

	/** URI for clear action. */
	public static final String PATH = "/data/clear";

	public ClearAction(ActionContext context) {
		super(context);
	}

	@Override
	public void execute() throws ServletException, IOException, ActionException {
		getContext().getManager().clear();
	}
}
