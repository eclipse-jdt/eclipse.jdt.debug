package org.eclipse.jdt.internal.debug.ui.display;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdt.debug.core.IJavaValue;

/**
 * An implementation of an expression produced from the
 * inspect action. An inspect expression removes
 * itself from the expression manager when its debug
 * target terminates.
 */
public class JavaInspectExpression extends PlatformObject implements IExpression, IDebugEventSetListener {
	
	/**
	 * The value of this expression
	 */
	private IJavaValue fValue;
	
	/**
	 * The code snippet for this expression.
	 */
	private String fExpression;

	/**
	 * Constucts a new inspect result for the given
	 * expression and resulting value. Starts listening
	 * to debug events such that this element will remove
	 * itself from the expression manager when its debug
	 * target terminates.
	 * 
	 * @param expression code snippet
	 * @param value value of the expression
	 */
	public JavaInspectExpression(String expression, IJavaValue value) {
		fValue = value;
		fExpression = expression;
		DebugPlugin.getDefault().addDebugEventListener(this);
	}
	
	/**
	 * @see IExpression#getExpressionText()
	 */
	public String getExpressionText() {
		return fExpression;
	}

	/**
	 * @see IExpression#getValue()
	 */
	public IValue getValue() {
		return fValue;
	}

	/**
	 * @see IDebugElement#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget() {
		return getValue().getDebugTarget();
	}

	/**
	 * @see IDebugElement#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return getValue().getModelIdentifier();
	}

	/**
	 * @see IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch() {
		return getValue().getLaunch();
	}

	/**
	 * @see IDebugEventListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getKind() == DebugEvent.TERMINATE && event.getSource().equals(getDebugTarget())) {
				DebugPlugin.getDefault().getExpressionManager().removeExpression(this);
			}
		}
	}

	/**
	 * @see IExpression#dispose()
	 */
	public void dispose() {
		DebugPlugin.getDefault().removeDebugEventListener(this);		
	}

}
