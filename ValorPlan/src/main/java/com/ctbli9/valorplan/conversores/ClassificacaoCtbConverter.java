package com.ctbli9.valorplan.conversores;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import com.ctbli9.valorplan.DAO.ClassificacaoCtbServiceDAO;
import com.ctbli9.valorplan.DAO.bd.ConexaoDB;

import ctbli9.modelo.ctb.ClasseDRE;

@FacesConverter(forClass = ClasseDRE.class)
public class ClassificacaoCtbConverter implements Converter {
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		ClasseDRE classe = null;
		if (value == null) {
			return null;
		}
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			classe = ClassificacaoCtbServiceDAO.pesquisarClassificacaoCtb(con.getConexao(), Integer.parseInt(value));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConexaoDB.close(con);
		}
		return classe;
	}
	
	public String getAsString(FacesContext context, UIComponent component, Object object) throws ConverterException {
		if (object == null) {
			return null;
		}
		ClasseDRE classe = (ClasseDRE) object;
		return Integer.toString(classe.getCdClasse());
	}
}
