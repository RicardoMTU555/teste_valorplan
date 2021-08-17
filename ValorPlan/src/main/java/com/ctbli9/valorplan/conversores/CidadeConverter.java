package com.ctbli9.valorplan.conversores;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;

import ctbli9.modelo.Cidade;
import ctbli9.negocio.CidadeService;

@FacesConverter(forClass = Cidade.class)
public class CidadeConverter implements Converter {
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Cidade cidade = null;
		if (value == null) {
			return null;
		}
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			
			cidade = new CidadeService(con.getConexao()).pesquisar(Integer.parseInt(value));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConexaoDB.close(con);
		}
		return cidade;
	}
	
	public String getAsString(FacesContext context, UIComponent component, Object object) throws ConverterException {
		if (object == null) {
			return null;
		}
		Cidade cidade = (Cidade) object;
		return Integer.toString(cidade.getCdCidade());
	}

}
