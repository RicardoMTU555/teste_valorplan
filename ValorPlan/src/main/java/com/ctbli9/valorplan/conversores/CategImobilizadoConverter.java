package com.ctbli9.valorplan.conversores;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.CategImobilizado;
import com.ctbli9.valorplan.negocio.CategImobilizadoService;

@FacesConverter(forClass = CategImobilizado.class)
public class CategImobilizadoConverter implements Converter {
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		CategImobilizado cargo = null;
		if (value == null) {
			return null;
		}
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			cargo = new CategImobilizadoService(con.getConexao()).pesquisarCategImobilizado(Integer.parseInt(value));
			
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			ConexaoDB.close(con);
		}
		return cargo;
	}
	
	public String getAsString(FacesContext context, UIComponent component, Object object) throws ConverterException {
		if (object == null) {
			return null;
		}
		CategImobilizado cargo = (CategImobilizado) object;
		return Integer.toString(cargo.getCdCategImobili());
	}
}
