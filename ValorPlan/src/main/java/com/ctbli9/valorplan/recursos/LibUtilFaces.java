package com.ctbli9.valorplan.recursos;

import java.util.Arrays;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.primefaces.PrimeFaces;

public class LibUtilFaces {
	
	public static void atualizarView(String... args) {
    	PrimeFaces.Ajax currentAjax = PrimeFaces.current().ajax();
	    currentAjax.update(Arrays.asList(args));

	}
	
	
	public static void abreRelatorio(String nomeRelatorio) {
		ServletContext sc = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
		String URL = sc.getContextPath() + "/relatorios/" + nomeRelatorio;
		sc = null;
		
		URL = "window.open('" + URL + "', '_blank')";
		PrimeFaces.current().executeScript(URL);
	}

}
