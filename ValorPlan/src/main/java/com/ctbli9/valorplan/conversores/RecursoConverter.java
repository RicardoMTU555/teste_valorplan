package com.ctbli9.valorplan.conversores;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.orc.Recurso;
import com.ctbli9.valorplan.negocio.EquipeService;


@FacesConverter(forClass = Recurso.class)
public class RecursoConverter implements Converter {
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Recurso recurso = null;
		if (value == null) {
			return null;
		}
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			recurso = new EquipeService(con.getConexao()).getRecurso(Long.parseLong(value));
			ConexaoDB.gravarTransacao(con);
		} catch (Exception e) {
			ConexaoDB.desfazerTransacao(con);
			e.printStackTrace();
		} finally {
			ConexaoDB.close(con);
		}
		return recurso;
	}
	
	public String getAsString(FacesContext context, UIComponent component, Object object) throws ConverterException {
		if (object == null) {
			return null;
		}
		Recurso func = (Recurso) object;
		return Long.toString(func.getCdRecurso());
	}
}
