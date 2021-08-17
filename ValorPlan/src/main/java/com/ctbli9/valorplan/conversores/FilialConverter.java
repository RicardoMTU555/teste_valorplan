package com.ctbli9.valorplan.conversores;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;

import ctbli9.modelo.Filial;
import ctbli9.negocio.FilialService;

@FacesConverter(forClass = Filial.class)
public class FilialConverter implements Converter {
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Filial filial = null;
		if (value == null) {
			return null;
		}
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			filial = new FilialService(con.getConexao()).pesquisar(Integer.parseInt(value));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConexaoDB.close(con);
		}
		return filial;
	}
	
	public String getAsString(FacesContext context, UIComponent component, Object object) throws ConverterException {
		if (object == null) {
			return null;
		}
		Filial filial = (Filial) object;
		return Integer.toString(filial.getCdFilial());
	}
}