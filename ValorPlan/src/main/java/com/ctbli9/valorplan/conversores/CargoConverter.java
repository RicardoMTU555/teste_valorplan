package com.ctbli9.valorplan.conversores;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.negocio.CargoService;

import com.ctbli9.valorplan.modelo.Cargo;


@FacesConverter(forClass = Cargo.class)
public class CargoConverter implements Converter {
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Cargo cargo = null;
		if (value == null) {
			return null;
		}
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			cargo = new CargoService(con).pesquisarCargo(Integer.parseInt(value));
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
		Cargo cargo = (Cargo) object;
		return Integer.toString(cargo.getCdCargo());
	}
}
