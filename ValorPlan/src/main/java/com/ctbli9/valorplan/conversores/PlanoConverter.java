package com.ctbli9.valorplan.conversores;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.negocio.PlanoService;

import ctbli9.modelo.Plano;

@FacesConverter(forClass = Plano.class)
public class PlanoConverter implements Converter {
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Plano pla = null;
		if (value == null) {
			return null;
		}
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			pla = new PlanoService(con).pesquisar(Integer.parseInt(value));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConexaoDB.close(con);
		}
		return pla;
	}
	
	public String getAsString(FacesContext context, UIComponent component, Object object) throws ConverterException {
		if (object == null) {
			return null;
		}
		Plano pla = (Plano) object;
		return Integer.toString(pla.getCdPlano());
	}
}
