package com.ctbli9.valorplan.conversores;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.Departamento;
import com.ctbli9.valorplan.negocio.DepartamentoService;

@FacesConverter(forClass = Departamento.class)
public class DepartamentoConverter implements Converter {
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Departamento depto = null;
		if (value == null) {
			return null;
		}
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();	
			depto = new DepartamentoService(con).pesquisarDepartamento(Long.parseLong(value));
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConexaoDB.close(con);
		}
		return depto;
	}
	
	public String getAsString(FacesContext context, UIComponent component, Object object) throws ConverterException {
		if (object == null) {
			return null;
		}
		Departamento depto = (Departamento) object;
		return Long.toString(depto.getCdDepartamento());
	}
}
