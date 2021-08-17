package com.ctbli9.valorplan.conversores;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import com.ctbli9.valorplan.DAO.bd.ConexaoDB;
import com.ctbli9.valorplan.modelo.Funcionario;
import com.ctbli9.valorplan.negocio.FuncionarioService;


@FacesConverter(forClass = Funcionario.class)
public class FuncionarioConverter implements Converter {
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Funcionario func = null;
		if (value == null) {
			return null;
		}
		ConexaoDB con = null;
		try {
			con = new ConexaoDB();
			func = new FuncionarioService(con.getConexao()).pesquisar(Integer.parseInt(value));
			ConexaoDB.gravarTransacao(con);
		} catch (Exception e) {
			ConexaoDB.desfazerTransacao(con);
			e.printStackTrace();
		} finally {
			ConexaoDB.close(con);
		}
		return func;
	}
	
	public String getAsString(FacesContext context, UIComponent component, Object object) throws ConverterException {
		if (object == null) {
			return null;
		}
		Funcionario func = (Funcionario) object;
		return Integer.toString(func.getCdFuncionario());
	}
}
