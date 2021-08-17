package com.ctbli9.valorplan.conversores;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("cpfConverter")
public class CpfConverter implements Converter {

    /**
    * <b>Metodo que remove a mascara do CPF.</b>
    *
    * @param facesContext
    * @param uIcomponent
    * @param cpf
    * @return Object
    */
    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uIcomponent, String cpf) {
    	
    	/*if (cpf.trim().equals("")) {
            return null;
        } else {
           cpf = cpf.replace("-", "");
           cpf = cpf.replace(".", "");
           return cpf;
       }*/
        
    	if(cpf != null || cpf != "") {
        	cpf = cpf.toString().replaceAll("[- /.]", "");
        	cpf = cpf.toString().replaceAll("[-()]", "");
    	}
    	return cpf;
    }

    /**
    * <b>Metodo que retorna a String do CPF.</b>
    *
    * @param facesContext
    * @param uIcomponent
    * @param object
    * @return String
    */
    @Override
    public String getAsString(FacesContext facesContext, UIComponent uIcomponent, Object valor) {
    	String cpf = valor.toString();
    	int tamanho = 0;
    	if (cpf.length() > 11)
    		tamanho = 14-cpf.length();
    	else
    		tamanho = 11-cpf.length();
    	
    	for (int i = 0; i < tamanho; i++) {
    		cpf = "0" + cpf;
		}
    	return cpf;
    }
}
