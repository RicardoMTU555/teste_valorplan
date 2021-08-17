/*
 * https://roytuts.com/jsf-2-javax-faces-application-viewexpiredexception/
 * DOCUMENTAÇÃO: https://qastack.com.br/programming/3642919/javax-faces-application-viewexpiredexception-view-could-not-be-restored
 */

package com.ctbli9.valorplan.recursos;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;
public class ViewExpiredExceptionHandlerFactory extends ExceptionHandlerFactory {
	private ExceptionHandlerFactory factory;
	public ViewExpiredExceptionHandlerFactory(ExceptionHandlerFactory factory) {
		this.factory = factory;
	}
	// This method is called once per request must return a new ExceptionHandler
	// instance each time it's called
	@Override
	public ExceptionHandler getExceptionHandler() {
		//System.out.println("CALL HANDLER!!!");
		// call the real ExceptionHandlerFactory and ask it to create the instance,
		// which we then wrap in our custom ViewExpiredExceptionHandlerFactory class
		ExceptionHandler handler = factory.getExceptionHandler();
		handler = new ViewExpiredExceptionHandler(handler);
		return handler;
	}
}