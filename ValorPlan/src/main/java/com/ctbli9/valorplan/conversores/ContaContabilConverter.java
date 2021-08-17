package com.ctbli9.valorplan.conversores;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;

import ctbli9.DAO.ContaContabilServiceDAO;
import ctbli9.modelo.ctb.ContaContabil;

@FacesConverter(forClass = ContaContabil.class)
public class ContaContabilConverter implements Converter {
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		ContaContabil conta = null;
		if (value == null) {
			return null;
		}
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			conta = ContaContabilServiceDAO.pesquisarConta(con.getConexao(), value);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConexaoDB.close(con);
		}
		return conta;
	}
	
	public String getAsString(FacesContext context, UIComponent component, Object object) throws ConverterException {
		if (object == null) {
			return null;
		}
		ContaContabil conta = (ContaContabil) object;
		return conta.getCdConta();
	}

}
